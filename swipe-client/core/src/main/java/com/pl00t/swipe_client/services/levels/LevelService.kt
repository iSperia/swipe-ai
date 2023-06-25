package com.pl00t.swipe_client.services.levels

import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.pl00t.swipe_client.services.profile.SwipeAct

interface LevelService {

    suspend fun getAct(act: SwipeAct): ActModel

    suspend fun getLevelDetails(act: SwipeAct, level: String): FrontLevelDetails
}

data class FrontActModel(
    val levels: List<FrontLevelModel>,
    val links: List<LinkModel>
)
data class FrontLevelModel(
    val x: Float,
    val y: Float,
    val id: String,
    val type: LevelType,
    val enabled: Boolean,
    val waves: List<List<FrontMonsterEntryModel>>
)
data class FrontLevelDetails(
    val locationId: String,
    val locationBackground: String,
    val locationTitle: String,
    val locationDescription: String,
    val dialog: List<DialogEntryModel>,
)
