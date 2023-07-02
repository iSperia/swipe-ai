package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.services.levels.DialogEntryModel
import com.pl00t.swipe_client.services.levels.DialogOrientation
import ktx.actors.onClick

class BattleDialogActor(
    private val w: Float,
    private val h: Float,
    private val dialog: DialogEntryModel,
    val unitsAtlas: TextureAtlas,
    val coreAtlas: TextureAtlas,
    private val onClose: () -> Unit
) : Group() {

    val unitImage: Image
    val dialogBackground: Image
//    val dialogLabel: Label
//    val titleLabel: Label

    private val _unitWidth = w * 0.6f
    private val _unitHeight = _unitWidth * 1.66f
    private val _unitY = - _unitHeight / 4f
    private val _unitX = _unitWidth * 0.25f
    private val _bubbleWidth = w * 0.7f
    private val _bubbleHeight = _unitHeight / 3f
    private val _bubblePadding = w * 0.15f
    private val _titleHeight = _bubbleHeight * 0.27f

    init {
        unitImage = Image(unitsAtlas.findRegion(dialog.actor.toString())).apply {
            width = _unitWidth
            height = _unitHeight
            y = _unitY
            x = if (dialog.side == DialogOrientation.left) -_unitX else w + _unitX
            setScaling(Scaling.stretch)
            scaleX = if (dialog.side == DialogOrientation.left) 1f else -1f
        }
        dialogBackground = Image(coreAtlas.createPatch("dialog_bubble")).apply {
            width = _bubbleWidth
            height = _bubbleHeight
            x = if (dialog.side == DialogOrientation.left) w - _bubbleWidth else 0f
            y = _unitY + _unitHeight - _bubbleHeight
        }
//        titleLabel = Fonts.createDialogTitle(dialog.title, _titleHeight).apply {
//            x = dialogBackground.x + _bubbleWidth * 0.1f
//            y = dialogBackground.y + _bubbleHeight * 0.9f - _titleHeight
//            width = _bubbleWidth * 0.8f
//            height = _titleHeight
//            setAlignment(Align.center)
//            touchable = Touchable.disabled
//        }
//        dialogLabel = Fonts.createDialogText(dialog.text, _titleHeight * 0.8f).apply {
//            x = titleLabel.x
//            width = titleLabel.width
//            y = dialogBackground.y + _bubbleHeight * 0.05f
//            height = _bubbleHeight * 0.85f - titleLabel.height
//            setAlignment(Align.topLeft)
//            touchable = Touchable.disabled
//        }

        addActor(unitImage)
        addActor(dialogBackground)
//        addActor(titleLabel)
//        addActor(dialogLabel)

        x = if (dialog.side == DialogOrientation.left) -w / 2f else w / 2f
        addAction(Actions.moveBy(if (dialog.side == DialogOrientation.left) w / 2f else -w / 2f, 0f, 0.6f))

        dialogBackground.onClick {
            dialogBackground.touchable = Touchable.disabled
            this@BattleDialogActor.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.moveBy(0f, -h, 0.6f),
                    Actions.alpha(0f, 0.6f)
                ),
                Actions.removeActor()
            ))
            onClose()
        }
    }
}
