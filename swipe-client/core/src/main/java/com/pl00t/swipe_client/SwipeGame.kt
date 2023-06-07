package com.pl00t.swipe_client

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.pl00t.swipe_client.screen.battle.BattleScreen
import com.pl00t.swipe_client.screen.map.MapScreen
import com.pl00t.swipe_client.services.battle.BattleService
import com.pl00t.swipe_client.services.battle.BattleServiceImpl
import com.pl00t.swipe_client.services.levels.FrontLevelDetails
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.levels.LevelServiceImpl
import com.pl00t.swipe_client.ux.Fonts
import ktx.async.KtxAsync

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms.  */
class SwipeGame : Game() {

    lateinit var amCore: AssetManager
    var coreLoaded = false
    lateinit var levelService: LevelService
    lateinit var battleService: BattleService
    lateinit var inputMultiplexer: InputMultiplexer

    override fun create() {
        KtxAsync.initiate()
        inputMultiplexer = InputMultiplexer()
        amCore = AssetManager()
        amCore.load("atlases/core.atlas", TextureAtlas::class.java)
        amCore.load("fonts/cinzel.fnt", BitmapFont::class.java)
        amCore.load("fonts/notepad.fnt", BitmapFont::class.java)
        battleService = BattleServiceImpl()
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT or (if (Gdx.graphics.getBufferFormat().coverageSampling) GL20.GL_COVERAGE_BUFFER_BIT_NV else 0))
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glClear(GL20.GL_ALPHA_BITS)

//        Gdx.gl.glClearColor(Colors.BG_COLOR.r, Colors.BG_COLOR.g, Colors.BG_COLOR.b, Colors.BG_COLOR.a)
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        super.render()

        if (!coreLoaded) {
            checkCoreLoading()
        }
    }

    private fun checkCoreLoading() {
        if (amCore.update()) {
            coreLoaded = true
            Fonts.init(amCore)
            levelService = LevelServiceImpl()
//            setScreen(MapScreen(amCore, inputMultiplexer, levelService))
            setScreen(BattleScreen(amCore, inputMultiplexer, battleService))
        }
    }
}
