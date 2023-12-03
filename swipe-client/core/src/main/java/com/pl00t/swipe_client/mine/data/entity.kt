package com.pl00t.swipe_client.mine.data

import com.game7th.items.ItemAffixType
import com.game7th.swipe.SbText

data class MineItemConfig(
    val skin: String,
    val affix: ItemAffixType,
    val name: SbText,
    val description: SbText,
)

data class MineItem(
    val skin: String,
    val tier: Int,
)

data class MineConfigurationFile(
    val stones: List<MineItemConfig>,
    val attempts: List<Int>,
    val maxTiers: List<Int>,
    val upgradeCosts: List<Int>,
    val maxLevel: Int,
)

data class MineProgressFile(
    val items: List<MineItem>,
    val level: Int,
)
