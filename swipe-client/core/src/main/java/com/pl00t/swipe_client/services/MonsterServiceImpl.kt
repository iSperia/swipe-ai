package com.pl00t.swipe_client.services

import com.badlogic.gdx.Gdx
import com.game7th.swipe.battle.SbMonsterConfiguration
import com.game7th.swipe.battle.SbMonsterEntry
import com.game7th.swipe.battle.UnitSkin
import com.game7th.swipe.monsters.MonsterConfigurationFile
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.Gson

class MonsterServiceImpl() : MonsterService {

    val handle = Gdx.files.internal("json/monsters.json")
    var config: MonsterConfigurationFile
    val gson = Gson()

    init {
        config = gson.fromJson(handle.readString(), MonsterConfigurationFile::class.java)
    }

    override suspend fun getMonster(skin: String): SbMonsterConfiguration? {
        return config.monsters.firstOrNull { it.skin == skin }
    }
}
