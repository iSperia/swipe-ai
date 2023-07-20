package com.pl00t.swipe_client.services.battle

import com.game7th.swipe.battle.*
import com.game7th.swipe.game.*
import com.game7th.swipe.game.SbComponent
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
    lateinit var component: SbComponent
    lateinit var context: SbContext
    lateinit var waves: List<List<SbMonsterEntry>>
    val events = MutableSharedFlow<SbDisplayEvent>(200)
    val endBattle = MutableSharedFlow<BattleResult>(5)

    private var actId = SwipeAct.ACT_1
    private var level = "c1"

    override suspend fun createBattle(actId: SwipeAct, level: String): BattleDecorations {

        this.actId = actId
        this.level = level
        val actModel = levelService.getAct(actId)
        val levelModel = actModel.levels.firstOrNull { it.id == level } ?: return BattleDecorations("")

        val character = profileService.getCharacters().first()

        val game = SbGame(0, 1, 0, emptyList())
        val triggers = mutableSetOf("common.poison")
        val balances = mutableMapOf<String, JsonObject>()
        monsterService.getMonster(character.skin)?.let { c ->
            triggers.addAll(c.triggers)
            balances[c.skin] = c.balance
        }
        waves = levelModel.monsters ?: emptyList()
        levelModel.monsters?.forEach { wave ->
            wave.forEach { monster ->
                monsterService.getMonster(monster.skin)?.let { c ->
                    triggers.addAll(c.triggers)
                    balances[c.skin] = c.balance
                }
            }
        }

        component = DaggerSbComponent.builder()
            .balances(balances)
            .build()

        val triggerMap = component.provideTriggers()

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
            triggers = triggers.mapNotNull { triggerMap[it] }
        ).apply {
            initHumans(component, listOf(SbHumanEntry(character.skin, character.level.level, character.attributes)))
            initWave(component, levelModel.monsters?.get(0)!!)
        }
        handleContext()
        return BattleDecorations(levelModel.background)
    }

    override suspend fun events(): Flow<SbDisplayEvent> = events.filter { event ->
        event !is SbDisplayEvent.SbCharacterSpecificEvent || event.characterId == 0
    }

    override suspend fun processSwipe(dx: Int, dy: Int) {
        context.swipe(0, dx, dy)
        handleContext()
    }

    override suspend fun processUltimate() {
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
        } else if (!context.game.teamAlive(1)) {
            val wavesTotal = waves.size
            if (context.game.wave < wavesTotal - 1) {
                context.game = context.game.copy(wave = context.game.wave + 1)
                context.initWave(component, waves[context.game.wave])
                events.emit(SbDisplayEvent.SbWave(context.game.wave + 1))
            } else {
                endBattle.emit(
                    BattleResult(
                    victory = true,
                    emptyList()
                ))
                profileService.markActComplete(actId, level)
                events.resetReplayCache()
                endBattle.resetReplayCache()
            }
        }
        context.events.forEach { e -> events.emit(e) }
        context.events.clear()
    }
}
