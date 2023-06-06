package com.pl00t.swipe_client.screen

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxScreen

abstract class StageScreen(
    protected val amCore: AssetManager,
    protected var multiplexer: InputMultiplexer,
) : KtxScreen {

    protected val taCore = amCore.get<TextureAtlas>("atlases/core.atlas")

    protected val root = Stage(ScreenViewport())

    private var isLoading = false

    private var loadingActor: Actor? = null

    private val loadingAms = mutableListOf<Pair<AssetManager, () -> Unit>>()

    override fun render(delta: Float) {
        super.render(delta)
        root.act()
        root.draw()
        if (loadingAms.isEmpty()) {
            if (isLoading) {
                isLoading = false
                loadingActor?.remove()
            }
        } else {
            loadingAms.removeAll { (am, action) ->
                am.update().also { loaded -> if (loaded) action() }
            }
        }
    }

    override fun dispose() {
        root.dispose()
        super.dispose()
    }

    protected fun loadAm(am: AssetManager, action: () -> Unit) {
        loadingAms.add(am to action)
        am.setErrorListener { asset, throwable ->
            ktx.log.error(throwable, "StageScreen") { "Loading ${asset.fileName}" }
        }
        if (!isLoading) {
            isLoading = true
            loadingActor = createLoadingActor()
            root.addActor(loadingActor)
        }
    }

    private fun createLoadingActor() = Image(taCore.findRegion("loading")).apply {
        x = 0f
        y = 0f
        width = this@StageScreen.root.width
        height = this@StageScreen.root.height
        setScaling(Scaling.fill)
    }
}
