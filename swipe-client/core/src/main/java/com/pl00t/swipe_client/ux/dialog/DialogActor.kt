package com.pl00t.swipe_client.ux.dialog

import com.badlogic.gdx.math.Interpolation.SwingIn
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.services.levels.DialogEntryModel
import com.pl00t.swipe_client.services.levels.DialogOrientation
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class DialogActor(
    private val r: Resources,
    private val dialog: DialogEntryModel,
    private val onClose: () -> Unit
) : Group() {

    lateinit var unitImage: Image
    lateinit var dialogBackground: Image
    lateinit var dialogLabel: Label
    lateinit var titleLabel: Label

    init {
        KtxAsync.launch {
            val monster = r.monsterService.getMonster(dialog.skin)!!
            unitImage = r.image(Resources.units_atlas, monster.skin).apply {
                width = 400f
                height = 600f
                y = -100f
                x = if (dialog.side == DialogOrientation.left) -150f else r.width + 150f
                setScaling(Scaling.stretch)
                scaleX = if (dialog.side == DialogOrientation.left) 1f else -1f
            }
            dialogBackground = r.image(Resources.ux_atlas, "background_black").apply {
                width = 320f
                height = 250f
                x = if (dialog.side == DialogOrientation.left) 110f else 30f
                y = 200f
                alpha = 0.8f
            }
            titleLabel = r.regular24Focus(monster.name.value(r.l)).apply {
                x = dialogBackground.x + 10f
                y = dialogBackground.y + dialogBackground.height - 30f
                wrap = true
                width = dialogBackground.width - 20f
                height = 30f
                setAlignment(Align.center)
            }
            dialogLabel = r.regular24White(dialog.text.value(r.l)).apply {
                x = titleLabel.x
                width = titleLabel.width
                y = dialogBackground.y + 10f
                height = dialogBackground.height - 45f
                setAlignment(Align.topLeft)
                wrap = true
            }

            addActor(dialogBackground)
            addActor(unitImage)
            addActor(titleLabel)
            addActor(dialogLabel)

            x = if (dialog.side == DialogOrientation.left) -240f else 240f
            addAction(
                Actions.parallel(
                    Actions.alpha(1f, 0.4f),
                    Actions.moveBy(if (dialog.side == DialogOrientation.left) 240f else -240f, 0f, 0.4f, SwingOut(1.6f)))
                )


            onClick {
                println("Dialog touch")
                this@DialogActor.touchable = Touchable.disabled
                this@DialogActor.addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.moveBy(if (dialog.side == DialogOrientation.left) -240f else 240f, 0f, 0.4f, SwingIn(1.6f)),
                        Actions.alpha(0f, 0.4f)
                    ),
                    Actions.removeActor()
                ))
                onClose()
            }
        }
    }
}
