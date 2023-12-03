package com.pl00t.swipe_client.mine.data

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.services.profile.SwipeCurrency

class MineServiceImpl(
    private val r: Resources
) : MineService {

    private val configFile = Gdx.files.internal("json/mine/mine_config.json")

    private val handle = Gdx.files.local("data/mine.txt")
    private val gson = Gson()

    private val config: MineConfigurationFile = gson.fromJson(configFile.readString("UTF-8"), MineConfigurationFile::class.java)
    private var progress: MineProgressFile = if (handle.exists()) {
        gson.fromJson(handle.readString(), MineProgressFile::class.java)
    } else {
        MineProgressFile(
            items = emptyList(),
            level = 0
        )
    }

    private fun saveProgress() {
        val text = gson.toJson(progress)
        handle.writeString(text, false)
    }

    init {
        saveProgress()
    }

    override suspend fun getMineProgress(): MineProgressFile {
        return progress
    }

    override suspend fun getGemTemplate(skin: String) = config.stones.first { it.skin == skin }

    override suspend fun getUpgradeCost(): Int {
        return config.upgradeCosts[progress.level]
    }

    override suspend fun levelUp() {
        if (progress.level >= config.maxLevel) {
            return
        }
        val cost = config.upgradeCosts[progress.level]

        if (cost <= r.profileService.getProfile().getBalance(SwipeCurrency.ETHERIUM_COIN)) {
            r.profileService.spendCurrency(arrayOf(SwipeCurrency.ETHERIUM_COIN), arrayOf(cost))
            progress = progress.copy(level = progress.level + 1)
            saveProgress()
        }
    }

    override suspend fun maxLevel(): Int {
        return config.maxLevel
    }

    override suspend fun level(): Int {
        return progress.level
    }

    override suspend fun getAttemptsPerTry(): Int {
        return config.attempts[progress.level]
    }

    override suspend fun getMaxTier(): Int {
        return config.maxTiers[progress.level]
    }

    override suspend fun addGems(gems: List<MineItem>) {
        progress = progress.copy(items = progress.items + gems)
        saveProgress()
    }

    override suspend fun listGems(): List<MineItem> = progress.items

    override suspend fun spendGem(skin: String, level: Int) {
        var found = false
        progress = progress.copy(items = progress.items.mapNotNull { gem ->
            if (found) gem else if (gem.skin == skin && gem.tier == level) {
                found = true
                null
            } else {
                gem
            }
        })
        saveProgress()
    }
}


