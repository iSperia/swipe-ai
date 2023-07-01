package com.pl00t.swipe_client.services.levels

import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency

interface LevelService {

    suspend fun getAct(act: SwipeAct): ActModel

    suspend fun getLevelDetails(act: SwipeAct, level: String): FrontLevelDetails

    suspend fun getFreeReward(act: SwipeAct, level: String): List<LevelReward>

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

enum class LevelRewardType {
    currency, item
}

data class CurrencyReward(
    val type: SwipeCurrency,
    val amount: Int,
)

data class LevelReward(
    val type: LevelRewardType,
    val currency: CurrencyReward?,
)
