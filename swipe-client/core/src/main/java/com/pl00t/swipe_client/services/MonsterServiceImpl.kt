package com.pl00t.swipe_client.services

import com.game7th.swipe.game.SbMonsterConfiguration
import com.game7th.swipe.game.SbTrigger
import com.game7th.swipe.game.characters.*
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pl00t.swipe_client.services.files.FileService

class MonsterServiceImpl(
    private val fileService: FileService,
) : MonsterService {

    val cache = mutableMapOf<String, SbMonsterConfiguration>()
    val loadedTriggers = mutableSetOf<String>()
    val triggerCache = mutableMapOf<String, SbTrigger>()
    val gson = Gson()

    override suspend fun getMonster(skin: String): SbMonsterConfiguration? {
        return cache[skin] ?: loadMonster(skin)
    }

    private fun loadMonster(skin: String): SbMonsterConfiguration? {
        val monsterString = fileService.localFile("assets/json/monsters/$skin.json") ?: return null
        val config = gson.fromJson(monsterString, SbMonsterConfiguration::class.java)
        cache[skin] = config
        return config
    }

    override suspend fun loadTriggers(skin: String) {
        if (loadedTriggers.contains(skin)) return
        val balance = getMonster(skin)?.balance ?: JsonObject()
        val map: Map<String, SbTrigger> = when(skin) {
            MonsterService.DEFAULT -> provideDefaultTriggers()
            MonsterService.CHARACTER_VALERIAN -> provideValerianTriggers(balance)
            MonsterService.MONSTER_THALENDROS -> provideThalendrosTriggers(balance)
            MonsterService.MONSTER_THORNSTALKER -> provideThornstalkerTriggers(balance)
            MonsterService.MONSTER_CORRUPTED_DRYAD -> provideCorruptedDryadTriggers(balance)
            MonsterService.MONSTER_THORNED_CRAWLER -> provideThornedCrawlerTriggers(balance)
            else -> emptyMap()
        }

        map.forEach { (key, trigger) ->
            triggerCache[key] = trigger
        }

        loadedTriggers.add(skin)
    }

    override suspend fun getTrigger(triggerId: String): SbTrigger? {
        return triggerCache[triggerId]
    }
}
