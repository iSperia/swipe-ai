package com.game7th.swipe.monsters

import com.game7th.swipe.battle.SbMonsterConfiguration
import com.google.gson.JsonObject

interface MonsterService {

    suspend fun getMonster(skin: String): SbMonsterConfiguration?

    suspend fun allBalances(): Map<String, JsonObject>
}

data class MonsterConfigurationFile(
    val monsters: List<SbMonsterConfiguration>
)


