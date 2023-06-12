package com.pl00t.swipe_client.services.monsters

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import com.pl00t.swipe_client.services.battle.MonsterConfiguration
import com.pl00t.swipe_client.services.battle.UnitSkin
import com.pl00t.swipe_client.services.battle.logic.processor.TileGeneratorConfig

interface MonsterService {

    suspend fun getMonster(skin: UnitSkin): MonsterConfiguration
}

data class MonsterConfigurationFile(
    val monsters: List<MonsterConfiguration>
)

class MonsterServiceImpl() : MonsterService {

    val handle = Gdx.files.local("json/monsters.json")
    var config: MonsterConfigurationFile
    val gson = Gson()

    init {
        config = gson.fromJson(handle.readString(), MonsterConfigurationFile::class.java)
    }

    override suspend fun getMonster(skin: UnitSkin): MonsterConfiguration {
        return config.monsters.firstOrNull { it.skin == skin }
            ?: MonsterConfiguration(skin = UnitSkin.MONSTER_THORNSTALKER, 30, TileGeneratorConfig(emptyList()), 1f, 0f, 0f, 0f, 0f, 0f, 0, 0, 0, 1)
    }
}
