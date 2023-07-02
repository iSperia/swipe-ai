package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.levels.DialogEntryModel
import com.pl00t.swipe_client.services.levels.DialogOrientation
import ktx.actors.onClick

class BattleDialogActor(
    private val dialog: DialogEntryModel,
    private val context: SwipeContext,
    private val skin: Skin,
    private val onClose: () -> Unit
) : Group() {

    val unitImage: Image
    val dialogBackground: Image
    val dialogLabel: Label
    val titleLabel: Label

    init {
        unitImage = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion(dialog.actor.toString())).apply {
            width = 400f
            height = 600f
            y = -100f
            x = if (dialog.side == DialogOrientation.left) -150f else context.width() + 150f
            setScaling(Scaling.stretch)
            scaleX = if (dialog.side == DialogOrientation.left) 1f else -1f
        }
        dialogBackground = Image(context.commonAtlas(Atlases.COMMON_UX).createPatch("dialog_bubble")).apply {
            width = 300f
            height = 200f
            x = if (dialog.side == DialogOrientation.left) 160f else 30f
            y = 200f
        }
        titleLabel = Label(dialog.title, skin, "wave_caption").apply {
            x = dialogBackground.x + 10f
            y = dialogBackground.y + 150f
            wrap = true
            width = 300f
            height = 40f
            setAlignment(Align.center)
            touchable = Touchable.disabled
        }
        dialogLabel = Label(dialog.text, skin, "text_regular").apply {
            x = titleLabel.x + 20f
            width = titleLabel.width - 50f
            y = dialogBackground.y + 10f
            height = 140f
            setAlignment(Align.topLeft)
            wrap = true
            touchable = Touchable.disabled
        }

        addActor(unitImage)
        addActor(dialogBackground)
        addActor(titleLabel)
        addActor(dialogLabel)

        x = if (dialog.side == DialogOrientation.left) -240f else 240f
        addAction(Actions.moveBy(if (dialog.side == DialogOrientation.left) 240f else -240f, 0f, 0.6f))

        dialogBackground.onClick {
            dialogBackground.touchable = Touchable.disabled
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
