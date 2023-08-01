package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.ux.require
import ktx.actors.repeatForever

class UltimateProgressActor(
    private val context: SwipeContext,
    private val skin: Skin
) : Group() {

    var imageBg: Image
    var iProgress: Image
    var description: Label
    var foreground: Image

    var progress: Float = 0f

    init {
        imageBg = Image(context.commonAtlas(Atlases.COMMON_BATTLE).findRegion("semi_black_pixel").require()).apply {
            this.width = 270f
            this.height = 45f
            setScaling(Scaling.stretch)
        }
        addActor(imageBg)
        iProgress = Image(context.commonAtlas(Atlases.COMMON_BATTLE).findRegion("ult_progress").require()).apply {
            this.width = 0f
            this.height = 35f
            setScaling(Scaling.stretch)
            this.x = 20f
            this.y = 5f
            setOrigin(Align.center)
        }
        addActor(iProgress)
        description = Label("Fill for ultimate", skin, "regular20_white").apply {
            this.width = 270f
            this.height = 45f
            setAlignment(Align.center)
            this.x = 0f
            this.y = 0f
        }
        addActor(description)
        foreground = Image(context.commonAtlas(Atlases.COMMON_BATTLE).findRegion("ult_progress_fg").require()).apply {
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
