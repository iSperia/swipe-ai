package com.pl00t.swipe_client.services.levels

import com.game7th.swipe.game.SbMonsterEntry
import com.game7th.swipe.game.UnitSkin

data class ActModel(
    val levels: List<LevelModel>,
    val links: List<LinkModel>,
    val background: String,
)

enum class LevelType {
    CAMPAIGN, RAID, BOSS
}

data class LinkModel(
    val n1: String,
    val n2: String
)

data class MonsterEntryModel(
    val skin: UnitSkin,
    val level: Int,
)

enum class DialogOrientation {
    left, right
}

data class DialogEntryModel(
    val actor: UnitSkin,
    val side: DialogOrientation,
    val text: String,
    val title: String,
)

data class SbMonsterPoolEntry(
    val skin: String,
    val weight: Int,
    val value: Float,
)

data class LevelModel(
    val id: String,
    val type: LevelType,
    val x: Float,
    val y: Float,
    val title: String,
    val description: String,
    val background: String,
    val monsters: List<List<SbMonsterEntry>>?,
    val monster_pool: List<SbMonsterPoolEntry>?,
    val dialog: List<DialogEntryModel>?,
    val freeReward: List<LevelReward>?,
)
