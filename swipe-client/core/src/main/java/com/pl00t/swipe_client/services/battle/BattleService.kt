package com.pl00t.swipe_client.services.battle

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SwipeProcesor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map

data class BattleResult(
    val victory: Boolean,
    val goldRewardCost: Int,
    val chronoShardsRewardCost: Int,
)

interface BattleService {
    suspend fun createMockBattle(): BattleDecorations
    suspend fun events(): Flow<BattleEvent>
    suspend fun processSwipe(dx: Int, dy: Int)
    suspend fun processUltimate()
    suspend fun battleEnd(): Flow<BattleResult>
}

class BattleServiceImpl() : BattleService {

    val processor = SwipeProcesor()
    lateinit var battle: Battle
    val events = MutableSharedFlow<BattleEvent>(200)
    val endBattle = MutableSharedFlow<BattleResult>(5)

    override suspend fun createMockBattle(): BattleDecorations {
        battle = Battle(0, emptyList(), 1)
        val configuration = BattleConfiguration(
            humans = listOf(
                HumanConfiguration(
                    skin = UnitSkin.CHARACTER_VALERIAN,
                    level = 1,
                    attributes = CharacterAttributes(mind = 1, body = 1, spirit = 1)
                )
            ),
            waves = listOf(
                MonsterWaveConfiguration(
                    monsters = listOf(
                        MonsterConfiguration(
                            skin = UnitSkin.MONSTER_THORNSTALKER,
                            level = 1,
                            baseHealth = 30
                        ),
                        MonsterConfiguration(
                            skin = UnitSkin.MONSTER_THORNSTALKER,
                            level = 1,
                            baseHealth = 30
                        ),
                        MonsterConfiguration(
                            skin = UnitSkin.MONSTER_CORRUPTED_DRYAD,
                            level = 1,
                            baseHealth = 20
                        )
                    )
                )
            )
        )
        val createResults = processor.createBattle(configuration, battle)
        createResults.events.forEach { events.emit(it) }
        battle = createResults.battle
        return BattleDecorations("location_groves")
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
    }

    override suspend fun processUltimate() {
        println("Processing ultimate")
        val result = processor.processUltimate(battle, 0)
        result.events.forEach { event ->
            handleEvent(event)
        }
        battle = result.battle
    }

    override suspend fun battleEnd() = endBattle

    private suspend fun handleEvent(event: BattleEvent) {
        if (event is BattleEvent.BattleEndEvent) {
            endBattle.emit(BattleResult(event.team == 0, 1000, 500))
        } else {
            events.emit(event)
        }
    }
}
