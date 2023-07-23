package com.pl00t.swipe_client.services.levels

import com.badlogic.gdx.Gdx
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

    override suspend fun getAct(act: SwipeAct): ActModel {
        return acts[act] ?: loadAct(act)
    }

    private suspend fun loadAct(act: SwipeAct): ActModel {
        val actModel = gson.fromJson(fileService.internalFile("json/${act.name}.json"), ActModel::class.java)
        acts[act] = actModel
        return actModel
    }

    override suspend fun getLevelDetails(act: SwipeAct, level: String): FrontLevelDetails {
        val actModel = getAct(act)
        val l = actModel.levels.firstOrNull { it.id == level } ?: return FrontLevelDetails.DEFAULT
        return FrontLevelDetails(
            x = l.x,
            y = 1024 - l.y,
            locationId = l.id,
            type = l.type,
            enabled = true,
            waves = l.monsters?.map { it.mapNotNull { e ->
                monsterService.getMonster(e.skin)?.let { monster ->
                    FrontMonsterEntryModel(monster.skin, monster.name, e.level)
                }
            } } ?: emptyList(),
            act = act,
            locationBackground = l.background,
            locationTitle = l.title,
            locationDescription = l.description,
            dialog = l.dialog ?: emptyList())
    }

    override suspend fun getFreeReward(act: SwipeAct, level: String): List<LevelReward> {
        val level = getAct(act).levels.firstOrNull { it.id == level } ?: return emptyList()
        val freeRewards = level.freeReward ?: emptyList()
        return freeRewards
    }
}
