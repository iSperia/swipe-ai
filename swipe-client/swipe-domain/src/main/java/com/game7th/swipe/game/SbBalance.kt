package com.game7th.swipe.game

import com.game7th.swipe.battle.*
import com.google.gson.JsonObject

interface SbBalanceProvider {
    fun getBalance(key: String): JsonObject
    fun getMonster(skin: String): SbMonsterConfiguration
}

