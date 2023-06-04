package com.pl00t.swipe_client.services

import com.pl00t.swipe.model.LevelType
import com.pl00t.swipe.model.LinkModel

interface LevelService {

    suspend fun getAct(actName: String): FrontActModel

    suspend fun getLevelDetails(level: String): FrontLevelDetails
}

data class FrontActModel(
    val levels: List<FrontLevelModel>,
    val links: List<LinkModel>,
    val background: String
)
data class FrontLevelModel(
    val x: Float,
    val y: Float,
    val id: String,
    val type: LevelType,
    val enabled: Boolean,
)
data class FrontLevelDetails(
    val locationId: String,
    val locationTitle: String,
)
