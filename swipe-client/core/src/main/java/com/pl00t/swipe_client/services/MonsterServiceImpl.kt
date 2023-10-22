package com.pl00t.swipe_client.services

import com.game7th.swipe.game.*
import com.game7th.swipe.game.characters.*
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pl00t.swipe_client.services.files.FileService
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
            MonsterService.CHARACTER_SAFFRON -> provideSaffronTriggers(balance)
            MonsterService.MONSTER_THALENDROS -> provideThalendrosTriggers(balance)
            MonsterService.MONSTER_XALITHAR -> provideXalitharTriggers(balance)
            MonsterService.MONSTER_MALACHI -> provideMalachiTriggers(balance)
            MonsterService.MONSTER_STONE_GOLEM -> provideStoneGolemTriggers(balance)
            MonsterService.MONSTER_THORNSTALKER -> provideThornstalkerTriggers(balance)
            MonsterService.MONSTER_CORRUPTED_DRYAD -> provideCorruptedDryadTriggers(balance)
            MonsterService.MONSTER_CRYSTAL_GUARDIAN -> provideCrystalGuardianTriggers(balance)
            MonsterService.MONSTER_SHADOW_OF_CHAOS -> provideShadowOfChaosTriggers(balance)
            MonsterService.MONSTER_SHADOWBLADE_ROGUE -> provideShadowbladeRogueTriggers(balance)
            MonsterService.MONSTER_THORNED_CRAWLER -> provideThornedCrawlerTriggers(balance)
            else -> emptyMap()
        }

        map.forEach { (key, trigger) ->
            triggerCache[key] = trigger
        }

        loadedTriggers.add(skin)
    }

    override suspend fun createMonster(skin: String, level: Int, rarity: Int): FrontMonsterConfiguration {
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
            MonsterService.MONSTER_CRYSTAL_GUARDIAN -> provideCrystalGuardianAttributes(configFile.balance, attributes)
            MonsterService.MONSTER_THORNED_CRAWLER -> provideThornedCrawlerAbilities(configFile.balance, attributes)
            MonsterService.CHARACTER_VALERIAN -> provideValerianAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_STONE_GOLEM -> provideStoneGolemAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_THALENDROS -> provideThalendrosAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_XALITHAR -> provideXalitharAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_MALACHI -> provideMalachiAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_THORNSTALKER -> provideThornstalkerAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_SHADOW_OF_CHAOS -> provideShadowOfChaosAbilities(configFile.balance, attributes)
            MonsterService.MONSTER_SHADOWBLADE_ROGUE -> provideShadowbladeRogueAbilities(configFile.balance, attributes)

            else -> provideValerianAbilities(configFile.balance, attributes)
        }

        val health = (configFile.balance.intAttribute("base_health") * (1f + 0.1f * body)).toInt()
        val ult = (configFile.balance.intAttribute("ult") * (1f + 0.05f * mind)).toInt()
        val luck = (configFile.balance.intAttribute("luck") * (1f + 0.1f * spirit))

        val rarityAffixCount = when (rarity) {
            1 -> 2
            2 -> 4
            3 -> 6
            else -> 0
        }
        val affixes = (0 until rarityAffixCount).map {
            SbMonsterRarityAffix.values().random()
        }

        return FrontMonsterConfiguration(
            skin = skin,
            name = configFile.name,
            level = level,
            rarity = rarity,
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
            ultMax = configFile.balance.intAttribute("ult_max"),
            ultPrefillPercent = 0,
            rarityAffixes = affixes
        )
    }

    override suspend fun getTrigger(triggerId: String): SbTrigger? {
        return triggerCache[triggerId]
    }

    companion object {
        val STATS_BASE = listOf(
            3,6,9,12,15,19,23,27,31,35,40,45,50,55,60,66,72,78,84,90,97,104,111,118,125,133,141,149,157,165,174,183,192,201,210,220,230,240,250,260,271,282,293,304,315,327,339,351,363,375,388,401,414
        )
    }
}
