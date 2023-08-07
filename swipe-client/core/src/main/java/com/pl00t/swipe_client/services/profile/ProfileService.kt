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
import com.pl00t.swipe_client.R
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

    suspend fun markActComplete(act: SwipeAct, level: String)

    suspend fun getAct(act: SwipeAct): FrontActModel

    suspend fun isFreeRewardAvailable(act: SwipeAct, level: String): Boolean

    suspend fun collectFreeReward(act: SwipeAct, level: String, tier: Int): List<CollectedReward>

    suspend fun collectRichReward(act: SwipeAct, level: String, tier: Int): List<CollectedReward>

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
    fun getText(r: R) = if (level > 0) "${UiTexts.LvlShortPrefix.value(r.l)}$level" else amount.toString()
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
    val lore: String,
    val name: SbText,
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
        currencyCache = gson.fromJson(currencyHandle.readString("UTF-8"), CurrenciesMetadata::class.java)

        profile = if (handle.exists()) {
            println(handle.file().absolutePath)
            val text = handle.readString()
            gson.fromJson(text, SwipeProfile::class.java)
        } else {
            SwipeProfile(
                balances = listOf(CurrencyBalance(SwipeCurrency.TOME_OF_ENLIGHTMENT, 10), CurrencyBalance(SwipeCurrency.SCROLL_OF_WISDOM, 100),
                    CurrencyBalance(SwipeCurrency.GRIMOIRE_OF_OMNISCENCE, 100), CurrencyBalance(SwipeCurrency.INFUSION_ORB, 100),
                    CurrencyBalance(SwipeCurrency.INFUSION_SHARD, 100), CurrencyBalance(SwipeCurrency.INFUSION_CRYSTAL, 100),
                    CurrencyBalance(SwipeCurrency.ASCENDANT_ESSENCE, 100), CurrencyBalance(SwipeCurrency.ETHERIUM_COIN, 1400)
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
                    )
                ),
                items = runBlocking {
                    listOf(
                        generateItem("HELM_OF_IRON_WILL", Random.nextInt(5)).copy(equippedBy = "CHARACTER_VALERIAN"),
                        generateItem("HELM_OF_IRON_WILL", Random.nextInt(5)),
                        generateItem("HELM_OF_IRON_WILL", Random.nextInt(5)),
                        generateItem("HELM_OF_IRON_WILL", Random.nextInt(5)),
                        generateItem("RING_OF_ILLUMINATION", Random.nextInt(5)).copy(equippedBy = "CHARACTER_VALERIAN"),
                        generateItem("RING_OF_ILLUMINATION", Random.nextInt(5)),
                        generateItem("RING_OF_ILLUMINATION", Random.nextInt(5)),
                        generateItem("RING_OF_ILLUMINATION", Random.nextInt(5)),
                        generateItem("IRONCLAD_GAUNTLETS", Random.nextInt(5)),
                        generateItem("IRONCLAD_GAUNTLETS", Random.nextInt(5)),
                        generateItem("IRONCLAD_GAUNTLETS", Random.nextInt(5)),
                        generateItem("IRONCLAD_GAUNTLETS", Random.nextInt(5)),
                        generateItem("IRONCLAD_GAUNTLETS", Random.nextInt(5)),
                    )
                },
                tiersUnlocked = emptyList(),
                mysteryShop = null
            )
        }
    }

    override suspend fun getProfile(): SwipeProfile = profile

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

            val attributes = character.attributes.copy(
                body = character.attributes.body + attrBody,
                spirit = character.attributes.spirit + attrSpirit,
                mind = character.attributes.mind + attrMind,
            )

            val abilities = when (skin) {
                MonsterService.CHARACTER_VALERIAN -> provideValerianAbilities(configFile.balance, attributes)

                else -> throw IllegalArgumentException("Can't create monster $skin")
            }

            val health = (hpFlat + configFile.balance.intAttribute("base_health") * (1f + 0.01f * hpPercent + 0.1f * attributes.body)).toInt()
            val ult = (configFile.balance.intAttribute("ult") * (1f + 0.05f * attributes.mind)).toInt()
            val luck = (configFile.balance.intAttribute("luck") * (1f + 0.1f * attributes.spirit))

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
                ultMax = configFile.balance.intAttribute("ult_max")
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

    override suspend fun collectFreeReward(act: SwipeAct, level: String, tier: Int): List<CollectedReward> = emptyList()

    override suspend fun collectRichReward(act: SwipeAct, level: String, tier: Int): List<CollectedReward> = emptyList()

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

    override suspend fun generateItem(skin: String, rarity: Int): InventoryItem {
        val template = itemService.getItemTemplate(skin)!!

        val implicitLevel = rarity + 2
        val implicitAffix = itemService.getAffix(template.implicit)!!

        val affixesList = mutableListOf<ItemAffixType>()
        (0 until rarity).forEach { i ->
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
            maxExperience = SwipeCharacter.experience[max(0, rarity * 5 - 1)]
        )
    }

    companion object {
        fun getExperience(level: Int): Int = level * level
    }
}
