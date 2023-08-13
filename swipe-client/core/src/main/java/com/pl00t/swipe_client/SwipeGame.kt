package com.pl00t.swipe_client

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.game7th.swipe.Lang
import com.pl00t.swipe_client.services.battle.BattleServiceImpl
import com.pl00t.swipe_client.services.levels.LevelServiceImpl
import com.google.gson.Gson
import com.pl00t.swipe_client.analytics.AnalyticsInterface
import com.pl00t.swipe_client.home.HomeScreen
import com.pl00t.swipe_client.services.MonsterServiceImpl
import com.pl00t.swipe_client.services.files.GdxFileService
import com.pl00t.swipe_client.services.items.ItemServiceImpl
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.ProfileServiceImpl
import ktx.async.KtxAsync

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms.  */
class SwipeGame(
    private val lang: String,
    private val analyticsInterface: AnalyticsInterface) : Game() {

    var coreLoaded = false
    lateinit var r: Resources
    lateinit var profileService: ProfileService

    override fun create() {
        KtxAsync.initiate()
        val ratio = Gdx.graphics.width.toFloat() / Gdx.graphics.height

        r = Resources().apply {
            width = 480f
            height = 480f / ratio
            l = if (lang.startsWith("ru")) Lang.RU else Lang.EN
            fileService = GdxFileService()
            itemService = ItemServiceImpl(Gson(), fileService)
            monsterService = MonsterServiceImpl(fileService)
            levelService = LevelServiceImpl(fileService, monsterService)
            profileService = ProfileServiceImpl(levelService, monsterService, itemService)
            battleService = BattleServiceImpl(levelService, monsterService, profileService)
            analytics = this@SwipeGame.analyticsInterface
        }

        setScreen(HomeScreen(r))
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT or (if (Gdx.graphics.getBufferFormat().coverageSampling) GL20.GL_COVERAGE_BUFFER_BIT_NV else 0))
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glClear(GL20.GL_ALPHA_BITS)

        r.update()
        super.render()
    }
}
