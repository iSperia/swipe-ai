package com.pl00t.swipe_client.services.battle

import com.game7th.swipe.game.*
import com.pl00t.swipe_client.services.levels.LevelService
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.JsonObject
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

data class BattleRewardConfig(
    val currency: SwipeCurrency,
    val amount: Int,
)

data class BattleResult(
    val victory: Boolean,
    val rewards: List<BattleRewardConfig>,
)

interface BattleService {
    suspend fun createBattle(act: SwipeAct, level: String, tier: Int): BattleDecorations
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

    lateinit var context: SbContext
    lateinit var waves: List<List<SbMonsterEntry>>
    lateinit var events: MutableSharedFlow<SbDisplayEvent>
    lateinit var endBattle: MutableSharedFlow<BattleResult>
    private var processEnabled = false

    private var actId = SwipeAct.ACT_1
    private var level = "c1"
    private var tier = -1


    override suspend fun createBattle(actId: SwipeAct, level: String, tier: Int): BattleDecorations {
        events = MutableSharedFlow(100)
        endBattle = MutableSharedFlow(5)
        processEnabled = true
        this.actId = actId
        this.level = level
        this.tier = tier
        val actModel = levelService.getAct(actId)
        val levelModel = actModel.levels.firstOrNull { it.id == level } ?: return BattleDecorations("")

        val character = profileService.getCharacters().first()

        val game = SbGame(0, 1, 0, emptyList())
        val triggers = mutableSetOf<String>()

        val monsterConfig = monsterService.getMonster(character.skin)!!
        monsterService.loadTriggers(MonsterService.DEFAULT)
        monsterService.loadTriggers(monsterConfig.skin)
        triggers.addAll(monsterConfig.triggers)

        if (tier == -1) {
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
        } else if (levelModel.type == LevelType.BOSS) {
            //it is a boss!
            val monsterSkins = levelModel.monsters?.flatMap { it }?.map { it.skin } ?: emptyList()
            val monsterConfigs = monsterSkins.mapNotNull { monsterService.getMonster(it) }
            monsterConfigs.forEach {
                monsterService.loadTriggers(it.skin)
                triggers.addAll(it.triggers)
            }
            waves = listOf(monsterConfigs.map { SbMonsterEntry(it.skin, (tier + 1) * 5) })
        } else if (levelModel.type == LevelType.RAID && levelModel.monster_pool != null) {
            val totalWaves = if (Random.nextInt(20) < tier + 1) 4 else 3
            val totalWeight = levelModel.monster_pool.sumOf { it.weight }

            val waves = mutableListOf<List<SbMonsterEntry>>()
            (0 until totalWaves).forEach { waveIndex ->
                val waveMonsters = if (Random.nextFloat() < 0.25f) 2 else 3
                val monsters = mutableListOf<SbMonsterEntry>()
                (0 until waveMonsters).forEach { monsterIndex ->
                    val roll = Random.nextInt(totalWeight)
                    var sum = 0
                    val nextSkin = levelModel.monster_pool.first {
                        sum += it.weight
                        sum > roll
                    }.skin
                    val monsterConfig = monsterService.getMonster(nextSkin)!!
                    monsterService.loadTriggers(monsterConfig.skin)
                    triggers.addAll(monsterConfig.triggers)
                    monsters.add(SbMonsterEntry(monsterConfig.skin, (tier + 1) * 5))
                }
                waves.add(monsters)
            }
            this.waves = waves
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
            initHumans(listOf(SbHumanEntry(character.skin, character.level.level, character.attributes, profileService.getItems().filter { it.equippedBy == character.skin })))
            initWave(waves[0])
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
                if (tier >= 0) {
                    profileService.unlockTier(actId, level, tier + 1)
                }
                processEnabled = false
            }
        }
        val eventsToEmit = context.events.map { it }
        eventsToEmit.forEach { e -> events.emit(e) }
        context.events.removeAll(eventsToEmit)
    }
}
