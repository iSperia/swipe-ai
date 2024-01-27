package com.game7th.swipe.monsters

import com.game7th.swipe.game.FrontMonsterConfiguration
import com.game7th.swipe.game.SbMonsterConfiguration
import com.game7th.swipe.game.SbTrigger

interface MonsterService {

    suspend fun getMonster(skin: String): SbMonsterConfiguration?

    suspend fun getTrigger(skin: String): SbTrigger?

    suspend fun loadTriggers(skin: String)

    suspend fun createMonster(skin: String, level: Int, rarity: Int): FrontMonsterConfiguration

    companion object {
        const val DEFAULT = "DEFAULT"
        const val CHARACTER_VALERIAN = "CHARACTER_VALERIAN"
        const val CHARACTER_SAFFRON = "CHARACTER_SAFFRON"
        const val MONSTER_THORNED_CRAWLER = "MONSTER_THORNED_CRAWLER"
        const val MONSTER_THORNSTALKER = "MONSTER_THORNSTALKER"
        const val MONSTER_SHADOW_OF_CHAOS = "MONSTER_SHADOW_OF_CHAOS"
        const val MONSTER_SHADOWBLADE_ROGUE = "MONSTER_SHADOWBLADE_ROGUE"
        const val MONSTER_CORRUPTED_DRYAD = "MONSTER_CORRUPTED_DRYAD"
        const val MONSTER_CRYSTAL_GUARDIAN = "MONSTER_CRYSTAL_GUARDIAN"
        const val MONSTER_THALENDROS = "MONSTER_THALENDROS"
        const val MONSTER_STONE_GOLEM = "MONSTER_STONE_GOLEM"
        const val MONSTER_XALITHAR = "MONSTER_XALITHAR"
        const val MONSTER_MALACHI = "MONSTER_MALACHI"
    }
}
