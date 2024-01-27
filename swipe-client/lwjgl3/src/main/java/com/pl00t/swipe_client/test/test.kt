package com.pl00t.swipe_client.test

import com.game7th.items.ItemAffix
import com.game7th.items.ItemAffixType
import com.game7th.swipe.game.SbMonsterConfiguration
import com.game7th.swipe.game.*
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.Gson
import com.pl00t.swipe_client.services.MonsterServiceImpl
import com.pl00t.swipe_client.services.files.FileService
import com.pl00t.swipe_client.services.items.ItemService
import com.pl00t.swipe_client.services.items.ItemServiceImpl
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.levels.LevelServiceImpl
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.generateCharacter
import kotlinx.coroutines.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.io.File
import java.lang.IllegalStateException
import kotlin.math.max
import kotlin.math.min

data class ProgressionEntry(
    val act: SwipeAct,
    val level: String,
    val characterAttributes: CharacterAttributes,
    val characterSkin: String,
    val characterItems: List<TestItemDescription>,
    val characterLevel: Int,
    val targetSwipes: Int,
)

data class ProgressionFile(
    val tests: List<ProgressionEntry>
)

data class TestResult(
    val act: SwipeAct,
    val level: String,
    val winrate: Float,
    val targetSwipes: Int,
    val deltaSwipes: Int,
    val avgSwipe: Int,
)

data class TestItemDescription(
    val skin: String,
    val rarity: Int,
    val level: Int,
)

private const val ITERATIONS = 500

private suspend fun createCharacter(monsterService: MonsterService, itemService: ItemService, attributes: CharacterAttributes, skin: String,
                                    items: List<TestItemDescription>, level: Int, rarity: Int): FrontMonsterConfiguration {
        val affixes = mutableListOf<ItemAffix>()
        items.forEach { item ->
            val template = itemService.getItemTemplate(item.skin)!!
            val implicitLevel = item.rarity + 2
            val implicitAffix = itemService.getAffix(template.implicit)!!
            affixes.add(ItemAffix(implicitAffix.affix, implicitAffix.valuePerTier * implicitLevel, implicitLevel, true))

            val affixCount = max(4, item.rarity + 1)
            val guaranteedTiers = (item.level - 1) / affixCount
            val extraTiers = (item.level - 1) % affixCount
            val affixesFilled = mutableListOf<ItemAffixType>()
            (0 until affixCount).forEach { index ->
                val affix = itemService.generateAffix(affixesFilled, template)
                val affixMeta = itemService.getAffix(affix)!!
                affixesFilled.add(affix)
                val affixLevel = 1 + guaranteedTiers + (if (index < extraTiers) 1 else 0)
                affixes.add(ItemAffix(affix, affixMeta.valuePerTier * affixLevel, affixLevel, true))
            }
        }

        return generateCharacter(monsterService, level, rarity, skin, attributes, affixes)
}

data class LevelTestResult(
    val winrate: Float,
    val avgSwipes: Int
)

suspend fun testLevel(
    levelService: LevelService,
    monsterService: MonsterService,
    itemService: ItemService,
    progression: ProgressionEntry
): LevelTestResult {
    var victories = 0
    var swipes = 0
    (0 until ITERATIONS).forEach { _ ->
        val game = SbGame(0, 1, 0, emptyList())
        val triggers = mutableSetOf<String>()
        monsterService.loadTriggers(MonsterService.DEFAULT)
        monsterService.getMonster(progression.characterSkin)?.let { c ->
            monsterService.loadTriggers(c.skin)
            triggers.addAll(c.triggers)
        }
        val levelModel = levelService.getAct(progression.act).levels.firstOrNull { it.id == progression.level } ?: throw IllegalStateException()

        val waves = levelModel.monsters ?: emptyList()
        levelModel.monsters?.forEach { wave ->
            wave.forEach { monster ->
                monsterService.getMonster(monster.skin)?.let { c ->
                    monsterService.loadTriggers(c.skin)
                    triggers.addAll(c.triggers)
                }
            }
        }

        var context: SbContext = SbContext(
            game = game,
            balance = object : SbBalanceProvider {
                override fun getBalance(key: String) = TODO("Not Implemented")

                override fun getMonster(skin: String): SbMonsterConfiguration = runBlocking { monsterService.getMonster(skin)!! }
            },
            triggers = triggers.mapNotNull { monsterService.getTrigger(it) }
        ).apply {
            initHumans(listOf(createCharacter(monsterService, itemService, progression.characterAttributes, progression.characterSkin, progression.characterItems, progression.characterLevel, 0)))
            initWave(levelModel.monsters?.get(0)!!.map { monsterService.createMonster(it.skin, it.level, it.rarity) })
        }

        var victory = -1
        while (victory < 0) {
            context.game.character(0)?.let { hero ->
                if (hero.ultimateProgress == hero.maxUltimateProgress) {
                    context.useUltimate(0)
                }
                var evaluation = Integer.MIN_VALUE
                (0 until 4).forEach {action ->
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

                    val contextCopy = context.copy()
                    contextCopy.swipe(0, dx, dy)
                    swipes++
                    var maxSubEv = contextCopy.evaluateSimple()

                    if (maxSubEv > evaluation) {
                        context = contextCopy
                    }
                }

                context.events.clear()

                if (!context.game.teamAlive(0)) {
                    victory = 1
                } else if (!context.game.teamAlive(1)) {
                    val wavesTotal = waves.size
                    if (context.game.wave < wavesTotal - 1) {
                        context.game = context.game.copy(wave = context.game.wave + 1)
                        context.initWave(waves[context.game.wave]!!.map { monsterService.createMonster(it.skin, it.level, it.rarity) })
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
    return LevelTestResult(
        victories.toFloat() / ITERATIONS,
        avgSwipes = swipes / ITERATIONS)
}

suspend fun testAct(
    gson: Gson,
    fileService: FileService,
    monsterService: MonsterService,
    itemService: ItemService,
    levelService: LevelService,
    act: SwipeAct
) {
    coroutineScope {
        launch(newFixedThreadPoolContext(10, "Async")) {
            val progressionFile = gson.fromJson(fileService.localFile("json/tests/test_$act.json"), ProgressionFile::class.java)
            val resultMap = mutableMapOf<String, TestResult>()
            val jobs = mutableListOf<Deferred<Unit>>()
            progressionFile.tests.forEach { entry ->
                jobs.add(async {
                    val testResult = testLevel(levelService, monsterService, itemService, entry)
                    val avgSwipes = testResult.avgSwipes
                    val totalSwipes = (avgSwipes / testResult.winrate).toInt()
                    println("$entry\nwinrate=${testResult.winrate}; avgSwipes=$avgSwipes\nthread=${Thread.currentThread().name}\n\n")
                    resultMap[entry.level] = TestResult(entry.act, entry.level, testResult.winrate, entry.targetSwipes, totalSwipes - entry.targetSwipes, avgSwipes)
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
                            tr.grey {
                                background: #cccccc
                            }
                            tr.white {
                                background: #eeeeee
                            }
                        """.trimIndent())
                            }
                        }
                    }
                    body {
                        table {
                            thead {
                                tr() {
                                    td { text("Act") }
                                    td { text("Level") }
                                    td { text("Config") }
                                    td { text("Avg. Swipes") }
                                    td { text("Tot. Swipes") }
                                    td { text("Winrate") }
                                    td { text("Target Swipes") }
                                    td { text("Delta winrate") }
                                    td { text("Estimate time to complete") }
                                }
                            }
                            tbody {
                                progressionFile.tests.forEachIndexed { i, progression ->
                                    tr(classes = if (i % 2 == 0) "grey" else "white") {
                                        td {
                                            text(progression.act.toString())
                                        }
                                        td {
                                            text(progression.level.toString())
                                        }
                                        td {
                                            p { text("${progression.characterSkin} Lv.${progression.characterLevel}") }
                                            p { text("${progression.characterAttributes}") }
                                            progression.characterItems.forEach {
                                                p { text("$it")}
                                            }
                                        }
                                        td {
                                            text("${resultMap[progression.level]?.avgSwipe ?: 0}")
                                        }
                                        td {
                                            val avgSwipes = resultMap[progression.level]?.avgSwipe ?: 0
                                            val winrate = resultMap[progression.level]?.winrate ?: 0f
                                            val totalSwipes = (avgSwipes / winrate).toInt()
                                            text("$totalSwipes")
                                        }
                                        td {
                                            text("${((resultMap[progression.level]?.winrate ?: 0f) * 100f).toInt()}%")
                                        }
                                        td {
                                            text("${(resultMap[progression.level]?.targetSwipes ?: 0)}")
                                        }

                                        val delta = (resultMap[progression.level]?.deltaSwipes ?: 0)
                                        val percDelta = delta.toFloat() / (resultMap[progression.level]?.targetSwipes ?: 0)
                                        val c = if (percDelta >= -0.1f && percDelta <= 0.1f) "green"
                                        else if (percDelta <= -0.1f && percDelta >= -0.25f) "blue"
                                        else if (percDelta <= -0.25f) "violet"
                                        else if (percDelta >= 0.1f && percDelta <= 0.25f) "yellow"
                                        else "red"

                                        td(classes = c) {
                                            text("$delta")
                                        }
                                        td {
                                            resultMap[progression.level]?.let {
                                                val seconds = (it.avgSwipe * 1f / it.winrate).toInt()
                                                val min = seconds / 60
                                                val sec = seconds % 60
                                                text("$min m. $sec s.")
                                            }
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
            val file = File("assets\\$name")
            if (!file.exists()) return null
            return file.readText()
        }

        override fun internalFile(name: String): String? {
            val file = File("assets\\$name")
            if (!file.exists()) return null
            return file.readText()
        }
    }

    val monsterService = MonsterServiceImpl(fileService)
    val levelService = LevelServiceImpl(fileService, monsterService)
    val itemService = ItemServiceImpl(Gson(), fileService)

    testAct(Gson(), fileService, monsterService, itemService, levelService, SwipeAct.ACT_1)
}
