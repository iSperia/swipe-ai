package com.pl00t.swipe_client

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.StretchViewport

abstract class SbBaseScreen(
    protected val r: Resources
) : Screen {

    protected val root: Stage = Stage(StretchViewport(r.width, r.height))

    lateinit var splashActor: Image

    override fun show() {
        r.loadAtlas(Resources.core_atlas)
        r.onLoad {
            splashActor = Image(r.atlas(Resources.core_atlas).findRegion("loading")).apply {
                width = r.width
                height = r.height
                setScaling(Scaling.fill)
            }
            root.addActor(splashActor)
            loadScreenContent()
        }

        r.inputMultiplexer = InputMultiplexer()
        Gdx.input.inputProcessor = r.inputMultiplexer

        r.inputMultiplexer.addProcessor(root)
    }

    abstract fun loadScreenContent()

    fun hideSplash() {
        splashActor.addAction(Actions.sequence(
            Actions.alpha(0f, 0.3f),
            Actions.removeActor()
        ))
    }

    override fun render(delta: Float) {
        root.act()
        root.draw()
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
    }

    override fun dispose() {
        r.inputMultiplexer.removeProcessor(root)
    }
}
