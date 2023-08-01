package com.pl00t.swipe_client.action

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import ktx.actors.alpha
import ktx.actors.onExit
import ktx.actors.onTouchDown
import ktx.actors.onTouchEvent

enum class Action(val icon: String) {
    VAULT("action_icon_vault"),
    SETTINGS("action_icon_settings"),
    CLOSE("action_icon_close"),
    ATTACK("action_icon_attack"),
}

sealed interface Mode {
    object NoText: Mode
    data class SingleLine(val text: String): Mode
    data class Purchase(val text: String, val currency: SwipeCurrency, val amount: Int): Mode
}

class ActionCompositeButton(
    private val r: R,
    private val action: Action,
    private val mode: Mode
): Group() {

    private val icon = r.image(R.ux_atlas, action.icon).apply { touchable = Touchable.disabled }
    private val label = r.labelAction(getLabelText()).apply { setAlignment(Align.center); touchable = Touchable.disabled }
    private val background = r.image(R.ux_atlas, "background_transparent50")

    var labelY = 0f

    init {
        background.alpha = 0f
        addActor(background)
        addActor(icon)
        addActor(label)

        label.isVisible = mode != Mode.NoText

        onTouchDown {
            icon.addAction(Actions.scaleTo(0.8f, 0.8f, 0.3f))
            background.addAction(Actions.alpha(1f, 0.3f))
            label.addAction(Actions.moveTo(0f, labelY + 5f, 0.3f))
        }
        onExit {
            icon.addAction(Actions.scaleTo(1f, 1f, 0.3f))
            background.addAction(Actions.alpha(0f, 0.3f))
            label.addAction(Actions.moveTo(0f, labelY, 0.3f))
        }
    }

    override fun sizeChanged() {
        super.sizeChanged()
        val w = this@ActionCompositeButton.width
        val h = this@ActionCompositeButton.height
        val btnSize = w * 0.65f
        val padding = w * 0.04f
        val freeHeight = h - btnSize - 2 * padding
        val shift = when (mode) {
            is Mode.SingleLine -> freeHeight / 2f
            else -> 0f
        }
        icon.apply {
            this.width = btnSize
            this.height = btnSize
            y = when (mode) {
                is Mode.NoText -> (h - btnSize) / 2f
                else -> h - btnSize - padding - shift
            }
            x = (w - btnSize) / 2f
            setOrigin(Align.center)
        }
        label.apply {
            this.width = w
            this.height = when (mode) {
                is Mode.SingleLine -> freeHeight
                is Mode.Purchase -> freeHeight / 2f
                else -> 0f
            }
            this.y = (freeHeight / 2f - shift).also { labelY = it }
            wrap = true
        }
        background.apply {
            this.width = w
            this.height = h
            setOrigin(Align.center)
        }
    }

    private fun getLabelText() = when (mode) {
        is Mode.NoText -> ""
        is Mode.SingleLine -> mode.text
        is Mode.Purchase -> mode.text
    }
}
