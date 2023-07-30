package com.pl00t.swipe_client.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import ktx.app.KtxScreen

abstract class StageScreen(
    protected val coreAssetManager: AssetManager,
    protected var multiplexer: InputMultiplexer,
) : KtxScreen, SwipeContext {

    private val atlases = mutableMapOf<String, TextureAtlas>()

    val root: Stage

    private var isLoading = false

    private var loadingActor: Actor? = null

    private val loadingAms = mutableListOf<Pair<AssetManager, () -> Unit>>()

    val width: Float
    val height: Float

    init {
        atlases[Atlases.COMMON_CORE] = coreAssetManager.get(Atlases.COMMON_CORE)
        val ratio = Gdx.graphics.width.toFloat() / Gdx.graphics.height
        val width = 480f
        val height = width / ratio
        val viewport = StretchViewport(width, height)
        root = Stage(viewport)
        this.width = 480f
        this.height = height
    }

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
                am.update().also {
                    loaded -> if (loaded) {
                        am.assetNames.forEach { assetName ->
                            if (am.getAssetType(assetName) == TextureAtlas::class.java) {
                                atlases[assetName] = am.get(assetName)
                            }
                        }
                        action()
                    }
                }
            }
        }
    }

    override fun dispose() {
        super.dispose()
        multiplexer.removeProcessor(root)
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

    private fun createLoadingActor() = Image(commonAtlas(Atlases.COMMON_CORE).findRegion("loading")).apply {
        x = 0f
        y = 0f
        width = this@StageScreen.root.width
        height = this@StageScreen.root.height
        setScaling(Scaling.fill)
    }

    override fun width() = this.width
    override fun height() = this.height

    override fun commonAtlas(atlas: String): TextureAtlas {
        return atlases[atlas]!!
    }
}
