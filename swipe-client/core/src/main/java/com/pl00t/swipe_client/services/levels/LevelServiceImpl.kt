package com.pl00t.swipe_client.services.levels

import com.google.gson.Gson
import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.game7th.swipe.monsters.MonsterService
import com.pl00t.swipe_client.services.files.FileService
import com.pl00t.swipe_client.services.profile.SwipeAct

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
        val actModel = gson.fromJson(fileService.internalFile("json/${act.name}.json"), ActModel::class.java)
        acts[act] = actModel
        return actModel
    }

    override suspend fun getLevelDetails(act: SwipeAct, level: String, enabled: Boolean): FrontLevelModel {
        val actModel = getAct(act)
        val l = actModel.levels.firstOrNull { it.id == level } ?: return FrontLevelModel.DEFAULT
        return FrontLevelModel(
            x = l.x,
            y = 1024 - l.y,
            locationId = l.id,
            type = l.type,
            enabled = enabled,
            waves = l.monsters?.map { it.mapNotNull { e ->
                monsterService.getMonster(e.skin)?.let {
                    FrontMonsterEntryModel(it.skin, it.name, it.level)
                }
            } } ?: emptyList(),
            act = act,
            locationBackground = l.background,
            locationTitle = l.title,
            locationDescription = l.description,
            dialog = l.dialog ?: emptyList(),
            monsterPool = l.monster_pool?.map { it.skin } ?: emptyList()
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
