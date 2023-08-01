package com.pl00t.swipe_client.services.levels

import com.game7th.swipe.SbText
import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency

data class SbDropFileEntry(
    val currency: List<SwipeCurrency>?,
    val items: List<String>?,
    val weight: Int,
    val value: Int,
    val act: SwipeAct?,
    val level: String?,
    val minLevel: Int,
    val rarity: Int,
)

data class SbDropFile(
    val entries: List<SbDropFileEntry>
)

data class SbDropEntry(
    val currency: SwipeCurrency?,
    val item: String?,
    val weight: Int,
    val value: Int,
    val act: SwipeAct?,
    val level: String?,
    val minLevel: Int,
    val rarity: Int,
)

interface LevelService {

    suspend fun getAct(act: SwipeAct): ActModel

    suspend fun getLevelDetails(act: SwipeAct, level: String, enabled: Boolean): FrontLevelModel

    suspend fun getFreeReward(act: SwipeAct, level: String): List<LevelReward>

    suspend fun getLevelSpecificDrops(act: SwipeAct, level: String, locationLevel: Int): List<SbDropEntry>

    suspend fun getCommonDrops(locationLevel: Int): List<SbDropEntry>

    suspend fun getLevelPremiumCost(act: SwipeAct, level: String, tier: Int): Int

}

data class FrontActModel(
    val title: SbText,
    val levels: List<FrontLevelModel>,
    val links: List<LinkModel>
)
data class FrontLevelModel(
    val act: SwipeAct,
    val locationId: String,
    val locationBackground: String,
    val locationTitle: String,
    val locationDescription: String,
    val dialog: List<DialogEntryModel>,
    val waves: List<List<FrontMonsterEntryModel>>,
    val monsterPool: List<String>,
    val enabled: Boolean,
    val type: LevelType,
    val x: Float,
    val y: Float,
) {
    companion object {
        val DEFAULT = FrontLevelModel(SwipeAct.ACT_1, "", "", "", "", emptyList(), emptyList(), emptyList(),
            false, LevelType.CAMPAIGN, 0f, 0f)
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
