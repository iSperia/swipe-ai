package com.pl00t.swipe_client.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.levels.DialogEntryModel
import com.pl00t.swipe_client.services.levels.DialogOrientation
import com.pl00t.swipe_client.ux.require
import ktx.actors.onClick

class BattleDialogActor(
    private val r: R,
    private val dialog: DialogEntryModel,
    private val onClose: () -> Unit
) : Group() {

    val unitImage: Image
    val dialogBackground: Image
    val dialogLabel: Label
    val titleLabel: Label

    init {
        unitImage = r.image(R.units_atlas, dialog.actor).apply {
            width = 400f
            height = 600f
            y = -100f
            x = if (dialog.side == DialogOrientation.left) -150f else r.width + 150f
            setScaling(Scaling.stretch)
            scaleX = if (dialog.side == DialogOrientation.left) 1f else -1f
        }
        dialogBackground = r.image(R.ux_atlas, "background_focus").apply {
            width = 300f
            height = 200f
            x = if (dialog.side == DialogOrientation.left) 160f else 30f
            y = 200f
        }
        titleLabel = r.regular24Error(dialog.title).apply {
            x = dialogBackground.x + 10f
            y = dialogBackground.y + 150f
            wrap = true
            width = 300f
            height = 40f
            setAlignment(Align.center)
        }
        dialogLabel = r.regular24Accent(dialog.text).apply {
            x = titleLabel.x + 20f
            width = titleLabel.width - 50f
            y = dialogBackground.y + 10f
            height = 140f
            setAlignment(Align.topLeft)
            wrap = true
        }

        addActor(unitImage)
        addActor(dialogBackground)
        addActor(titleLabel)
        addActor(dialogLabel)

        x = if (dialog.side == DialogOrientation.left) -240f else 240f
        addAction(Actions.moveBy(if (dialog.side == DialogOrientation.left) 240f else -240f, 0f, 0.6f))

        onClick {
            println("Dialog touch")
            this@BattleDialogActor.touchable = Touchable.disabled
            this@BattleDialogActor.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.moveBy(0f, -600f, 0.6f),
                    Actions.alpha(0f, 0.6f)
                ),
                Actions.removeActor()
            ))
            onClose()
        }
    }
}
