package com.pl00t.swipe_client.services

import com.game7th.swipe.game.*
import com.game7th.swipe.game.characters.*
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pl00t.swipe_client.services.files.FileService
import ktx.style.skin
import java.lang.IllegalArgumentException

class MonsterServiceImpl(
    private val fileService: FileService,
) : MonsterService {

    val cache = mutableMapOf<String, SbMonsterConfiguration>()
    val loadedTriggers = mutableSetOf<String>()
    val triggerCache = mutableMapOf<String, SbTrigger>()
    val gson = Gson()

    override suspend fun getMonster(skin: String): SbMonsterConfiguration? {
        return cache[skin] ?: loadMonster(skin).let { cache[skin] }
    }

    private fun loadMonster(skin: String) {
        println("loading $skin")
        val monsterString = fileService.internalFile("json/monsters/$skin.json") ?: return
        val config = gson.fromJson(monsterString, SbMonsterConfiguration::class.java)
        cache[skin] = config
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

    override suspend fun createMonster(skin: String, level: Int): FrontMonsterConfiguration {
        val configFile = getMonster(skin) ?: throw IllegalArgumentException("Did not find $skin monster")
        val amountOfStats = (STATS_BASE[level] * (0.34f + 0.03f * level)).toInt()
        var body = (configFile.attributes.body * 0.01f * amountOfStats).toInt()
        var spirit = (configFile.attributes.spirit * 0.01f * amountOfStats).toInt()
        var mind = (configFile.attributes.mind * 0.01f * amountOfStats).toInt()
        if (body + spirit + mind < amountOfStats) {
            if (configFile.attributes.body > configFile.attributes.spirit) {
                if (configFile.attributes.body > configFile.attributes.mind) {
                    body++
                } else {
                    mind++
                }
            } else {
                if (configFile.attributes.spirit > configFile.attributes.mind) {
                    spirit++
                } else {
                    mind++
                }
            }
        }
        var attributes = CharacterAttributes(mind = mind, spirit = spirit, body = body)

        val abilities = when (skin) {
            MonsterService.MONSTER_CORRUPTED_DRYAD -> provideCorruptedDryadAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_THORNED_CRAWLER -> provideThornedCrawlerAbilities(configFile.balance, attributes)
            MonsterService.CHARACTER_VALERIAN -> provideValerianAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_THALENDROS -> provideThalendrosAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_THORNSTALKER -> provideThornstalkerAbilities(configFile.balance, attributes)

            else -> provideValerianAbilities(configFile.balance, attributes)
//            else -> throw IllegalArgumentException("Can't create monster $skin")
        }

        val health = (configFile.balance.intAttribute("base_health") * (1f + 0.01f * body)).toInt()
        val ult = (configFile.balance.intAttribute("ult") * (1f + 0.05f * mind)).toInt()
        val luck = (configFile.balance.intAttribute("luck") * (1f + 0.1f * spirit))

        return FrontMonsterConfiguration(
            skin = skin,
            name = configFile.name,
            level = level,
            attributes = attributes,
            resist = configFile.balance.getAsJsonObject("resist").let { r ->
                SbElemental(
                    phys = r.floatAttribute("phys"),
                    dark = r.floatAttribute("dark"),
                    light = r.floatAttribute("light"),
                    shock = r.floatAttribute("shock"),
                    fire = r.floatAttribute("fire"),
                    cold = r.floatAttribute("cold"),
                )
            },
            damage = SbElemental(),
            abilities = abilities,
            lore = configFile.lore,
            health = health,
            luck = luck,
            ult = ult,
            ultMax = configFile.balance.intAttribute("ult_max")
        )
    }

    override suspend fun getTrigger(triggerId: String): SbTrigger? {
        return triggerCache[triggerId]
    }

    companion object {
        val STATS_BASE = listOf(
            3,6,9,12,15,19,23,27,31,35,40,45,50,55,60,66,72,78,84
        )
    }
}
