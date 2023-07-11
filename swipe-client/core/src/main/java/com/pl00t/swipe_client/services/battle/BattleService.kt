package com.pl00t.swipe_client.services.battle

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SwipeProcesor
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.monsters.MonsterService
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import ktx.app.profile
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

    val processor = SwipeProcesor(monsterService)
    private var configuration: BattleConfiguration? = null
    lateinit var battle: Battle
    val events = MutableSharedFlow<BattleEvent>(200)
    val endBattle = MutableSharedFlow<BattleResult>(5)

    private var actId = SwipeAct.ACT_1
    private var level = "c1"

    override suspend fun createBattle(actId: SwipeAct, level: String): BattleDecorations {
        this.actId = actId
        this.level = level
        val actModel = levelService.getAct(actId)
        val levelModel = actModel.levels.firstOrNull { it.id == level } ?: return BattleDecorations("")

        val waves = levelModel.monsters?.map { wave ->
            MonsterWaveConfiguration(
                monsters = wave.map { monster ->
                    monsterService.getMonster(monster.skin).copy(level = monster.level)
                }
            )
        } ?: throw IllegalStateException("Did not find waves of monsters")

        val character = profileService.getCharacters().first()

        configuration = BattleConfiguration(
            humans = listOf(
                HumanConfiguration(
                    configuration = monsterService.getMonster(UnitSkin.CHARACTER_VALERIAN),
                    level = character.level.level,
                    attributes = CharacterAttributes(mind = character.attributes.mind, body = character.attributes.body, spirit = character.attributes.spirit)
                )
            ),
            waves = waves
        )
        battle = Battle(0, emptyList(), 1, waves, 0)
        val createResults = processor.createBattle(configuration!!, battle, 0)
        createResults.events.forEach { events.emit(it) }
        battle = createResults.battle
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
        val lastWave = battle.activeWave >= battle.waves.size - 1
        println("CHECK: $hasTeam0 $hasTeam1")
        if (!hasTeam0) {
            endBattle.emit(BattleResult(victory = false, emptyList()))
            events.resetReplayCache()
            endBattle.resetReplayCache()
        } else if (!hasTeam1) {
            if (lastWave) {
                endBattle.emit(BattleResult(victory = true, emptyList()))
                profileService.markActComplete(actId, level)
                events.resetReplayCache()
                endBattle.resetReplayCache()
            } else {
                events.emit(BattleEvent.WaveEvent(battle.activeWave + 2))
                val waveResult = processor.createBattle(configuration!!, battle, battle.activeWave + 1)
                battle = waveResult.battle.copy(activeWave = waveResult.battle.activeWave + 1)
                waveResult.events.forEach { events.emit(it) }
            }
        }
    }

    private suspend inline fun handleEvent(event: BattleEvent) = events.emit(event)
}
