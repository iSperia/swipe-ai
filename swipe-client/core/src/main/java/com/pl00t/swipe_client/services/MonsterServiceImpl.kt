package com.pl00t.swipe_client.services

import com.badlogic.gdx.Gdx
import com.game7th.swipe.battle.SbMonsterConfiguration
import com.game7th.swipe.battle.SbMonsterEntry
import com.game7th.swipe.battle.UnitSkin
import com.game7th.swipe.monsters.MonsterConfigurationFile
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.Gson
import com.google.gson.JsonObject

class MonsterServiceImpl() : MonsterService {

    val handle = Gdx.files.internal("json/monsters.json")
    var config: MonsterConfigurationFile
    val gson = Gson()

    private val balanceCache = mutableMapOf<String, JsonObject>()

    init {
        config = gson.fromJson(handle.readString(), MonsterConfigurationFile::class.java)
        config.monsters.forEach { monsterConfig ->
            balanceCache[monsterConfig.skin] = monsterConfig.balance
        }
    }

    override suspend fun getMonster(skin: String): SbMonsterConfiguration? {
        return config.monsters.firstOrNull { it.skin == skin }
    }

    override suspend fun allBalances(): Map<String, JsonObject> = balanceCache
}
