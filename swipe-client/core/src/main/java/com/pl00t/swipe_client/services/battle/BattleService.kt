package com.pl00t.swipe_client.services.battle

import com.game7th.swipe.battle.*
import com.game7th.swipe.game.*
import com.pl00t.swipe_client.services.levels.LevelService
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.JsonObject
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

data class BattleRewardConfig(
    val currency: SwipeCurrency,
    val amount: Int,
)

data class BattleResult(
    val victory: Boolean,
    val rewards: List<BattleRewardConfig>,
)

interface BattleService {
    suspend fun createBattle(act: SwipeAct, level: String): BattleDecorations
    suspend fun events(): Flow<SbDisplayEvent>
    suspend fun processSwipe(dx: Int, dy: Int)
    suspend fun processUltimate()
    suspend fun battleEnd(): Flow<BattleResult>
}

@ExperimentalCoroutinesApi
class BattleServiceImpl(
    private val levelService: LevelService,
    private val monsterService: MonsterService,
    private val profileService: ProfileService,) : BattleService {

    private var configuration: BattleConfiguration? = null
    lateinit var context: SbContext
    lateinit var waves: List<List<SbMonsterEntry>>
    lateinit var events: MutableSharedFlow<SbDisplayEvent>
    lateinit var endBattle: MutableSharedFlow<BattleResult>
    private var processEnabled = false

    private var actId = SwipeAct.ACT_1
    private var level = "c1"

    override suspend fun createBattle(actId: SwipeAct, level: String): BattleDecorations {
        events = MutableSharedFlow(20)
        endBattle = MutableSharedFlow(5)
        processEnabled = true
        this.actId = actId
        this.level = level
        val actModel = levelService.getAct(actId)
        val levelModel = actModel.levels.firstOrNull { it.id == level } ?: return BattleDecorations("")

        val character = profileService.getCharacters().first()

        val game = SbGame(0, 1, 0, emptyList())
        val triggers = mutableSetOf<String>()

        monsterService.loadTriggers(MonsterService.DEFAULT)
        monsterService.getMonster(character.skin)?.let { c ->
            monsterService.loadTriggers(c.skin)
            triggers.addAll(c.triggers)
        }
        waves = levelModel.monsters ?: emptyList()
        levelModel.monsters?.forEach { wave ->
            wave.forEach { monster ->
                monsterService.getMonster(monster.skin)?.let { c ->
                    monsterService.loadTriggers(c.skin)
                    triggers.addAll(c.triggers)
                }
            }
        }

        context = SbContext(
            game = game,
            balance = object : SbBalanceProvider {
                override fun getBalance(key: String): JsonObject {
                    TODO("Not yet implemented")
                }

                override fun getMonster(skin: String): SbMonsterConfiguration {
                    return runBlocking { monsterService.getMonster(skin)!! }
                }
            },
            triggers = triggers.mapNotNull { monsterService.getTrigger(it) }
        ).apply {
            initHumans(listOf(SbHumanEntry(character.skin, character.level.level, character.attributes)))
            initWave(levelModel.monsters?.get(0)!!)
        }
        handleContext()
        return BattleDecorations(levelModel.background)
    }

    override suspend fun events(): Flow<SbDisplayEvent> = events.filter { event ->
        event !is SbDisplayEvent.SbCharacterSpecificEvent || event.characterId == 0
    }

    override suspend fun processSwipe(dx: Int, dy: Int) {
        if (!processEnabled) return
        context.swipe(0, dx, dy)
        handleContext()
    }

    override suspend fun processUltimate() {
        if (!processEnabled) return
        context.game.character(0)?.let { character ->
            if (character.ultimateProgress == character.maxUltimateProgress) {
                context.useUltimate(0)
                handleContext()
            }
        }
    }

    override suspend fun battleEnd() = endBattle

    private suspend fun handleContext() {
        if (!context.game.teamAlive(0)) {
            endBattle.emit(
                BattleResult(
                victory = false,
                rewards = emptyList()
            ))
            processEnabled = false
        } else if (!context.game.teamAlive(1)) {
            val wavesTotal = waves.size
            if (context.game.wave < wavesTotal - 1) {
                context.game = context.game.copy(wave = context.game.wave + 1)
                context.initWave(waves[context.game.wave])
                events.emit(SbDisplayEvent.SbWave(context.game.wave + 1))
            } else {
                endBattle.emit(
                    BattleResult(
                    victory = true,
                    emptyList()
                ))
                profileService.markActComplete(actId, level)
                processEnabled = false
            }
        }
        val eventsToEmit = context.events.map { it.also { println(it) } }
        eventsToEmit.forEach { e -> events.emit(e) }
        context.events.removeAll(eventsToEmit)
        println("--------------------------------")
    }
}
