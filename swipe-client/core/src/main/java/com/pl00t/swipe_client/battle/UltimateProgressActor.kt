package com.pl00t.swipe_client.battle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.ux.require
import ktx.actors.repeatForever

class UltimateProgressActor(
    private val r: R
) : Group() {

    var imageBg: Image
    var iProgress: Image
    var description: Label
    var foreground: Image

    var progress: Float = 0f

    init {
        imageBg = r.image(R.battle_atlas, "semi_black_pixel").apply {
            this.width = 270f
            this.height = 45f
            setScaling(Scaling.stretch)
        }
        addActor(imageBg)
        iProgress = r.image(R.battle_atlas, "ult_progress").apply {
            this.width = 0f
            this.height = 35f
            setScaling(Scaling.stretch)
            this.x = 20f
            this.y = 5f
            setOrigin(Align.center)
        }
        addActor(iProgress)
        description = r.regular20White("Fill for ultimate").apply {
            this.width = 270f
            this.height = 45f
            setAlignment(Align.center)
            this.x = 0f
            this.y = 0f
        }
        addActor(description)
        foreground = r.image(R.battle_atlas, "ult_progress_fg").apply {
            this.width = 270f
            this.height = 45f
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
        val newWidth = progress * 230f
        iProgress.addAction(Actions.sizeTo(newWidth, iProgress.height, 0.5f))
    }
}
