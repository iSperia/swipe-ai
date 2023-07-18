package com.pl00t.swipe_client.services.battle

import com.game7th.swipe.battle.*
import com.game7th.swipe.game.SbBalanceProvider
import com.game7th.swipe.game.SbContext
import com.game7th.swipe.game.SbGame
import com.game7th.swipe.game.di.SbComponent
import com.pl00t.swipe_client.services.levels.LevelService
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.JsonObject
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import java.lang.IllegalStateException

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
    suspend fun events(): Flow<BattleEvent>
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
    lateinit var game: SbGame
    lateinit var component: SbComponent
    lateinit var context: SbContext
    val events = MutableSharedFlow<BattleEvent>(200)
    val endBattle = MutableSharedFlow<BattleResult>(5)

    private var actId = SwipeAct.ACT_1
    private var level = "c1"

    override suspend fun createBattle(actId: SwipeAct, level: String): BattleDecorations {
        this.actId = actId
        this.level = level
        val actModel = levelService.getAct(actId)
        val levelModel = actModel.levels.firstOrNull { it.id == level } ?: return BattleDecorations("")

        val character = profileService.getCharacters().first()

        game = SbGame(0, 1, 0, emptyList())
        context = SbContext(
            game = game,
            balance = object : SbBalanceProvider {
                override fun getBalance(key: String): JsonObject {
                    TODO("Not yet implemented")
                }

                override fun getMonster(skin: String): SbMonsterConfiguration {
                    TODO("Not yet implemented")
                }
            },
            triggers = emptyList()
        )
        return BattleDecorations(levelModel.background)
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

    }

    override suspend fun processUltimate() {

    }

    override suspend fun battleEnd() = endBattle

    private suspend fun checkBattleEnd() {

    }

    private suspend inline fun handleEvent(event: BattleEvent) = events.emit(event)
}
