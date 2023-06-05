package com.pl00t.swipe_client.services.levels

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import com.pl00t.swipe.model.ActModel
import kotlin.random.Random

class LevelServiceImpl : LevelService {

    private val gson = Gson()

    val act1: ActModel = gson.fromJson(Gdx.files.local("assets/json/act1.json").readString(), ActModel::class.java)

    override suspend fun getAct(actName: String): FrontActModel {
        //assume everything is enabled for now
        return FrontActModel(
            levels = act1.levels.map {
                FrontLevelModel(
                    x = it.x,
                    y = 1024 - it.y,
                    enabled = Random.nextInt(5) > 2,
                    type = it.type,
                    id = it.id
                )
            },
            links = act1.links,
            background = act1.background
        )
    }

    override suspend fun getLevelDetails(level: String): FrontLevelDetails {
        return when(Random.nextInt(3)) {
            0 -> FrontLevelDetails("location_groves", "The Verdant Grove")
            1 -> FrontLevelDetails("location_crystal_caves", "The Crystal Caverns")
            else -> FrontLevelDetails("location_harmony_citadel", "The Sacred Citadel")
        }
    }
}
