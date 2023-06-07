package com.pl00t.swipe_client.services.battle

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SwipeProcesor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNot

interface BattleService {
    suspend fun createMockBattle(): BattleDecorations
    suspend fun events(): Flow<BattleEvent>
    suspend fun processSwipe(dx: Int, dy: Int)
    suspend fun processUltimate()
}

class BattleServiceImpl() : BattleService {

    val processor = SwipeProcesor()
    lateinit var battle: Battle
    val events = MutableSharedFlow<BattleEvent>(200)

    override suspend fun createMockBattle(): BattleDecorations {
        battle = Battle(0, emptyList())
        val configuration = BattleConfiguration(
            humans = listOf(
                HumanConfiguration(
                    skin = UnitSkin.CHARACTER_VALERIAN,
                    level = 10,
                    body = 4,
                    spirit = 4,
                    mind = 4
                )
            ),
            waves = listOf(
                MonsterWaveConfiguration(
                    monsters = listOf(
                        MonsterConfiguration(
                            skin = UnitSkin.MONSTER_THORNSTALKER,
                            level = 10
                        ),
                        MonsterConfiguration(
                            skin = UnitSkin.MONSTER_THORNSTALKER,
                            level = 10
                        ),
                        MonsterConfiguration(
                            skin = UnitSkin.MONSTER_CORRUPTED_DRYAD,
                            level = 10
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
            it is BattleEvent.MergeTileEvent && it.unitId != 0
    }

    override suspend fun processSwipe(dx: Int, dy: Int) {
        val result = processor.processSwipe(battle, 0, dx, dy)
        result.events.forEach { events.emit(it) }
        battle = result.battle
    }

    override suspend fun processUltimate() {
        val result = processor.processUltimate(battle, 1)
        result.events.forEach { events.emit(it) }
        battle = result.battle
    }
}
