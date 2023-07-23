package com.game7th.swipe.monsters

import com.game7th.swipe.battle.SbMonsterConfiguration
import com.game7th.swipe.game.SbTrigger
import com.google.gson.JsonObject

interface MonsterService {

    suspend fun getMonster(skin: String): SbMonsterConfiguration?

    suspend fun getTrigger(skin: String): SbTrigger?

    suspend fun loadTriggers(skin: String)

    companion object {
        const val DEFAULT = "DEFAULT"
        const val CHARACTER_VALERIAN = "CHARACTER_VALERIAN"
        const val MONSTER_THORNED_CRAWLER = "MONSTER_THORNED_CRAWLER"
        const val MONSTER_THORNSTALKER = "MONSTER_THORNSTALKER"
        const val MONSTER_CORRUPTED_DRYAD = "MONSTER_CORRUPTED_DRYAD"
        const val MONSTER_THALENDROS = "MONSTER_THALENDROS"
    }
}

data class MonsterConfigurationFile(
    val monsters: List<SbMonsterConfiguration>
)


