package com.pl00t.swipe_client.services.levels

import com.game7th.swipe.SbText
import com.game7th.swipe.game.SbMonsterEntry
import com.pl00t.swipe_client.services.profile.SwipeAct

data class ActModel(
    val levels: List<LevelModel>,
    val links: List<LinkModel>,
    val title: SbText,
    val lore: SbText,
)

enum class LevelType {
    CAMPAIGN, RAID, BOSS, ZEPHYR_SHOP
}

data class LinkModel(
    val n1: String,
    val n2: String
)

enum class DialogOrientation {
    left, right
}

data class DialogEntryModel(
    val skin: String,
    val text: SbText,
    val side: DialogOrientation,
)

data class DialogScript(
    val replicas: List<DialogEntryModel>
)

data class SbMonsterPoolEntry(
    val skin: String,
    val weight: Int,
    val level: Int,
    val rarity: Int,
)

data class LevelModel(
    val id: String,
    val type: LevelType,
    val x: Float,
    val y: Float,
    val title: SbText,
    val description: SbText,
    val background: String,
    val music: String?,
    val monsters: List<List<SbMonsterEntry>>?,
    val dialog: List<DialogEntryModel>?,
    val freeReward: List<LevelReward>?,
    val tiers: List<RaidTierModel>?,
    val unlock_act: SwipeAct?
)
