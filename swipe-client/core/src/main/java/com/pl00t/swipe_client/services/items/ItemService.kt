package com.pl00t.swipe_client.services.items

import com.game7th.items.*
import com.google.gson.Gson
import com.pl00t.swipe_client.services.files.FileService
import java.util.UUID
import kotlin.random.Random

interface ItemService {

    suspend fun getItemTemplate(skin: String): ItemTemplate?

    suspend fun generateItem(skin: String, rarity: Int): InventoryItem?

    suspend fun generateAffix(affixesFilled: List<ItemAffixType>, template: ItemTemplate): ItemAffixType

    suspend fun getAffix(affix: ItemAffixType): AffixConfigEntry?
}

data class ItemTemplatesFile(
    val items: List<ItemTemplate>
)

data class AffixWeightConfig(
    val weight: Int,
    val category: ItemCategory?
)

data class AffixConfigEntry(
    val affix: ItemAffixType,
    val valuePerTier: Float,
    val weights: List<AffixWeightConfig>?,
    val scalable: Boolean? = false,
    val description: String
)

data class AffixesFile(
    val affixes: List<AffixConfigEntry>
)

class ItemServiceImpl(gson: Gson, fileService: FileService): ItemService {

    private val templates: MutableMap<String, ItemTemplate> = mutableMapOf()
    private val affixes: MutableMap<ItemAffixType, AffixConfigEntry> = mutableMapOf()

    init {
        val file = gson.fromJson(fileService.internalFile("json/items.json"), ItemTemplatesFile::class.java)
        file.items.forEach { template ->
            templates[template.skin] = template
        }
        val affixFile = gson.fromJson(fileService.internalFile("json/affixes.json"), AffixesFile::class.java)
        affixFile.affixes.forEach { affix ->
            affixes[affix.affix] = affix
        }
    }

    override suspend fun getItemTemplate(skin: String): ItemTemplate? = templates[skin]

    private fun generateAffixWeights(template: ItemTemplate): Pair<Map<ItemAffixType, Int>, Int> {
        val weightMap = mutableMapOf<ItemAffixType, Int>()
        var totalWeight = 0
        this.affixes.forEach { affix, entry ->
            var weight = 0
            if (entry.weights != null) {
                weight += entry.weights.sumOf { awc ->
                    if (awc.category != null) {
                        if (awc.category == template.category) {
                            awc.weight
                        } else {
                            0
                        }
                    } else {
                        awc.weight
                    }
                }
            }
            if (weight > 0) {
                weightMap[affix] = weight
                totalWeight += weight
            }
        }
        return weightMap to totalWeight
    }

    override suspend fun generateAffix(affixesFilled: List<ItemAffixType>, template: ItemTemplate): ItemAffixType {
        val weights = generateAffixWeights(template)
        var entry: ItemAffixType? = null
        while (entry == null || affixesFilled.contains(entry)) {
            val random = Random.nextInt(weights.second)
            var s = 0
            entry = weights.first.entries.first {
                s += it.value
                s >= random
            }.key
        }
        return entry
    }

    override suspend fun generateItem(skin: String, rarity: Int): InventoryItem? {

        val affixesToGenerate = if (Random.nextFloat() < 0.5f) rarity - 1 else rarity - 2
        val template = templates[skin] ?: return null


        val affixes = mutableListOf<ItemAffix>()

        val affixesFilled = mutableListOf<ItemAffixType>()
        (0 until affixesToGenerate).forEach { _ ->
            generateAffix(affixesFilled, template)?.let { entry ->
                affixesFilled.add(entry)
                this.affixes[entry]?.let { ace ->
                    affixes.add(ItemAffix(
                        affix = ace.affix,
                        value = ace.valuePerTier,
                        level = 1,
                        scalable = true
                    ))
                }
            }
        }

        return getItemTemplate(skin)?.let { template ->
            InventoryItem(
                id = UUID.randomUUID().toString(),
                skin = skin,
                implicit = template.implicit.mapNotNull { affixConfig ->
                    this.affixes[affixConfig]?.let {ace ->
                        ItemAffix(
                            affix = ace.affix,
                            value = ace.valuePerTier,
                            level = 1,
                            scalable = ace.scalable ?: true
                        )
                    }
                },
                affixes = affixes,
                level = 1,
                maxLevel = rarity * 5,
                rarity = rarity,
                category = template.category,
                equippedBy = null,
                experience = 0
            )
        }
    }

    override suspend fun getAffix(affix: ItemAffixType): AffixConfigEntry? = affixes[affix]
}
