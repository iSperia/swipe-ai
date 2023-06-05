package com.pl00t.swipe_client.services.battle

import com.pl00t.swipe_client.services.battle.logic.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface BattleService {
    suspend fun createMockBattle(): BattleDecorations
    suspend fun events(): Flow<BattleEvent>
    suspend fun processSwipe(dx: Int, dy: Int)
    suspend fun processUltimate()
}

class BattleServiceImpl() : BattleService {

    val processor = SwipeProcesor()
    lateinit var battle: Battle
    lateinit var events: MutableSharedFlow<BattleEvent>

    override suspend fun createMockBattle(): BattleDecorations {
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
                        )
                    )
                )
            )
        )
        return BattleDecorations("location_groves")
    }

    override suspend fun events(): Flow<BattleEvent> = events

    override suspend fun processSwipe(dx: Int, dy: Int) {
        val result = processor.processSwipe(battle, 1, dx, dy)
        result.events.forEach { events.emit(it) }
    }

    override suspend fun processUltimate() {
        val result = processor.processUltimate(battle, 1)
        result.events.forEach { events.emit(it) }
    }
}
