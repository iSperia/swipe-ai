package com.pl00t.swipe_client.services.battle

import com.game7th.items.ItemAffixType
import com.game7th.swipe.SbText
import com.game7th.swipe.game.*
import com.pl00t.swipe_client.services.levels.LevelService
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.JsonObject
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.services.profile.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.lang.IllegalStateException
import kotlin.random.Random

data class ExperienceResult(
    val skin: String,
    val name: SbText,
    val expBoost: Int,
)

sealed interface EncounterResultModel {
    data class BattleResult(
        val victory: Boolean,
        val act: SwipeAct,
        val level: String,
        val tier: Int,
        val exp: ExperienceResult?,
        val freeRewards: List<FrontItemEntryModel>,
        val extraRewardsCost: Int,
    ) : EncounterResultModel

    data class MineResult(
        val gems: List<FrontItemEntryModel.GemItemEntryModel>,
        val level: Int,
    ) : EncounterResultModel
}

interface BattleService {
    suspend fun createBattle(act: SwipeAct, level: String, tier: Int)
    suspend fun events(): Flow<SbDisplayEvent>
    suspend fun processSwipe(dx: Int, dy: Int)
    suspend fun processUltimate()
    suspend fun battleEnd(): Flow<EncounterResultModel.BattleResult>
    suspend fun getDecorations(): BattleDecorations
    suspend fun getActId(): SwipeAct
    suspend fun getLevelId(): String
}

@ExperimentalCoroutinesApi
class BattleServiceImpl(
    private val levelService: LevelService,
    private val monsterService: MonsterService,
    private val profileService: ProfileService,) : BattleService {

    lateinit var context: SbContext
    lateinit var waves: List<List<FrontMonsterConfiguration>>
    lateinit var events: MutableSharedFlow<SbDisplayEvent>
    lateinit var endBattle: MutableSharedFlow<EncounterResultModel.BattleResult>
    private var processEnabled = false

    private var actId = SwipeAct.ACT_1
    private var level = "c1"
    private var tier = -1
    private var rewardCollectCost = 0
    private var experienceIfWin = 0

    override suspend fun getDecorations(): BattleDecorations {
        val level = levelService.getAct(actId).levels.firstOrNull { it.id == level } ?: throw IllegalStateException("No decorations found")
        return BattleDecorations(level.background, level.music ?: "theme_verdant_grove")
    }

    override suspend fun getActId() = actId

    override suspend fun getLevelId() = level

    override suspend fun createBattle(actId: SwipeAct, level: String, tier: Int) {
        events = MutableSharedFlow(100)
        endBattle = MutableSharedFlow(5)
        processEnabled = true
        this.actId = actId
        this.level = level
        this.tier = tier
        val actModel = levelService.getAct(actId)
        val levelModel = actModel.levels.firstOrNull { it.id == level } ?: throw IllegalStateException("No level found")
        rewardCollectCost = when (levelModel.type) {
            LevelType.CAMPAIGN -> 0
            LevelType.BOSS -> 200
            LevelType.RAID -> 120
            else -> 0
        }

        val character = profileService.getCharacters().first { it.skin == profileService.getActiveCharacter() }

        val tutorial = if (actId == SwipeAct.ACT_1 && level == "c1" && !profileService.getTutorial().c1BattleIntroPassed) {
            GameTutorialMetadata.DEFAULT.copy(isFirstTutorial = true)
        } else {
            GameTutorialMetadata.DEFAULT
        }

        val game = SbGame(0, 1, 0, emptyList(), tutorial)
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
            waves = levelModel.monsters?.map { it.map { monsterService.createMonster(it.skin, it.level, it.rarity) } } ?: emptyList()
            levelModel.monsters?.forEach { wave ->
                wave.forEach { monster ->
                    monsterService.getMonster(monster.skin)?.let { c ->
                        monsterService.loadTriggers(c.skin)
                        triggers.addAll(c.triggers)
                    }
                }
            }
        } else if ((levelModel.type == LevelType.BOSS || levelModel.type == LevelType.RAID) && levelModel.tiers != null) {
            val totalWaves = if (levelModel.type == LevelType.BOSS) 1 else if (Random.nextInt(20) < tier + 1) 5 else 4
            val totalWeight = levelModel.tiers[tier].monster_pool.sumOf { it.weight }

            val waves = mutableListOf<List<FrontMonsterConfiguration>>()
            (0 until totalWaves).forEach { waveIndex ->
                val waveMonsters = if (levelModel.type == LevelType.BOSS) 1 else if (Random.nextFloat() < 0.1f) 2 else 3
                val monsters = mutableListOf<FrontMonsterConfiguration>()
                (0 until waveMonsters).forEach { monsterIndex ->
                    val roll = Random.nextInt(totalWeight)
                    var sum = 0

                    val nextMonster = levelModel.tiers[tier].monster_pool.first {
                        sum += it.weight
                        sum > roll
                    }
                    val nextSkin = nextMonster.skin
                    val nextLevel = nextMonster.level
                    val rarityRoll = Random.nextInt(14)
                    val nextRarity = if (levelModel.type == LevelType.BOSS) 0 else when (rarityRoll) {
                        0,1,2,3,4 -> 0
                        5,6,7,8 -> 1
                        9,10,11 -> 2
                        else -> 3
                    }

                    val monsterConfig = monsterService.getMonster(nextSkin)!!
                    monsterService.loadTriggers(monsterConfig.skin)
                    triggers.addAll(monsterConfig.triggers)
                    monsters.add(monsterService.createMonster(nextSkin, nextLevel, nextRarity))
                }
                waves.add(monsters)
            }
            this.waves = waves
        }

        val expBoostCoef = profileService.getItems()
            .filter { it.equippedBy == profileService.getActiveCharacter() }
            .flatMap { it.affixes + it.enchant + it.implicit }
            .filterNotNull()
            .sumOf {
                if (it.affix == ItemAffixType.EXP_BOOST_PERCENT) {
                    it.value.toDouble()
                } else {
                    0.0
                }
            }.toFloat() / 100f + 1f

        experienceIfWin = (waves.flatMap { it }.sumOf {
            val bonusExpRarity = when (it.rarity) {
                0 -> 0
                1 -> 2
                2 -> 4
                3 -> 6
                else -> 8
            }
            it.level * (10 + bonusExpRarity)
        }.toFloat() * expBoostCoef).toInt()


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
            triggers = triggers.mapNotNull { monsterService.getTrigger(it) },
        ).apply {
            initHumans(listOf(profileService.createCharacter(character.skin)))
            initWave(waves[0])
        }
        handleContext()
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
            endBattle.emit(EncounterResultModel.BattleResult(victory = false, actId, level, tier, null, emptyList(), 0))
            processEnabled = false
        } else if (!context.game.teamAlive(1)) {
            val wavesTotal = waves.size
            if (context.game.wave < wavesTotal - 1) {
                context.game = context.game.copy(wave = context.game.wave + 1)
                context.initWave(waves[context.game.wave])
                events.emit(SbDisplayEvent.SbWave(context.game.wave + 1))
            } else {
                var freeRewards = emptyList<FrontItemEntryModel>()
                if (tier == -1) {
                    if (profileService.isFreeRewardAvailable(actId, level)) {
                        freeRewards = profileService.collectFreeReward(actId, level, tier)
                    }
                } else {
                    freeRewards = profileService.collectFreeRaidReward(experienceIfWin)
                }
                profileService.addCharacterExperience(profileService.getActiveCharacter(), experienceIfWin)
                val monster = monsterService.getMonster(profileService.getActiveCharacter())!!
                endBattle.emit(EncounterResultModel.BattleResult(
                    victory = true,
                    act = actId,
                    level = level,
                    tier = tier,
                    exp = ExperienceResult(monster.skin, monster.name, experienceIfWin),
                    freeRewards = freeRewards,
                    extraRewardsCost = if (tier == -1) 0 else rewardCollectCost))
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
