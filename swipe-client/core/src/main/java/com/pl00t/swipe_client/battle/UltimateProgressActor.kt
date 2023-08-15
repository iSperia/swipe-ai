package com.pl00t.swipe_client.battle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import ktx.actors.alpha
import ktx.actors.repeatForever

class UltimateProgressActor(
    private val r: Resources
) : Group() {

    var imageBg: Image
    var iProgress: Image
    var description: Label

    var progress: Float = 0f

    init {
        imageBg = r.image(Resources.ux_atlas, "background_accent").apply {
            this.width = 300f
            this.height = 30f
            alpha = 1f
        }
        addActor(imageBg)
        iProgress = r.image(Resources.ux_atlas, "background_main").apply {
            this.width = 284f
            this.height = 24f
            setScaling(Scaling.stretch)
            this.x = 3f
            this.y = 3f
            setOrigin(Align.left)
            scaleX = 0f
        }
        addActor(iProgress)
        val shadow = r.image(Resources.ux_atlas, "background_black").apply {
            setSize(300f, 30f)
            alpha = 0.5f
        }
        addActor(shadow)
        description = r.regular24White("${UiTexts.BattleUltProgress.value(r.l)}0%").apply {
            this.width = 270f
            this.height = 30f
            setAlignment(Align.center)
            this.x = 0f
            this.y = 0f
        }
        addActor(description)
    }

    fun updateUltimateProgress(progress: Float) {
        if (this.progress != progress) {
            this.progress = progress
            this.iProgress.scaleX = progress
            if (progress == 1f) {
                description.color = Color.RED
                description.setText(UiTexts.BattleUltReady.value(r.l))
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
                            Actions.scaleBy(0f, 0.05f, 0.2f)
                        ),
                        Actions.parallel(
                            Actions.alpha(1f,  0.1f),
                            Actions.scaleBy(0f, -0.05f, 0.2f)
                        )
                    ).repeatForever()
                )
            } else {
                description.color = Color.WHITE
                description.setText("${UiTexts.BattleUltProgress.value(r.l)}${"%.0f".format(progress*100)}%")
                iProgress.clearActions()
                description.clearActions()
            }
        }
        iProgress.addAction(Actions.scaleTo(progress, 1f, 0.5f))
    }
}
