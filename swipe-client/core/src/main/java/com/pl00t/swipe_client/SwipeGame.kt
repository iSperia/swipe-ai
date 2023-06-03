package com.pl00t.swipe_client

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.pl00t.swipe_client.screen.MapScreen
import com.pl00t.swipe_client.ux.Colors
import ktx.async.KtxAsync

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms.  */
class SwipeGame : Game() {

    lateinit var amCore: AssetManager
    var coreLoaded = false

    override fun create() {
        KtxAsync.initiate()
        amCore = AssetManager()
        amCore.load("atlases/core.atlas", TextureAtlas::class.java)
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
            setScreen(MapScreen(amCore))
        }
    }
}
