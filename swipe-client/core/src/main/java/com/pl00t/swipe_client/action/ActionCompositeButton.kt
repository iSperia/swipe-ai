package com.pl00t.swipe_client.action

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import ktx.actors.alpha
import ktx.actors.onExit
import ktx.actors.onTouchDown

sealed class Action(val atlas: String, val icon: String) {
    object Vault: Action(Resources.ux_atlas, "action_icon_vault")
    object Stash: Action(Resources.ux_atlas, "action_stash")
    object Shop: Action(Resources.ux_atlas, "action_shop")
    object Party: Action(Resources.ux_atlas, "action_party")
    object Settings: Action(Resources.ux_atlas, "action_icon_settings")
    object Close: Action(Resources.ux_atlas, "action_icon_close")
    object FilterCurrency: Action(Resources.ux_atlas, "filter_currency")
    object FilterHelmet: Action(Resources.ux_atlas, "silh_helm")
    object FilterGloves: Action(Resources.ux_atlas, "silh_gauntlets")
    object FilterBoots: Action(Resources.ux_atlas, "silh_boots")
    object FilterAmulet: Action(Resources.ux_atlas, "silh_amulet")
    object FilterRing: Action(Resources.ux_atlas, "silh_ring")
    object FilterBelt: Action(Resources.ux_atlas, "silh_belt")
    object Complete: Action(Resources.ux_atlas, "fg_complete")
    object Attack: Action(Resources.ux_atlas, "action_icon_attack")
    object Stats: Action(Resources.ux_atlas, "action_icon_stats")
    object Attributes: Action(Resources.ux_atlas, "action_icon_attributes")
    object Resistance: Action(Resources.ux_atlas, "action_icon_defence")
    object Skillset: Action(Resources.ux_atlas, "action_icon_skillset")
    object Story: Action(Resources.ux_atlas, "action_icon_story")

    object Tarot: Action(Resources.ux_atlas, "action_tarot")
    object Equipment: Action(Resources.ux_atlas, "action_equipment")
    object LevelUp: Action(Resources.ux_atlas, "action_level_up")

    class SkillDetails(skin: String): Action(Resources.skills_atlas, skin)
    class ItemDetails(skin: String): Action(Resources.ux_atlas, skin)
}

sealed interface Mode {
    object NoText: Mode
    data class SingleLine(val text: String): Mode
    data class Purchase(val text: String, val currency: SwipeCurrency, val amount: Int): Mode
}

class ActionCompositeButton(
    private val r: Resources,
    private val action: Action,
    private val mode: Mode
): Group() {

    private val icon = r.image(action.atlas, action.icon).apply { touchable = Touchable.disabled }
    private val iconShadow = r.image(action.atlas, action.icon).apply { touchable = Touchable.disabled; color = Color.BLACK; setOrigin(Align.center); setScale(1.1f); alpha = 0.5f }
    private val label = r.labelAction(getLabelText()).apply { setAlignment(Align.center); touchable = Touchable.disabled }
    private val background = r.image(Resources.ux_atlas, "background_transparent50")

    var labelY = 0f

    init {
        background.alpha = 0f
        addActor(background)
        addActor(iconShadow)
        addActor(icon)
        addActor(label)

        label.isVisible = mode != Mode.NoText

        onTouchDown {
            clickedDown()
        }
        onExit {
            clickedUp()
        }
    }

    private fun clickedUp() {
        icon.addAction(Actions.scaleTo(1f, 1f, 0.3f))
        background.addAction(Actions.alpha(0f, 0.3f))
        label.addAction(Actions.moveTo(0f, labelY, 0.3f))
    }

    private fun clickedDown() {
        icon.addAction(Actions.scaleTo(0.8f, 0.8f, 0.3f))
        background.addAction(Actions.alpha(1f, 0.3f))
        label.addAction(Actions.moveTo(0f, labelY + 5f, 0.3f))
    }

    override fun sizeChanged() {
        super.sizeChanged()
        val w = this@ActionCompositeButton.width
        val h = this@ActionCompositeButton.height
        val btnSize = 50f
        val paddingHor = (w - btnSize) / 2f
        val paddingVer = 15f
        val freeHeight = h - btnSize - paddingVer
        icon.apply {
            this.width = btnSize
            this.height = btnSize
            y = when (mode) {
                is Mode.NoText -> (h - btnSize) / 2f
                else -> h - btnSize - paddingVer
            }
            x = (w - btnSize) / 2f
            setOrigin(Align.center)
        }
        iconShadow.apply {
            setSize(btnSize, btnSize)
            setPosition(icon.x, icon.y)
            setOrigin(Align.center)
            setScale(1.2f)
        }
        label.apply {
            this.width = w
            this.height = when (mode) {
                is Mode.SingleLine -> freeHeight
                is Mode.Purchase -> freeHeight
                else -> 0f
            }
            wrap = true
            this.setAlignment(Align.top)
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
        is Mode.Purchase -> "${mode.amount}\n${mode.text}"
    }
}
