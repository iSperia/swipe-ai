package com.pl00t.swipe_client.mine.data

interface MineService {

    suspend fun getMineProgress(): MineProgressFile

    suspend fun levelUp()

    suspend fun getUpgradeCost(): Int

    suspend fun maxLevel(): Int

    suspend fun level(): Int

    suspend fun getAttemptsPerTry(): Int

    suspend fun getMaxTier(): Int


}
