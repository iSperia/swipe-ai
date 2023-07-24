package com.pl00t.swipe_client.services.items

import com.game7th.items.*
import com.google.gson.Gson
import com.pl00t.swipe_client.services.files.FileService
import kotlin.random.Random

interface ItemService {

    suspend fun getItemTemplate(skin: String): ItemTemplate?

    suspend fun generateItem(skin: String, rarity: Int): InventoryItem?

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
    val scalable: Boolean? = false
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

    override suspend fun generateItem(skin: String, rarity: Int): InventoryItem? {

        val affixesToGenerate = if (Random.nextFloat() < 0.5f) rarity - 1 else rarity - 2
        val template = templates[skin] ?: return null

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

        val affixes = mutableListOf<ItemAffix>()

        (0 until affixesToGenerate).forEach { _ ->
            val random = Random.nextInt(totalWeight)
            var s = 0
            val entry = weightMap.entries.first {
                s += it.value
                s >= random
            }
            this.affixes[entry.key]?.let { ace ->
                affixes.add(ItemAffix(
                    affix = ace.affix,
                    value = ace.valuePerTier,
                    level = 1,
                    scalable = true
                ))
            }
        }

        return getItemTemplate(skin)?.let { template ->
            InventoryItem(
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
                rarity = rarity,
                category = template.category
            )
        }
    }

    override suspend fun getAffix(affix: ItemAffixType): AffixConfigEntry? = affixes[affix]
}
