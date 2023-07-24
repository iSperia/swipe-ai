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
    val levels: List<FrontLevelDetails>,
    val links: List<LinkModel>
)
data class FrontLevelDetails(
    val act: SwipeAct,
    val locationId: String,
    val locationBackground: String,
    val locationTitle: String,
    val locationDescription: String,
    val dialog: List<DialogEntryModel>,
    val waves: List<List<FrontMonsterEntryModel>>,
    val enabled: Boolean,
    val type: LevelType,
    val x: Float,
    val y: Float,
) {
    companion object {
        val DEFAULT = FrontLevelDetails(SwipeAct.ACT_1, "", "", "", "", emptyList(), emptyList(), false, LevelType.CAMPAIGN, 0f, 0f)
    }
}

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
    val rarity: Int?,
    val skin: String?
)
