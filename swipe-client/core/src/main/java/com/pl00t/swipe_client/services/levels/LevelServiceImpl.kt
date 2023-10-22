package com.pl00t.swipe_client.services.levels

import com.game7th.swipe.SbText
import com.google.gson.Gson
import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.game7th.swipe.monsters.MonsterService
import com.pl00t.swipe_client.services.files.FileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import java.lang.IllegalArgumentException

private data class ActFileModel(
    val title: SbText,
    val levels: List<String>,
    val links: List<LinkModel>,
    val lore: SbText,
)

class LevelServiceImpl(
    private val fileService: FileService,
    private val monsterService: MonsterService
) : LevelService {

    private val gson = Gson()
    private val acts = mutableMapOf<SwipeAct, ActModel>()
    private val drops = mutableListOf<SbDropEntry>()

    init {
        val fileEntries = gson.fromJson(fileService.internalFile("json/droprate.json"), SbDropFile::class.java).entries
        fileEntries.forEach { fileEntry ->
            fileEntry.currency?.forEach { currency ->
                drops.add(
                    SbDropEntry(
                    currency = currency,
                    item = null,
                    weight = fileEntry.weight,
                    value = fileEntry.value,
                    act = fileEntry.act,
                    level = fileEntry.level,
                    minLevel = fileEntry.minLevel,
                    rarity = fileEntry.rarity
                ))
            }
            fileEntry.items?.forEach { item ->
                drops.add(
                    SbDropEntry(
                        currency = null,
                        item = item,
                        weight = fileEntry.weight,
                        value = fileEntry.value,
                        act = fileEntry.act,
                        level = fileEntry.level,
                        minLevel = fileEntry.minLevel,
                        rarity = fileEntry.rarity
                    ))
            }
        }
    }

    override suspend fun getAct(act: SwipeAct): ActModel {
        return acts[act] ?: loadAct(act)
    }

    private suspend fun loadAct(act: SwipeAct): ActModel {
        val actModel = gson.fromJson(fileService.internalFile("json/acts/${act.name}/act.json"), ActFileModel::class.java)
        val levels = actModel.levels.map {
            gson.fromJson(fileService.internalFile("json/acts/${act.name}/$it.json"), LevelModel::class.java)
        }
        acts[act] = ActModel(
            levels = levels,
            links = actModel.links,
            title = actModel.title,
            lore = actModel.lore,
        )
        return acts[act]!!
    }

    override suspend fun getLevelDetails(act: SwipeAct, level: String, enabled: Boolean): FrontLevelModel {
        val actModel = getAct(act)
        val l = actModel.levels.firstOrNull { it.id == level } ?: throw IllegalArgumentException("$act invalid act")
        return FrontLevelModel(
            x = l.x,
            y = 1024 - l.y,
            locationId = l.id,
            type = l.type,
            enabled = enabled,
            waves = l.monsters?.map { it.mapNotNull { e ->
                monsterService.getMonster(e.skin)?.let {
                    FrontMonsterEntryModel(it.skin, it.name, e.level, e.rarity)
                }
            } } ?: emptyList(),
            act = act,
            locationBackground = l.background,
            locationTitle = l.title,
            dialog = l.dialog ?: emptyList(),
        )
    }

    override suspend fun getRaidDetails(act: SwipeAct, level: String): FrontRaidModel {
        val actModel = getAct(act)
        val l = actModel.levels.firstOrNull { it.id == level } ?: throw IllegalArgumentException("$act invalid act")
        return FrontRaidModel(
            act = act,
            locationId = l.id,
            locationBackground = l.background,
            locationTitle = l.title,
            tiers = l.tiers!!,
            locationType = l.type,
            bossSkin = if (l.type == LevelType.BOSS) l.monsters!!.first().first().skin else null
        )
    }

    override suspend fun getFreeReward(act: SwipeAct, level: String): List<LevelReward> {
        val level = getAct(act).levels.firstOrNull { it.id == level } ?: return emptyList()
        val freeRewards = level.freeReward ?: emptyList()
        return freeRewards
    }


    override suspend fun getLevelSpecificDrops(act: SwipeAct, level: String, locationLevel: Int): List<SbDropEntry> {
        return drops.filter { it.act == act && it.level == level && locationLevel >= it.minLevel}
    }

    override suspend fun getCommonDrops(locationLevel: Int): List<SbDropEntry> {
        return drops.filter {
            it.act == null && it.level == null && locationLevel >= it.minLevel
        }
    }

    override suspend fun getLevelPremiumCost(act: SwipeAct, level: String, tier: Int): Int {
        return 1
    }
}
