package com.pl00t.swipe_client.services.levels

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import com.pl00t.swipe_client.services.profile.SwipeAct

class LevelServiceImpl : LevelService {

    private val gson = Gson()
    private val acts = mutableMapOf<SwipeAct, ActModel>()

    override suspend fun getAct(act: SwipeAct): ActModel {
        return acts[act] ?: loadAct(act)
    }

    private suspend fun loadAct(act: SwipeAct): ActModel {
        val file = Gdx.files.local("assets/json/${act.name}.json")
        val actModel = gson.fromJson<ActModel>(file.readString(), ActModel::class.java)
        acts[act] = actModel
        return actModel
    }

    override suspend fun getLevelDetails(act: SwipeAct, level: String): FrontLevelDetails {
        val actModel = getAct(act)
        val level = actModel.levels.firstOrNull { it.id == level } ?: return FrontLevelDetails("", "", "", "", emptyList())
        return FrontLevelDetails(
            locationId = level.id,
            locationBackground = level.background,
            locationDescription = level.description,
            locationTitle = level.title,
            dialog = level.dialog ?: emptyList()
        )
    }

    override suspend fun getFreeReward(act: SwipeAct, level: String): List<LevelReward> {
        val level = getAct(act).levels.firstOrNull { it.id == level } ?: return emptyList()
        val freeRewards = level.freeReward ?: emptyList()
        return freeRewards
    }
}
