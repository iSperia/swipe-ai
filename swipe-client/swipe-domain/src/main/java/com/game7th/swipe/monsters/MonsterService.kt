package com.game7th.swipe.monsters

import com.game7th.swipe.battle.SbMonsterConfiguration

interface MonsterService {

    suspend fun getMonster(skin: String): SbMonsterConfiguration?
}

data class MonsterConfigurationFile(
    val monsters: List<SbMonsterConfiguration>
)


