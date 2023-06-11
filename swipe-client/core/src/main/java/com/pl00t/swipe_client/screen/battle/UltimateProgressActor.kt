package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.ux.Colors
import com.pl00t.swipe_client.ux.Fonts
import ktx.actors.repeat
import ktx.actors.repeatForever
import kotlin.random.Random

class UltimateProgressActor(
    private val taBattle: TextureAtlas,
    private val w: Float,
    private val h: Float
) : Group() {

    var imageBg: Image
    var iProgress: Image
    var description: Label
    var foreground: Image

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
            setOrigin(Align.center)
        }
        addActor(iProgress)
        description = Fonts.createWhiteCaption("Fill for ultimate", h).apply {
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

    fun updateUltimateProgress(progress: Float) {
        if (this.progress != progress) {
            this.progress = progress
            if (progress == 1f) {
                description.color = Color.RED
                description.setText("Ultimate ready!")
                description.addAction(
                    Actions.sequence(
                        Actions.color(Color.WHITE, 0.1f),
                        Actions.color(Color.RED, 0.1f)
                    ).repeatForever()
                )
                iProgress.addAction(
                    Actions.sequence(
                        Actions.parallel(
                            Actions.alpha(0.9f, 0.1f),
                            Actions.scaleBy(0.02f, 0.02f, 0.2f)
                        ),
                        Actions.parallel(
                            Actions.alpha(1f,  0.1f),
                            Actions.scaleBy(-0.02f, -0.02f, 0.2f)
                        )
                    ).repeatForever()
                )
            } else {
                description.color = Color.WHITE
                description.setText("Fill to ultimate")
                iProgress.clearActions()
                description.clearActions()
            }
        }
        val newWidth = 0.9f * w * progress
        iProgress.addAction(Actions.sizeTo(newWidth, iProgress.height, 0.5f))
    }
}
