package com.pl00t.swipe_client

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.pl00t.swipe_client.screen.ScreenRouter
import com.pl00t.swipe_client.screen.battle.BattleScreen
import com.pl00t.swipe_client.screen.map.MapScreen
import com.pl00t.swipe_client.services.battle.BattleService
import com.pl00t.swipe_client.services.battle.BattleServiceImpl
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.levels.LevelServiceImpl
import com.game7th.swipe.monsters.MonsterService
import com.google.gson.Gson
import com.pl00t.swipe_client.home.HomeScreen
import com.pl00t.swipe_client.services.MonsterServiceImpl
import com.pl00t.swipe_client.services.files.FileService
import com.pl00t.swipe_client.services.files.GdxFileService
import com.pl00t.swipe_client.services.items.ItemService
import com.pl00t.swipe_client.services.items.ItemServiceImpl
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.ProfileServiceImpl
import com.pl00t.swipe_client.services.profile.SwipeAct
import kotlinx.coroutines.launch
import ktx.async.KtxAsync

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms.  */
class SwipeGame : Game(), ScreenRouter {

    var coreLoaded = false
    lateinit var r: R
    lateinit var profileService: ProfileService

    override fun create() {
        KtxAsync.initiate()
        val ratio = Gdx.graphics.width.toFloat() / Gdx.graphics.height

        r = R().apply {
            width = 480f
            height = 480f / ratio
            router = this@SwipeGame
            fileService = GdxFileService()
            itemService = ItemServiceImpl(Gson(), fileService)
            monsterService = MonsterServiceImpl(fileService)
            levelService = LevelServiceImpl(fileService, monsterService)
            profileService = ProfileServiceImpl(levelService, monsterService, itemService)
            battleService = BattleServiceImpl(levelService, monsterService, profileService)
        }

        navigateMap()
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT or (if (Gdx.graphics.getBufferFormat().coverageSampling) GL20.GL_COVERAGE_BUFFER_BIT_NV else 0))
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glClear(GL20.GL_ALPHA_BITS)

        r.update()
        super.render()
    }

    override fun navigateBattle() {
        setScreen(BattleScreen(r, this@SwipeGame))
    }

    override fun navigateMap() {
        setScreen(HomeScreen(r))
    }
}
