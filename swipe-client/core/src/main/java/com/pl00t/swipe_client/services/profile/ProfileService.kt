package com.pl00t.swipe_client.services.profile

import com.badlogic.gdx.Gdx
import com.game7th.items.InventoryItem
import com.game7th.items.ItemAffix
import com.game7th.swipe.game.CharacterAttributes
import com.google.gson.Gson
import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.game7th.swipe.monsters.MonsterService
import com.pl00t.swipe_client.services.items.ItemService
import com.pl00t.swipe_client.services.levels.*
import java.lang.IllegalArgumentException
import kotlin.math.min
import kotlin.random.Random

interface ProfileService {

    suspend fun getProfile(): SwipeProfile

    suspend fun markActComplete(act: SwipeAct, level: String)

    suspend fun getAct(act: SwipeAct): FrontActModel

    suspend fun isFreeRewardAvailable(act: SwipeAct, level: String): Boolean

    suspend fun collectFreeReward(act: SwipeAct, level: String): List<CollectedReward>

    suspend fun getCurrency(currency: SwipeCurrency): CurrencyMetadata

    suspend fun getCharacters(): List<SwipeCharacter>
    fun spendExperienceCurrency(currency: SwipeCurrency, skin: String): SpendExperienceCurrencyResult

    suspend fun addItem(item: InventoryItem)

    suspend fun getItems(): List<InventoryItem>

    suspend fun equipItem(skin: String, item: InventoryItem)

    suspend fun unequipItem(id: String)

    suspend fun dustItem(id: String): DustItemResult
    suspend fun spendCraftCurrency(id: String, currency: SwipeCurrency)

    data class SpendExperienceCurrencyResult(
        val character: SwipeCharacter,
        val balance: Int,
    )

    data class DustItemResult(
        val rewards: List<CurrencyReward>
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

    data class CollectedItem(
        val skin: String,
        val level: Int,
        val title: String,
        val rarity: Int,
        val lore: String
    ) : CollectedReward
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
    val itemService: ItemService,
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
                ),
                items = emptyList()
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
                LevelRewardType.item -> {
                    val item = itemService.generateItem(reward.skin ?: "", reward.rarity ?: 1)
                    item?.let {
                        addItem(it)
                        val template = itemService.getItemTemplate(it.skin)!!
                        result.add(CollectedReward.CollectedItem(
                            skin = it.skin,
                            level= it.level,
                            title = template.name,
                            rarity = it.rarity,
                            lore = template.lore
                        ))
                    }
                }
                else -> {}
            }
        }
        profile = profile.copy(rewardsCollected = (profile.rewardsCollected ?: emptyList()) + ActCollectedReward(act, level))
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
                    SwipeCurrency.SCROLL_OF_WISDOM -> 1
                    SwipeCurrency.TOME_OF_ENLIGHTMENT -> 10
                    SwipeCurrency.CODEX_OF_ASCENDANCY -> 100
                    SwipeCurrency.GRIMOIRE_OF_OMNISCENCE -> 1000
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

    override suspend fun spendCraftCurrency(id: String, currency: SwipeCurrency) {
        profile.items.firstOrNull { it.id == id }?.let { item ->
            val balance = profile.getBalance(currency)
            if (balance > 0) {
                val exp = when (currency) {
                    SwipeCurrency.INFUSION_ORB -> 1
                    SwipeCurrency.INFUSION_SHARD -> 10
                    SwipeCurrency.INFUSION_CRYSTAL -> 100
                    SwipeCurrency.ASCENDANT_ESSENCE -> 1000
                    else -> 0
                }
                val expAfterIncrease = item.experience + exp
                var newItem = item

                if (expAfterIncrease >= item.level * item.level) {
                    newItem = newItem.copy(level = newItem.level + 1, experience = 0)

                    newItem = newItem.copy(implicit = newItem.implicit.map { implicit ->
                        val affixTemplate = itemService.getAffix(implicit.affix)!!
                        implicit.copy(level = implicit.level + 1, value = (implicit.level + 1) * affixTemplate.valuePerTier)
                    })

                    val maxAffixCount = item.rarity - 1
                    if (maxAffixCount > 0) {
                        val affixIndexToUpgrade = Random.nextInt(maxAffixCount)
                        if (affixIndexToUpgrade >= item.affixes.size) {
                            val affix = itemService.generateAffix(item.affixes.map { it.affix }, itemService.getItemTemplate(item.skin)!!)
                            val affixTemplate = itemService.getAffix(affix)!!
                            newItem = newItem.copy(affixes = item.affixes + ItemAffix(affix, affixTemplate.valuePerTier, 1, true))
                        } else {
                            newItem = newItem.copy(affixes = item.affixes.withIndex().map { (index, affix) ->
                                if (index == affixIndexToUpgrade) {
                                    val affixTemplate = itemService.getAffix(affix.affix)!!
                                    affix.copy(level = affix.level + 1, value = affixTemplate.valuePerTier * (affix.level + 1))
                                } else {
                                    affix
                                }
                            })
                        }
                    }
                } else {
                    newItem = item.copy(experience = expAfterIncrease)
                }
                val updatedItems = profile.items.map { i -> if (i.id == newItem.id) newItem else i }
                profile = profile.copy(items = updatedItems)
                profile = profile.addBalance(currency, -1)
            }
            saveProfile()
        }
    }

    override suspend fun addItem(item: InventoryItem) {
        profile = profile.addItem(item)
        saveProfile()
    }

    override suspend fun getItems(): List<InventoryItem> = profile.items

    override suspend fun equipItem(skin: String, item: InventoryItem) {
        val updatedItems = profile.items.map { itemToUpdate ->
            if (itemToUpdate.id == item.id) {
                itemToUpdate.copy(equippedBy = skin)
            } else if (itemToUpdate.category == item.category && itemToUpdate.equippedBy == skin) {
                itemToUpdate.copy(equippedBy = null)
            } else {
                itemToUpdate
            }
        }
        profile = profile.copy(items = updatedItems)
        saveProfile()
    }

    override suspend fun unequipItem(id: String) {
        val updatedItems = profile.items.map { item ->
            if (item.id == id) {
                item.copy(equippedBy = null)
            } else {
                item
            }
        }
        profile = profile.copy(items = updatedItems)
        saveProfile()
    }

    override suspend fun dustItem(id: String): ProfileService.DustItemResult {
        return profile.items.firstOrNull { it.id == id }?.let { item ->
            val level = item.level
            var exp = 0
            (1 until item.level).forEach { l ->
                exp += l * l
            }
            val minExp = (exp * 0.1f * item.rarity).toInt()
            var expCompensate = Random.nextInt(minExp, exp + 1)
            if (expCompensate <= 0) expCompensate = 1
            val c4 = expCompensate / 1000
            val c3 = (expCompensate % 1000) / 100
            val c2 = (expCompensate % 100) / 10
            val c1 = (expCompensate % 10)

            val result = mutableListOf<CurrencyReward>()
            if (c4 > 0) {
                profile = profile.addBalance(SwipeCurrency.ASCENDANT_ESSENCE, c4)
                result.add(CurrencyReward(SwipeCurrency.ASCENDANT_ESSENCE, c4))
            }
            if (c3 > 0) {
                profile = profile.addBalance(SwipeCurrency.INFUSION_CRYSTAL, c3)
                result.add(CurrencyReward(SwipeCurrency.INFUSION_CRYSTAL, c3))
            }
            if (c2 > 0) {
                profile = profile.addBalance(SwipeCurrency.INFUSION_SHARD, c2)
                result.add(CurrencyReward(SwipeCurrency.INFUSION_SHARD, c2))
            }
            if (c1 > 0) {
                profile = profile.addBalance(SwipeCurrency.INFUSION_ORB, c1)
                result.add(CurrencyReward(SwipeCurrency.INFUSION_ORB, c1))
            }

            profile = profile.copy(items = profile.items.filter { it.id != id })

            saveProfile()

            ProfileService.DustItemResult(result)
        } ?: ProfileService.DustItemResult(emptyList())
    }

    companion object {
        fun getExperience(level: Int): Int = level * level
    }
}
