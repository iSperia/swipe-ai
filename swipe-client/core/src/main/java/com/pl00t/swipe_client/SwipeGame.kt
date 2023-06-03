package com.pl00t.swipe_client

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.pl00t.swipe_client.screen.map.MapScreen
import com.pl00t.swipe_client.services.LevelService
import com.pl00t.swipe_client.services.LevelServiceImpl
import com.pl00t.swipe_client.ux.Colors
import com.pl00t.swipe_client.ux.Fonts
import ktx.async.KtxAsync

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms.  */
class SwipeGame : Game() {

    lateinit var amCore: AssetManager
    var coreLoaded = false
    lateinit var levelService: LevelService

    override fun create() {
        KtxAsync.initiate()
        amCore = AssetManager()
        amCore.load("atlases/core.atlas", TextureAtlas::class.java)
        amCore.load("fonts/cinzel.fnt", BitmapFont::class.java)
        amCore.load("fonts/notepad.fnt", BitmapFont::class.java)
    }

    override fun render() {
        Gdx.gl.glClearColor(Colors.BG_COLOR.r, Colors.BG_COLOR.g, Colors.BG_COLOR.b, Colors.BG_COLOR.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
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
            setScreen(MapScreen(amCore, levelService))
        }
    }
}
