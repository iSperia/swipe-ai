package com.pl00t.swipe_client.services.battle

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SwipeProcesor
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.monsters.MonsterService
import com.pl00t.swipe_client.services.profile.SwipeAct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import java.lang.IllegalStateException

data class BattleResult(
    val victory: Boolean,
    val freeReward: Boolean,
    val goldRewardCost: Int,
    val chronoShardsRewardCost: Int,
)

interface BattleService {
    suspend fun createBattle(act: SwipeAct, level: String): BattleDecorations
    suspend fun events(): Flow<BattleEvent>
    suspend fun processSwipe(dx: Int, dy: Int)
    suspend fun processUltimate()
    suspend fun battleEnd(): Flow<BattleResult>
}

class BattleServiceImpl(
    private val levelService: LevelService,
    private val monsterService: MonsterService) : BattleService {

    val processor = SwipeProcesor()
    lateinit var battle: Battle
    val events = MutableSharedFlow<BattleEvent>(200)
    val endBattle = MutableSharedFlow<BattleResult>(5)

    override suspend fun createBattle(actId: SwipeAct, level: String): BattleDecorations {
        val actModel = levelService.getAct(actId)
        val levelModel = actModel.levels.firstOrNull { it.id == level } ?: return BattleDecorations("")

        val waves = levelModel.monsters?.map { wave ->
            MonsterWaveConfiguration(
                monsters = wave.map { monster ->
                    monsterService.getMonster(monster.skin).copy(level = monster.level)
                }
            )
        } ?: throw IllegalStateException("Did not find waves of monsters")

        battle = Battle(0, emptyList(), 1)
        val configuration = BattleConfiguration(
            humans = listOf(
                HumanConfiguration(
                    configuration = monsterService.getMonster(UnitSkin.CHARACTER_VALERIAN),
                    level = 1,
                    attributes = CharacterAttributes(mind = 1, body = 1, spirit = 1)
                )
            ),
            waves = waves
        )
        val createResults = processor.createBattle(configuration, battle)
        createResults.events.forEach { events.emit(it) }
        battle = createResults.battle
        return BattleDecorations("groves")
    }

    override suspend fun events(): Flow<BattleEvent> = events.filterNot {
        it is BattleEvent.CreateTileEvent && it.unitId != 0 ||
            it is BattleEvent.MoveTileEvent && it.unitId != 0 ||
            it is BattleEvent.MergeTileEvent && it.unitId != 0 ||
            it is BattleEvent.DestroyTileEvent && it.unitId != 0 ||
            it is BattleEvent.UltimateProgressEvent && it.unitId != 0 ||
            it is BattleEvent.TileEffectEvent && it.characterId != 0
    }.map { event ->
        if (event is BattleEvent.UltimateEvent) {
            event.copy(events = event.events.filterNot {
                it is BattleEvent.CreateTileEvent && it.unitId != 0 ||
                    it is BattleEvent.MoveTileEvent && it.unitId != 0 ||
                    it is BattleEvent.MergeTileEvent && it.unitId != 0 ||
                    it is BattleEvent.DestroyTileEvent && it.unitId != 0 ||
                    it is BattleEvent.UltimateProgressEvent && it.unitId != 0 ||
                    it is BattleEvent.TileEffectEvent && it.characterId != 0
            })
        } else event
    }

    override suspend fun processSwipe(dx: Int, dy: Int) {
        val result = processor.processSwipe(battle, 0, dx, dy)
        result.events.forEach { event ->
            handleEvent(event)
        }
        battle = result.battle
        checkBattleEnd()
    }

    override suspend fun processUltimate() {
        println("Processing ultimate")
        val result = processor.processUltimate(battle, 0)
        result.events.forEach { event ->
            handleEvent(event)
        }
        battle = result.battle
        checkBattleEnd()
    }

    override suspend fun battleEnd() = endBattle

    private suspend fun checkBattleEnd() {
        val hasTeam0 = battle.characters.any { it.team == 0 && it.human }
        val hasTeam1 = battle.characters.any { it.team == 1 }
        println("CHECK: $hasTeam0 $hasTeam1")
        if (!hasTeam0) {
            endBattle.emit(BattleResult(victory = false, freeReward = false, goldRewardCost = 0, chronoShardsRewardCost = 0))
            events.resetReplayCache()
            endBattle.resetReplayCache()
        } else if (!hasTeam1) {
            endBattle.emit(BattleResult(victory = true, freeReward = true, goldRewardCost = 0, chronoShardsRewardCost = 0))
            events.resetReplayCache()
            endBattle.resetReplayCache()
        }
    }

    private suspend inline fun handleEvent(event: BattleEvent) = events.emit(event)
}
