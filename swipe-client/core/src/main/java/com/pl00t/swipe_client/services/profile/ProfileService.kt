package com.pl00t.swipe_client.services.profile

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.pl00t.swipe_client.services.levels.FrontActModel
import com.pl00t.swipe_client.services.levels.FrontLevelModel
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.monsters.MonsterService

interface ProfileService {

    suspend fun getProfile(): SwipeProfile

    suspend fun markActComplete(act: SwipeAct, level: String)

    suspend fun getAct(act: SwipeAct): FrontActModel
}

class ProfileServiceImpl(
    val levelService: LevelService,
    val monsterService: MonsterService,
) : ProfileService {

    val gson = Gson()
    val handle = Gdx.files.local("data/profile.txt")

    var profile: SwipeProfile

    init {
        profile = if (handle.exists()) {
            println(handle.file().absolutePath)
            val text = handle.readString()
            gson.fromJson(text, SwipeProfile::class.java)
        } else {
            SwipeProfile(
                balances = emptyList(),
                actProgress = listOf(
                    ActProgress(
                        SwipeAct.ACT_1,
                        listOf("c1")
                    )
                )
            )
        }
    }

    override suspend fun getProfile(): SwipeProfile = profile

    override suspend fun getAct(act: SwipeAct): FrontActModel {
        val actModel = levelService.getAct(act)
        val progress = profile.actProgress.firstOrNull { it.act == act } ?: return FrontActModel(emptyList(), emptyList())
        val availableLevels = actModel.levels.filter { progress.levelsAvailable.contains(it.id) }
        val availableLinks = actModel.links.filter { progress.levelsAvailable.contains(it.n1) || progress.levelsAvailable.contains(it.n2) }
        val disabledLevels = actModel.levels.filter { l -> !progress.levelsAvailable.contains(l.id) && availableLinks.any { it.n1 == l.id || it.n2 == l.id } }
        return FrontActModel(
            levels = availableLevels.map { l ->
                FrontLevelModel(l.x, 1024 - l.y, l.id, l.type, true, l.monsters?.map { it.map { e ->
                    val monster = monsterService.getMonster(e.skin)
                    FrontMonsterEntryModel(monster.skin, monster.name, e.level)
                } } ?: emptyList())
            } + disabledLevels.map { l ->
                FrontLevelModel(l.x, 1024 - l.y, l.id, l.type, false, l.monsters?.map { it.map { e ->
                    val monster = monsterService.getMonster(e.skin)
                    FrontMonsterEntryModel(monster.skin, monster.name, e.level)
                } } ?: emptyList())
            },
            links = availableLinks
        )
    }

    override suspend fun markActComplete(act: SwipeAct, level: String) {
        val actModel = levelService.getAct(act)
        val actProgress = profile.actProgress.firstOrNull { it.act == act } ?: return
        val levelsToUnlock = actModel.links
            .filter { it.n1 == level || it.n2 == level }
            .flatMap { listOf(it.n1, it.n2) }
            .filter { !actProgress.levelsAvailable.contains(it) }
        profile = profile.copy(actProgress = profile.actProgress.map { pap ->
            if (pap.act == act) {
                pap.copy(levelsAvailable = pap.levelsAvailable + levelsToUnlock)
            } else {
                pap
            }
        })
        saveProfile()
    }

    private fun saveProfile() {
        val text = gson.toJson(profile)
        handle.writeString(text, false)
    }
}
