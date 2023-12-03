package com.pl00t.swipe_client.mine.data

import com.pl00t.swipe_client.services.profile.FrontItemEntryModel

interface MineService {

    suspend fun getMineProgress(): MineProgressFile

    suspend fun levelUp()

    suspend fun getUpgradeCost(): Int

    suspend fun maxLevel(): Int

    suspend fun level(): Int

    suspend fun getAttemptsPerTry(): Int

    suspend fun getMaxTier(): Int

    suspend fun getGemTemplate(skin: String): MineItemConfig

    suspend fun addGems(gems: List<MineItem>)

    suspend fun listGems(): List<MineItem>
    suspend fun spendGem(skin: String, level: Int)

}
