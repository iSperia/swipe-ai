package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.ux.Fonts
import kotlin.random.Random

class UltimateProgressActor(
    private val taBattle: TextureAtlas,
    private val w: Float,
    private val h: Float
) : Group() {

    lateinit var imageBg: Image
    lateinit var iProgress: Image
    lateinit var description: Label
    lateinit var foreground: Image

    var progress: Float = 0f

    init {
        imageBg = Image(taBattle.findRegion("semi_black_pixel")).apply {
            this.width = w * 0.9f
            this.height = h * 0.9f
            setScaling(Scaling.stretch)
            this.x = 0.05f * w
            this.y = 0.05f * h
        }
        addActor(imageBg)
        iProgress = Image(taBattle.findRegion("ult_progress")).apply {
            this.width = 0f
            this.height = h * 0.8f
            setScaling(Scaling.stretch)
            this.x = 0.05f * w
            this.y = 0.1f * h
            setScaling(Scaling.stretch)
        }
        addActor(iProgress)
        description = Fonts.createCaption("Fill for ultimate", h).apply {
            this.width = w
            this.height = h
            setAlignment(Align.center)
            this.x = 0f
            this.y = 0f
        }
        addActor(description)
        foreground = Image(taBattle.findRegion("ult_progress_fg")).apply {
            this.width = w
            this.height = h
            setScaling(Scaling.stretch)
        }
        addActor(foreground)
    }

    private fun updateUltimateProgress() {
        val newWidth = 0.9f * w * progress
        iProgress.addAction(SizeToAction().apply {
            setSize(newWidth, iProgress.height)
            duration = 0.2f
        })
    }
}
