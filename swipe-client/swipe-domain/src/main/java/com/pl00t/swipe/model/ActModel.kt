package com.pl00t.swipe.model

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

data class LevelModel(
    val id: String,
    val type: LevelType,
    val x: Float,
    val y: Float,
)
