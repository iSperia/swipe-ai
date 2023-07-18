package com.pl00t.swipe_client.services.profile

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.game7th.swipe.battle.UnitSkin
import com.game7th.swipe.battle.CharacterAttributes
import com.pl00t.swipe_client.services.levels.FrontActModel
import com.pl00t.swipe_client.services.levels.FrontLevelDetails
import com.pl00t.swipe_client.services.levels.LevelRewardType
import com.pl00t.swipe_client.services.levels.LevelService
import com.game7th.swipe.monsters.MonsterService
import java.lang.IllegalArgumentException
import kotlin.math.min

interface ProfileService {

    suspend fun getProfile(): SwipeProfile

    suspend fun markActComplete(act: SwipeAct, level: String)

    suspend fun getAct(act: SwipeAct): FrontActModel

    suspend fun isFreeRewardAvailable(act: SwipeAct, level: String): Boolean

    suspend fun collectFreeReward(act: SwipeAct, level: String): List<CollectedReward>

    suspend fun getCurrency(currency: SwipeCurrency): CurrencyMetadata

    suspend fun getCharacters(): List<SwipeCharacter>
    abstract fun spendExperienceCurrency(currency: SwipeCurrency, skin: String): SpendExperienceCurrencyResult

    data class SpendExperienceCurrencyResult(
        val character: SwipeCharacter,
        val balance: Int,
    )
}

sealed interface CollectedReward {
    data class CountedCurrency(
        val currency: SwipeCurrency,
        val amount: Int,
        val title: String,
        val rarity: Int,
        val description: String,
    ): CollectedReward
}

data class CurrencyMetadata(
    val currency: SwipeCurrency,
    val lore: String,
    val name: String,
    val rarity: Int,
    val description: String,
)

data class CurrenciesMetadata(
    val currencies: List<CurrencyMetadata>
)

class ProfileServiceImpl(
    val levelService: LevelService,
    val monsterService: MonsterService,
) : ProfileService {

    val gson = Gson()
    val handle = Gdx.files.local("data/profile.txt")
    val currencyHandle = Gdx.files.internal("json/currency.json")

    var profile: SwipeProfile
    val currencyCache: CurrenciesMetadata

    init {
        currencyCache = gson.fromJson(currencyHandle.readString(), CurrenciesMetadata::class.java)

        profile = if (handle.exists()) {
            println(handle.file().absolutePath)
            val text = handle.readString()
            gson.fromJson(text, SwipeProfile::class.java)
        } else {
            SwipeProfile(
                balances = emptyList(),
                actProgress = listOf(
                    ActProgress(
                        SwipeAct.ACT_1,
                        listOf("c1")
                    )
                ),
                rewardsCollected = emptyList(),
                characters = listOf(
                    SwipeCharacter(
                        name = "Valerian",
                        skin = "CHARACTER_VALERIAN",
                        attributes = CharacterAttributes(mind = 1, body = 1, spirit = 1),
                        level = SwipeCharacterLevelInfo(0, 1, 1)
                    )
                )
            )
        }
    }

    override suspend fun getProfile(): SwipeProfile = profile

    override suspend fun getAct(act: SwipeAct): FrontActModel {
        val actModel = levelService.getAct(act)
        val progress = profile.actProgress.firstOrNull { it.act == act } ?: return FrontActModel(emptyList(), emptyList())
        val availableLevels = actModel.levels.filter { progress.levelsAvailable.contains(it.id) }
        val availableLinks = actModel.links.filter { progress.levelsAvailable.contains(it.n1) || progress.levelsAvailable.contains(it.n2) }
        val disabledLevels = actModel.levels.filter { l -> !progress.levelsAvailable.contains(l.id) && availableLinks.any { it.n1 == l.id || it.n2 == l.id } }
        return FrontActModel(
            levels = availableLevels.map { l ->
                FrontLevelDetails(
                    x = l.x,
                    y = 1024 - l.y,
                    locationId = l.id,
                    type = l.type,
                    enabled = true,
                    waves = l.monsters?.map { it.mapNotNull { e ->
                        monsterService.getMonster(e.skin)?.let { config ->
                            FrontMonsterEntryModel(config.skin, config.name, e.level)
                        }
                    } } ?: emptyList(),
                    act = act,
                    locationBackground = l.background,
                    locationTitle = l.title,
                    locationDescription = l.description,
                    dialog = l.dialog ?: emptyList())
            } + disabledLevels.map { l ->
                FrontLevelDetails(
                    x = l.x,
                    y = 1024 - l.y,
                    locationId = l.id,
                    type = l.type,
                    enabled = false,
                    waves = l.monsters?.map { it.mapNotNull { e ->
                        monsterService.getMonster(e.skin)?.let { monster ->
                            FrontMonsterEntryModel(monster.skin, monster.name, e.level)
                        }
                    } } ?: emptyList(),
                    act = act,
                    locationBackground = l.background,
                    locationTitle = l.title,
                    locationDescription = l.description,
                    dialog = l.dialog ?: emptyList())
            },
            links = availableLinks
        )
    }

    override suspend fun markActComplete(act: SwipeAct, level: String) {
        val actModel = levelService.getAct(act)
        val actProgress = profile.actProgress.firstOrNull { it.act == act } ?: return
        val levelsToUnlock = actModel.links
            .filter { it.n1 == level || it.n2 == level }
            .flatMap { listOf(it.n1, it.n2) }
            .filter { !actProgress.levelsAvailable.contains(it) }
        profile = profile.copy(actProgress = profile.actProgress.map { pap ->
            if (pap.act == act) {
                pap.copy(levelsAvailable = pap.levelsAvailable + levelsToUnlock)
            } else {
                pap
            }
        })
        saveProfile()
    }

    private fun saveProfile() {
        val text = gson.toJson(profile)
        handle.writeString(text, false)
    }

    override suspend fun isFreeRewardAvailable(act: SwipeAct, level: String): Boolean {
        return profile.rewardsCollected?.firstOrNull { it.act == act && it.level == level } == null
    }

    override suspend fun collectFreeReward(act: SwipeAct, level: String): List<CollectedReward> {
        val result = mutableListOf<CollectedReward>()
        val rewards = levelService.getFreeReward(act, level)
        rewards.forEach { reward ->
            when (reward.type) {
                LevelRewardType.currency -> {
                    val currency = getCurrency(reward.currency!!.type)
                    profile = profile.addBalance(currency.currency, reward.currency.amount)
                    result.add(CollectedReward.CountedCurrency(reward.currency.type, reward.currency.amount, currency.name, currency.rarity, currency.description))
                }
                else -> {}
            }
        }
//        profile = profile.copy(rewardsCollected = (profile.rewardsCollected ?: emptyList()) + ActCollectedReward(act, level))
        saveProfile()
        return result
    }

    override suspend fun getCurrency(currency: SwipeCurrency): CurrencyMetadata {
        return currencyCache.currencies.firstOrNull { it.currency == currency } ?: CurrencyMetadata(currency, "", "", 0, "")
    }

    override suspend fun getCharacters(): List<SwipeCharacter> {
        return profile.characters
    }

    override fun spendExperienceCurrency(currency: SwipeCurrency, skin: String): ProfileService.SpendExperienceCurrencyResult {
        profile.characters.firstOrNull { it.skin == skin }?.let { character ->
            val balance = profile.getBalance(currency)
            if (balance > 0) {
                val newExp = min(character.level.experience + when (currency) {
                    SwipeCurrency.SPARK_OF_INSIGHT -> 1
                    SwipeCurrency.EXPERIENCE_CRYSTAL -> 10
                    SwipeCurrency.EXPERIENCE_RELIC -> 100
                    else -> throw IllegalArgumentException("Invalid experience currency")
                }, character.level.maxExperience)

                val isUpdateLevel = newExp == character.level.maxExperience
                val newLevel = if (isUpdateLevel) {
                    character.level.copy(experience = 0, level = character.level.level + 1, maxExperience = getExperience(character.level.level + 1))
                } else {
                    character.level.copy(experience = newExp)
                }

                profile = profile.addBalance(currency, -1)
                profile = profile.updateLevel(skin, newLevel)
                if (isUpdateLevel) {
                    profile = profile.modifyAttributes(skin, character.attributes.copy(mind = character.attributes.mind + 1,
                        spirit = character.attributes.spirit + 1,
                        body = character.attributes.body + 1))
                }

                saveProfile()

                return ProfileService.SpendExperienceCurrencyResult(profile.characters.first { it.skin == skin }, balance - 1)
            } else {
                return ProfileService.SpendExperienceCurrencyResult(profile.characters.first { it.skin == skin }, balance)
            }
        } ?: throw IllegalArgumentException("Character does not exist")
    }

    companion object {
        fun getExperience(level: Int): Int = level * level
    }
}
