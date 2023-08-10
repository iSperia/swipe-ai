package com.pl00t.swipe_client.services.profile

import com.badlogic.gdx.Gdx
import com.game7th.items.InventoryItem
import com.game7th.items.ItemAffix
import com.game7th.items.ItemAffixType
import com.game7th.swipe.SbText
import com.game7th.swipe.game.*
import com.game7th.swipe.game.characters.*
import com.google.gson.Gson
import com.game7th.swipe.monsters.MonsterService
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.services.items.ItemService
import com.pl00t.swipe_client.services.levels.*
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

interface ProfileService {

    suspend fun getProfile(): SwipeProfile

    suspend fun createCharacter(skin: String): FrontMonsterConfiguration

    suspend fun createActiveCharacter(): FrontMonsterConfiguration

    suspend fun markActComplete(act: SwipeAct, level: String)

    suspend fun getAct(act: SwipeAct): FrontActModel

    suspend fun isFreeRewardAvailable(act: SwipeAct, level: String): Boolean

    suspend fun collectFreeReward(act: SwipeAct, level: String, tier: Int): List<FrontItemEntryModel>

    suspend fun collectFreeRaidReward(expBoost: Int): List<FrontItemEntryModel>

    suspend fun collectRichReward(act: SwipeAct, level: String, tier: Int, cost: Int): List<FrontItemEntryModel>

    suspend fun getCurrency(currency: SwipeCurrency): CurrencyMetadata

    suspend fun getCharacters(): List<SwipeCharacter>

    suspend fun addItem(item: InventoryItem)

    suspend fun getItems(): List<InventoryItem>

    suspend fun equipItem(skin: String, item: InventoryItem)

    suspend fun unequipItem(id: String)

    suspend fun getTierUnlocked(act: SwipeAct, level: String): Int

    suspend fun unlockTier(act: SwipeAct, level: String, tier: Int)

    suspend fun getMysteryShop(): List<SbMysteryItem>

    suspend fun rerollMysteryShop()

    suspend fun buyMysteryItem(id: String): List<CollectedReward>
    fun spendCurrency(currencies: Array<SwipeCurrency>, useCount: Array<Int>)

    fun addCharacterExperience(skin: String, boostExp: Int)
    suspend fun addItemExperience(id: String, boostExp: Int)

    suspend fun generateItem(skin: String, rarity: Int): InventoryItem

    suspend fun previewDust(id: String): List<CurrencyBalance>
    suspend fun dustItem(id: String)
    suspend fun getRaidDetails(act: SwipeAct, level: String): FrontRaidModel
    suspend fun getBossDetails(act: SwipeAct, level: String): FrontBossModel

    suspend fun getActiveCharacter(): String

    suspend fun setActiveCharacter(skin: String)
    suspend fun useElixir(skin: String, currency: SwipeCurrency): Boolean


    data class DustItemResult(
        val rewards: List<CurrencyReward>
    )
}

data class FrontItemEntryModel(
    val skin: String,
    val amount: Int,
    val level: Int,
    val rarity: Int,
    val name: SbText,
    val currency: SwipeCurrency?,
    val item: InventoryItem?,
) {
    fun getText(r: Resources) = if (level > 0) "${UiTexts.LvlShortPrefix.value(r.l)}$level" else amount.toString()
}

data class SbMysteryItem(
    val id: String,
    val currency: SwipeCurrency?,
    val item: String?,
    val rarity: Int,
    val price: Int,
    val title: String,
)

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
    val name: SbText,
    val rarity: Int,
    val description: SbText,
)

data class CurrenciesMetadata(
    val currencies: List<CurrencyMetadata>
)

val DEBUG = true

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
        currencyCache = gson.fromJson(currencyHandle.readString("UTF-8"), CurrenciesMetadata::class.java)

        profile = if (handle.exists()) {
            println(handle.file().absolutePath)
            val text = handle.readString()
            gson.fromJson(text, SwipeProfile::class.java)
        } else {
            if (DEBUG) {
                SwipeProfile(
                    balances = listOf(CurrencyBalance(SwipeCurrency.TOME_OF_ENLIGHTMENT, 10), CurrencyBalance(SwipeCurrency.SCROLL_OF_WISDOM, 100),
                        CurrencyBalance(SwipeCurrency.GRIMOIRE_OF_OMNISCENCE, 100), CurrencyBalance(SwipeCurrency.INFUSION_ORB, 100),
                        CurrencyBalance(SwipeCurrency.INFUSION_SHARD, 100), CurrencyBalance(SwipeCurrency.INFUSION_CRYSTAL, 100),
                        CurrencyBalance(SwipeCurrency.ASCENDANT_ESSENCE, 100), CurrencyBalance(SwipeCurrency.ETHERIUM_COIN, 1400)
                    ),
                    actProgress = listOf(
                        ActProgress(
                            SwipeAct.ACT_1,
                            listOf("c1", "c2", "c3", "c4", "c5", "c6", "c7","c8","c9","c10","c11","c12")
                        ),
                    ),
                    rewardsCollected = emptyList(),
                    characters = listOf(
                        SwipeCharacter(
                            skin = "CHARACTER_VALERIAN",
                            attributes = CharacterAttributes(mind = 1, body = 1, spirit = 1),
                            experience = 0,
                        ),
                        SwipeCharacter(
                            skin = "CHARACTER_SAFFRON",
                            attributes = CharacterAttributes(mind = 1, body = 1, spirit = 1),
                            experience = 0
                        )
                    ),
                    items = runBlocking {
                        listOf(
                            generateItem("HELM_OF_IRON_WILL", 4),
                            generateItem("RING_OF_ILLUMINATION", 4),
                            generateItem("FROSTGUARD_GAUNTLETS", 4),
                            generateItem("FLAMEHEART_BELT", 4),
                            generateItem("ENCHANTED_BAND", 4),
                            generateItem("SHADOWBANE_AMULET", 4),
                            generateItem("ENIGMA_HELM", 4),
                            generateItem("CRYSTALWEAVE_LUCK", 4),
                            generateItem("ENCHANTED_BAND", 4),
                            generateItem("VERDANT_HEART", 4),
                            generateItem("RADIANT_EMBRACE", 4),
                            generateItem("HARMONYS_ECHO", 4),
                        )
                    },
                    tiersUnlocked = emptyList(),
                    mysteryShop = null,
                    activeCharacter = "CHARACTER_VALERIAN",
                )
            } else {
                SwipeProfile(
                    balances = listOf(
                        CurrencyBalance(SwipeCurrency.ETHERIUM_COIN, 1000)
                    ),
                    actProgress = listOf(
                        ActProgress(
                            SwipeAct.ACT_1,
                            listOf("c1")
                        )
                    ),
                    rewardsCollected = emptyList(),
                    characters = listOf(
                        SwipeCharacter(
                            skin = "CHARACTER_VALERIAN",
                            attributes = CharacterAttributes(mind = 1, body = 1, spirit = 1),
                            experience = 0,
                        ),
                        SwipeCharacter(
                            skin = "CHARACTER_SAFFRON",
                            attributes = CharacterAttributes(mind = 1, body = 1, spirit = 1),
                            experience = 0
                        )
                    ),
                    items = emptyList(),
                    tiersUnlocked = emptyList(),
                    mysteryShop = null,
                    activeCharacter = "CHARACTER_VALERIAN"
                )
            }
        }
    }

    override suspend fun getProfile(): SwipeProfile = profile

    override suspend fun createActiveCharacter() = createCharacter(getActiveCharacter())

    override suspend fun createCharacter(skin: String): FrontMonsterConfiguration {
        profile.characters.firstOrNull { it.skin == skin }?.let { character ->
            val configFile = monsterService.getMonster(skin) ?: throw IllegalArgumentException("Did not find $skin monster")

            val affixes = profile.items.filter { it.equippedBy == character.skin }.flatMap { it.affixes + it.implicit }

            val hpPercent = affixes.sumOf { if (it.affix == ItemAffixType.PERCENT_HP) it.value.toDouble() else 0.0 }.toFloat()
            val hpFlat = affixes.sumOf { if (it.affix == ItemAffixType.FLAT_HP) it.value.toDouble() else 0.0 }.toFloat()
            val dmgPhys = affixes.sumOf { if (it.affix == ItemAffixType.PHYS_DAMAGE_INCREASE) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val dmgCold = affixes.sumOf { if (it.affix == ItemAffixType.COLD_DAMAGE_INCREASE) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val dmgFire = affixes.sumOf { if (it.affix == ItemAffixType.FIRE_DAMAGE_INCREASE) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val dmgDark = affixes.sumOf { if (it.affix == ItemAffixType.DARK_DAMAGE_INCREASE) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val dmgLight = affixes.sumOf { if (it.affix == ItemAffixType.LIGHT_DAMAGE_INCREASE) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val dmgShock = affixes.sumOf { if (it.affix == ItemAffixType.SHOCK_DAMAGE_INCREASE) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val resPhys = affixes.sumOf { if (it.affix == ItemAffixType.PHYS_RESIST_FLAT) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val resCold = affixes.sumOf { if (it.affix == ItemAffixType.COLD_RESIST_FLAT) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val resFire = affixes.sumOf { if (it.affix == ItemAffixType.FIRE_RESIST_FLAT) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val resDark = affixes.sumOf { if (it.affix == ItemAffixType.DARK_RESIST_FLAT) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val resLight = affixes.sumOf { if (it.affix == ItemAffixType.LIGHT_RESIST_FLAT) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val resShock = affixes.sumOf { if (it.affix == ItemAffixType.SHOCK_RESIST_FLAT) it.value.toDouble() else 0.0 }.toFloat() / 100f
            val attrBody = affixes.sumOf { if (it.affix == ItemAffixType.FLAT_BODY) it.value.toDouble() else 0.0 }.toInt()
            val attrSpirit = affixes.sumOf { if (it.affix == ItemAffixType.FLAT_SPIRIT) it.value.toDouble() else 0.0 }.toInt()
            val attrMind = affixes.sumOf { if (it.affix == ItemAffixType.FLAT_MIND) it.value.toDouble() else 0.0 }.toInt()
            val luckFlat = affixes.sumOf { if (it.affix == ItemAffixType.LUCK_FLAT) it.value.toDouble() else 0.0 }.toFloat()
            val ultFlat = affixes.sumOf { if (it.affix == ItemAffixType.ULT_FLAT) it.value.toDouble() else 0.0 }.toFloat()
            val ultPrefillPercent = affixes.sumOf { if (it.affix == ItemAffixType.ULT_PREFILL) it.value.toDouble() else 0.0 }.toFloat()

            val attributes = character.attributes.copy(
                body = character.attributes.body + attrBody,
                spirit = character.attributes.spirit + attrSpirit,
                mind = character.attributes.mind + attrMind,
            )

            val abilities = when (skin) {
                MonsterService.CHARACTER_VALERIAN -> provideValerianAbilities(configFile.balance, attributes)
                MonsterService.CHARACTER_SAFFRON -> provideSaffronAbilities(configFile.balance, attributes)

                else -> throw IllegalArgumentException("Can't create monster $skin")
            }

            val health = (hpFlat + configFile.balance.intAttribute("base_health") * (1f + 0.01f * hpPercent + 0.1f * attributes.body)).toInt()
            val ult = ((configFile.balance.intAttribute("ult") + ultFlat) * (1f + 0.05f * attributes.mind)).toInt()
            val luck = ((configFile.balance.intAttribute("luck") + luckFlat) * (1f + 0.1f * attributes.spirit))

            return FrontMonsterConfiguration(
                skin = skin,
                name = configFile.name,
                level = SwipeCharacter.getLevel(character.experience),
                attributes = attributes,
                resist = configFile.balance.getAsJsonObject("resist").let { r ->
                    SbElemental(
                        phys = r.floatAttribute("phys") + resPhys,
                        dark = r.floatAttribute("dark") + resDark,
                        light = r.floatAttribute("light") + resLight,
                        shock = r.floatAttribute("shock") + resShock,
                        fire = r.floatAttribute("fire") + resFire,
                        cold = r.floatAttribute("cold") + resCold,
                    )
                },
                damage = SbElemental(
                    phys = dmgPhys,
                    dark = dmgDark,
                    light = dmgLight,
                    shock = dmgShock,
                    fire = dmgFire,
                    cold = dmgCold
                ),
                abilities = abilities,
                lore = configFile.lore,
                health = health,
                luck = luck,
                ult = ult,
                ultMax = configFile.balance.intAttribute("ult_max"),
                ultPrefillPercent = ultPrefillPercent.toInt()
            )

        } ?: throw IllegalArgumentException("No hero $skin")
    }

    override suspend fun getAct(act: SwipeAct): FrontActModel {
        val actModel = levelService.getAct(act)
        val progress = profile.actProgress.firstOrNull { it.act == act } ?: throw IllegalArgumentException("No act $act found")
        val availableLevels = actModel.levels.filter { progress.levelsAvailable.contains(it.id) }
        val availableLinks = actModel.links.filter { progress.levelsAvailable.contains(it.n1) || progress.levelsAvailable.contains(it.n2) }
        val disabledLevels = actModel.levels.filter { l -> !progress.levelsAvailable.contains(l.id) && availableLinks.any { it.n1 == l.id || it.n2 == l.id } }
        return FrontActModel(
            title = actModel.title,
            levels = availableLevels.map { l ->
                levelService.getLevelDetails(act, l.id, true)
            } + disabledLevels.map { l ->
                levelService.getLevelDetails(act, l.id, false)
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

    override suspend fun collectFreeReward(act: SwipeAct, level: String, tier: Int): List<FrontItemEntryModel> {
        val result = mutableListOf<FrontItemEntryModel>()
        levelService.getFreeReward(act, level).forEach { reward ->
            if (reward.currency != null) {
                val currency = reward.currency.type
                val amount = reward.currency.amount
                profile = profile.addBalance(currency, amount)
                val meta = getCurrency(currency)
                result.add(FrontItemEntryModel(
                    skin = meta.currency.toString(),
                    amount = amount,
                    level = 0,
                    rarity = meta.rarity,
                    name = meta.name,
                    currency = currency,
                    item = null
                ))
            } else if (reward.skin != null) {
                val item = generateItem(reward.skin, reward.rarity ?: 0)
                addItem(item)
                val template = itemService.getItemTemplate(item.skin)!!
                result.add(FrontItemEntryModel(
                    skin = item.skin,
                    amount = 1,
                    level = SwipeCharacter.getLevel(item.experience),
                    rarity = item.rarity,
                    name = template.name,
                    currency = null,
                    item = item
                ))
            }
        }
        profile = profile.copy(rewardsCollected = (profile.rewardsCollected?: emptyList()) + ActCollectedReward(act, level))
        saveProfile()
        return result
    }

    override suspend fun collectFreeRaidReward(expBoost: Int): List<FrontItemEntryModel> {
        val minValue = expBoost
        val maxValue = expBoost * 3
        val roll = Random.nextInt(minValue, maxValue)
        profile = profile.addBalance(SwipeCurrency.ETHERIUM_COIN, roll)
        val meta = getCurrency(SwipeCurrency.ETHERIUM_COIN)
        return listOf(FrontItemEntryModel(SwipeCurrency.ETHERIUM_COIN.toString(), roll, 0, meta.rarity, meta.name, meta.currency, null))
    }

    override suspend fun collectRichReward(act: SwipeAct, level: String, tier: Int, cost: Int): List<FrontItemEntryModel> {
        if (profile.getBalance(SwipeCurrency.ETHERIUM_COIN) < cost) {
            return emptyList()
        }
        profile = profile.addBalance(SwipeCurrency.ETHERIUM_COIN, -cost)

        var result = mutableListOf<FrontItemEntryModel>()

        val levelModel = levelService.getAct(act).levels.first { it.id == level }
        val pool = levelModel.tiers!![tier].rewards
        val totalWeight = pool.sumOf { it.weight }
        var stuffLeft = cost
        while (stuffLeft > 0) {
            val roll = Random.nextInt(totalWeight)
            var sum = 0
            val reward = pool.first { sum += it.weight; sum >= roll }
            if (reward.currency != null) {
                val currency = reward.currency.type
                val amount = reward.currency.amount
                profile = profile.addBalance(currency, amount)
                val meta = getCurrency(currency)
                result.add(FrontItemEntryModel(
                    skin = meta.currency.toString(),
                    amount = amount,
                    level = 0,
                    rarity = meta.rarity,
                    name = meta.name,
                    currency = currency,
                    item = null
                ))
                stuffLeft -= reward.currency.type.coins
            } else if (reward.skin != null) {
                val item = generateItem(reward.skin, reward.rarity ?: 0)
                addItem(item)
                val template = itemService.getItemTemplate(item.skin)!!
                result.add(FrontItemEntryModel(
                    skin = item.skin,
                    amount = 1,
                    level = SwipeCharacter.getLevel(item.experience),
                    rarity = item.rarity,
                    name = template.name,
                    currency = null,
                    item = item
                ))
                stuffLeft -= ITEM_COST[item.rarity]
            }
        }
        val currencies = SwipeCurrency.values().map { c -> c to result.sumOf { if (it.currency == c) it.amount else 0 } }
        result = (result.filter { it.currency == null } + currencies.mapNotNull {
            if (it.second <= 0) {
                null
            } else {
                val meta = getCurrency(it.first)
                FrontItemEntryModel(
                    skin = meta.currency.toString(),
                    amount = it.second,
                    level = 0,
                    rarity = meta.rarity,
                    name = meta.name,
                    currency = it.first,
                    item = null
                )
            }
        }).toMutableList()

        saveProfile()
        return result
    }

    private val ITEM_COST = arrayOf(500, 1500, 5000, 15000, 50000)

    override suspend fun getCurrency(currency: SwipeCurrency): CurrencyMetadata {
        return currencyCache.currencies.firstOrNull { it.currency == currency } ?: throw IllegalArgumentException("No currency $currency")
    }

    override suspend fun getCharacters(): List<SwipeCharacter> {
        return profile.characters
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

    override suspend fun getTierUnlocked(act: SwipeAct, level: String): Int {
        return profile.tiersUnlocked?.firstOrNull { it.act == act && it.level == level }?.let {
            it.tier
        } ?: 0
    }

    override suspend fun unlockTier(act: SwipeAct, level: String, tier: Int) {
        val nowTiers = profile.tiersUnlocked ?: emptyList()
        val tiersUpdated = if (nowTiers.none { it.act == act && it.level == level }) {
            nowTiers + LevelTierUnlocked(tier, level, act)
        } else nowTiers.map {
            if (it.act == act && it.level == level && it.tier < tier) {
                it.copy(tier = tier)
            } else {
                it
            }
        }
        profile = profile.copy(tiersUnlocked = tiersUpdated)
        saveProfile()
    }

    override suspend fun getMysteryShop(): List<SbMysteryItem> {
        if (profile.mysteryShop == null) {
            rerollMysteryShop()
        }
        return profile.mysteryShop ?: emptyList()
    }

    override suspend fun rerollMysteryShop() {

    }

    override suspend fun buyMysteryItem(id: String): List<CollectedReward> = emptyList()

    override fun spendCurrency(currencies: Array<SwipeCurrency>, useCount: Array<Int>) {
        currencies.forEachIndexed { i, c ->
            profile = profile.addBalance(c, -useCount[i])
        }
        saveProfile()
    }

    override suspend fun useElixir(skin: String, currency: SwipeCurrency): Boolean {
        val character = profile.characters.first { it.skin == skin }
        var db = 0
        var ds = 0
        var dm = 0
        when (currency) {
            SwipeCurrency.ELIXIR_AMBER -> { db = 2; ds = -1; dm = -1 }
            SwipeCurrency.ELIXIR_CITRINE -> { db = 1; ds = 1; dm = -2 }
            SwipeCurrency.ELIXIR_AGATE -> { db = 1; ds = -2; dm = 1 }
            SwipeCurrency.ELIXIR_TURQUOISE -> { db = -2; ds = 1; dm = 1 }
            SwipeCurrency.ELIXIR_JADE -> { db = -1; ds = 2; dm = -1 }
            SwipeCurrency.ELIXIR_LAPIS -> { db = -1; ds = -1; dm = 2 }
            else -> {}
        }
        val updatedCharacter = character.copy(attributes = character.attributes.copy(
            body = character.attributes.body + db,
            spirit = character.attributes.spirit + ds,
            mind = character.attributes.mind + dm
        ))
        if (updatedCharacter.attributes.let { it.spirit >= 0 && it.body >= 0 && it.mind >= 0 }) {
            profile = profile.copy(characters = profile.characters.map {
                if (it.skin == updatedCharacter.skin) updatedCharacter else it
            })
            saveProfile()
            return true
        }
        return false
    }

    override fun addCharacterExperience(skin: String, boostExp: Int) {
        profile = profile.copy(characters = profile.characters.map { character ->
            if (character.skin == skin) {
                val oldLevel = SwipeCharacter.getLevel(character.experience)
                val newLevel = SwipeCharacter.getLevel(character.experience + boostExp)
                val attributes = (oldLevel until newLevel).sumOf { l -> 3 + l / 5 }
                val rolls = (0 until attributes).map { Random.nextFloat() }
                val boostBody = rolls.count { it < 0.33f }
                val boostSpirit = rolls.count { it >= 0.33f && it < 0.66f }
                val boostMind = rolls.count { it >= 0.66f }
                character.copy(experience = character.experience + boostExp, attributes = character.attributes.copy(
                    mind = character.attributes.mind + boostMind,
                    spirit = character.attributes.spirit + boostSpirit,
                    body = character.attributes.body + boostBody
                ))
            } else {
                character
            }
        })
        saveProfile()
    }

    override suspend fun addItemExperience(id: String, boostExp: Int) {
        profile = profile.copy(items = profile.items.map { item ->
            if (item.id == id) {
                val oldLevel = SwipeCharacter.getLevel(item.experience)
                val newLevel = SwipeCharacter.getLevel(item.experience + boostExp)
                val boosts = item.affixes.map { 0 }.toIntArray()
                if (boosts.isNotEmpty()) {
                    (0 until (newLevel - oldLevel)).forEach { i ->
                        boosts[boosts.indices.random()]++
                    }
                }
                val newAffixes = item.affixes.mapIndexed { i, affix ->
                    val newLevel = affix.level + boosts[i]
                    val meta = itemService.getAffix(affix.affix)!!
                    val newValue = (affix.level + boosts[i]) * meta.valuePerTier

                    affix.copy(level = newLevel, value = newValue)
                }

                item.copy(affixes = newAffixes, experience = min(item.experience + boostExp, item.maxExperience))
            } else {
                item
            }
        })
        saveProfile()
    }

    override suspend fun previewDust(id: String): List<CurrencyBalance> {
        return profile.items.firstOrNull { it.id == id }?.let { item ->
            val currencies = arrayOf(SwipeCurrency.INFUSION_ORB, SwipeCurrency.INFUSION_SHARD, SwipeCurrency.INFUSION_CRYSTAL, SwipeCurrency.ASCENDANT_ESSENCE)
            val count = arrayOf(0, 0, 0, 0)
            count[min(3, item.rarity)]++
            if (item.rarity == 4) count[3] += 2
            var experienceCompensation = (0.8f * item.experience).toInt()
            currencies.indices.reversed().forEach { i ->
                val amount = experienceCompensation / currencies[i].expBonus
                count[i] += amount
                experienceCompensation -= amount * currencies[i].expBonus
            }
            currencies.indices.map { CurrencyBalance(currencies[it], count[it]) }
        } ?: emptyList()
    }

    override suspend fun dustItem(id: String) {
        previewDust(id).forEach { entry ->
            profile = profile.addBalance(entry.currency, entry.amount)
        }
        profile = profile.copy(items = profile.items.filter { it.id != id })
        saveProfile()
    }

    override suspend fun getRaidDetails(act: SwipeAct, level: String): FrontRaidModel {
        return levelService.getRaidDetails(act, level)
    }

    override suspend fun getBossDetails(act: SwipeAct, level: String): FrontBossModel {
        return levelService.getBossDetails(act, level)
    }

    override suspend fun getActiveCharacter(): String {
        return profile.activeCharacter ?: profile.characters.first().skin
    }

    override suspend fun setActiveCharacter(skin: String) {
        profile = profile.copy(activeCharacter = skin)
        saveProfile()
    }

    override suspend fun generateItem(skin: String, rarity: Int): InventoryItem {
        val template = itemService.getItemTemplate(skin)!!

        val implicitLevel = rarity + 2
        val implicitAffix = itemService.getAffix(template.implicit)!!

        val affixesList = mutableListOf<ItemAffixType>()
        (0 until min(4, rarity + 1)).forEach { i ->
            val affix = itemService.generateAffix(affixesList, template)
            affixesList.add(affix)
        }
        val affixesMapped = affixesList.map {
            val meta = itemService.getAffix(it)!!
            ItemAffix(
                affix = it,
                value = meta.valuePerTier,
                level = 1,
                scalable = true
            )
        }

        return InventoryItem(
            id = UUID.randomUUID().toString(),
            skin = skin,
            implicit = ItemAffix(
                affix = implicitAffix.affix,
                value = implicitLevel * implicitAffix.valuePerTier,
                level = implicitLevel,
                true
            ),
            affixes = affixesMapped,
            experience = 0,
            rarity = rarity,
            category = template.category,
            equippedBy = null,
            maxExperience = SwipeCharacter.experience[max(0, (rarity + 1) * 5-1)]
        )
    }

    companion object {
        fun getExperience(level: Int): Int = level * level
    }
}
