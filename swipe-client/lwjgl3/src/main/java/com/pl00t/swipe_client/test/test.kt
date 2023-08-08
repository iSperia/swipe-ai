package com.pl00t.swipe_client.test

import com.game7th.swipe.game.SbMonsterConfiguration
import com.game7th.swipe.game.*
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.Gson
import com.pl00t.swipe_client.services.MonsterServiceImpl
import com.pl00t.swipe_client.services.files.FileService
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.levels.LevelServiceImpl
import com.pl00t.swipe_client.services.profile.SwipeAct
import kotlinx.coroutines.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.io.File
import kotlin.random.Random

data class ProgressionEntry(
    val act: SwipeAct,
    val level: String,
    val characterAttributes: CharacterAttributes,
    val characterSkin: String,
    val characterLevel: Int,
    val targetWinrate: Float,
)

data class ProgressionFile(
    val tests: List<ProgressionEntry>
)

data class TestResult(
    val act: SwipeAct,
    val level: String,
    val winrate: Float,
    val targetWinrate: Float,
    val delta: Float
)

private const val ITERATIONS = 1000

suspend fun testLevel(
    levelService: LevelService,
    monsterService: MonsterService,
    progression: ProgressionEntry
): Float {
    var victories = 0
    (0 until ITERATIONS).forEach { _ ->
        val game = SbGame(0, 1, 0, emptyList())
        val triggers = mutableSetOf<String>()
        monsterService.loadTriggers(MonsterService.DEFAULT)
        monsterService.getMonster(progression.characterSkin)?.let { c ->
            monsterService.loadTriggers(c.skin)
            triggers.addAll(c.triggers)
        }
        val levelModel = levelService.getAct(progression.act).levels.firstOrNull { it.id == progression.level } ?: return 0f

        val waves = levelModel.monsters ?: emptyList()
        levelModel.monsters?.forEach { wave ->
            wave.forEach { monster ->
                monsterService.getMonster(monster.skin)?.let { c ->
                    monsterService.loadTriggers(c.skin)
                    triggers.addAll(c.triggers)
                }
            }
        }

        val context: SbContext = SbContext(
            game = game,
            balance = object : SbBalanceProvider {
                override fun getBalance(key: String) = TODO("Not Implemented")

                override fun getMonster(skin: String): SbMonsterConfiguration = runBlocking { monsterService.getMonster(skin)!! }
            },
            triggers = triggers.mapNotNull { monsterService.getTrigger(it) }
        ).apply {
//            initHumans(listOf(SbHumanEntry(progression.characterSkin, progression.characterLevel, progression.characterAttributes, emptyList())))
//            initWave(levelModel.monsters?.get(0)!!)
        }

        var victory = -1
        while (victory < 0) {
            context.game.character(0)?.let { hero ->
                if (hero.ultimateProgress == hero.maxUltimateProgress) {
                    context.useUltimate(0)
                }
                val action = Random.nextInt(4)
                val dx = when (action) {
                    0 -> 1
                    1,3 -> 0
                    else -> -1
                }
                val dy = when (action) {
                    0, 2 -> 0
                    1 -> 1
                    else -> -1
                }
                context.swipe(0, dx, dy)
//                context.events.forEach { e ->
//                    println(e)
//                }
                context.events.clear()

                if (!context.game.teamAlive(0)) {
                    victory = 1
                } else if (!context.game.teamAlive(1)) {
                    val wavesTotal = waves.size
                    if (context.game.wave < wavesTotal - 1) {
                        context.game = context.game.copy(wave = context.game.wave + 1)
//                        context.initWave(waves[context.game.wave])
                    } else {
                        victory = 0
                    }
                }
            }
        }
        if (victory == 0) {
            victories++
        }
    }
    return victories.toFloat() / ITERATIONS
}

suspend fun testAct(
    gson: Gson,
    fileService: FileService,
    monsterService: MonsterService,
    levelService: LevelService,
    act: SwipeAct
) {
    coroutineScope {
        launch(newFixedThreadPoolContext(10, "Async")) {
            val progressionFile = gson.fromJson(fileService.localFile("assets/json/tests/test_$act.json"), ProgressionFile::class.java)
            val resultMap = mutableMapOf<String, TestResult>()
            val jobs = mutableListOf<Deferred<Unit>>()
            progressionFile.tests.forEach { entry ->
                jobs.add(async {
                    val winrate = testLevel(levelService, monsterService, entry)
                    val humanWinrate = winrate
                    println("$entry\nwinrate=$humanWinrate\nthread=${Thread.currentThread().name}\n\n")
                    resultMap[entry.level] = TestResult(entry.act, entry.level, humanWinrate, entry.targetWinrate, humanWinrate - entry.targetWinrate)
                })
            }
            jobs.forEach { it.await() }
            println("Calculatations complete")
            val reportFile = File("$act balance report.html")
            reportFile.createNewFile()
            reportFile.outputStream().use {

                val report = createHTML(true).html {
                    head {
                        style {
                            unsafe {
                                raw("""
                            td.green {
                                background-color: #57a237;
                            }
                            td.yellow {
                                background-color: #b3a339;
                            }
                            td.blue {
                                background-color: #3989b3;
                            }
                            td.red {
                                background-color: #b34739;
                            }
                            td.violet {
                                background-color: #6639b3;
                            }
                        """.trimIndent())
                            }
                        }
                    }
                    body {
                        table {
                            thead {
                                tr {
                                    td { text("Act") }
                                    td { text("Level") }
                                    td { text("Config") }
                                    td { text("Winrate") }
                                    td { text("Target Winrate") }
                                    td { text("Delta winrate") }
                                }
                            }
                            tbody {
                                progressionFile.tests.forEach { progression ->
                                    tr {
                                        td {
                                            text(progression.act.toString())
                                        }
                                        td {
                                            text(progression.level.toString())
                                        }
                                        td {
                                            text("${progression.characterSkin} ${progression.characterLevel} ${progression.characterAttributes}")
                                        }
                                        td {
                                            text("${((resultMap[progression.level]?.winrate ?: 0f) * 100f).toInt()}%")
                                        }
                                        td {
                                            text("${((resultMap[progression.level]?.targetWinrate ?: 0f) * 100f).toInt()}%")
                                        }
                                        val delta = ((resultMap[progression.level]?.delta ?: 0f) * 100f).toInt()
                                        val c = if (delta >= -5 && delta <= 5) "green"
                                        else if (delta in 6..10) "blue"
                                        else if (delta > 10) "violet"
                                        else if (delta >= -10 && delta < -5) "yellow"
                                        else if (delta < -10) "red"
                                        else "green"
                                        td(classes = c) {

                                            text("$delta%")
                                        }
                                    }

                                }
                            }
                        }
                    }

                }.toString()

                reportFile.writeText(report)
            }
        }
    }
}

suspend fun main() {
    val fileService = object : FileService {
        override fun localFile(name: String): String? {
            val file = File("F:\\swipe\\swipe-ai\\swipe-client\\$name")
            if (!file.exists()) return null
            return file.readText()
        }

        override fun internalFile(name: String): String? {
            val file = File("F:\\swipe\\swipe-ai\\swipe-client\\assets\\$name")
            if (!file.exists()) return null
            return file.readText()
        }
    }

    val monsterService = MonsterServiceImpl(fileService)
    val levelService = LevelServiceImpl(fileService, monsterService)

    testAct(Gson(), fileService, monsterService, levelService, SwipeAct.ACT_1)
}
