package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.battle.BattleDialogActor
import com.pl00t.swipe_client.services.battle.logic.Character
import com.pl00t.swipe_client.services.battle.logic.CharacterAttributes
import ktx.actors.alpha
import ktx.actors.onClick

class AttributeActor(
    private var attributes: CharacterAttributes,
    private val mode: Mode,
    private val context: SwipeContext,
    private val skin: Skin,
) : Group() {

    enum class Mode {
        PERCENT, ABSOLUTE
    }

    private val valueBody: Label
    private val valueMind: Label
    private val valueSpirit: Label

    private val dialogGroup: Group
    private val dialogLabel: Label

    private val triangleGroup: Group

    init {
        width = 356f
        height = 396f
        val suffix = when (mode) {
            Mode.PERCENT -> "%"
            Mode.ABSOLUTE -> ""
        }
        triangleGroup = Group().apply {
            width = 356f
            height = 396f
        }
        valueBody = Label("Body: ${attributes.body}$suffix", skin, "damage_popup").apply {
            x = 128f
            width = 100f
            height = 20f
            setAlignment(Align.center)
            y = 376f
        }
        valueMind = Label("Mind: ${attributes.mind}$suffix", skin, "damage_popup").apply {
            x = 256f
            width = 100f
            setAlignment(Align.center)
            y = 0f
            height = 20f
        }
        valueSpirit = Label("Spirit: ${attributes.spirit}$suffix", skin, "damage_popup").apply {
            x = 0f
            width = 100f
            setAlignment(Align.center)
            y = 0f
            height = 20f
        }
        val iconBody = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("attribute_body")).apply {
            width = 100f
            height = 100f
            setScaling(Scaling.stretch)
            x = 128f
            y = 276f
        }
        val iconSpirit = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("attribute_spirit")).apply {
            width = 100f
            height = 100f
            setScaling(Scaling.stretch)
            x = 0f
            y = 20f
        }
        val iconMind = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("attribute_mind")).apply {
            width = 100f
            height = 100f
            setScaling(Scaling.stretch)
            x = 258f
            y = 20f
        }
        val triangle = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("triangle_attributes")).apply {
            x = 50f
            y = 70f
            width = 256f
            height = 256f
            setScaling(Scaling.stretch)
        }

        dialogGroup = Group().apply {
            x = 25f
            y = 128f
        }

        val dialogBackground = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_bg")).apply {
            width = 306f
            height = 140f
        }
        dialogLabel = Label("BODY\nPhysical qualities of character. Health is increased by 10% per BODY. Typically scales physical and fire damage", skin, "lore_medium").apply {
            setAlignment(Align.center)
            width = 286f
            height = 120f
            x = 10f
            y = 10f
            wrap = true
        }

        dialogGroup.addActor(dialogBackground)
        dialogGroup.addActor(dialogLabel)
        dialogGroup.isVisible = false
        dialogGroup.alpha = 0f


        triangleGroup.addActor(triangle)
        triangleGroup.addActor(iconMind)
        triangleGroup.addActor(iconSpirit)
        triangleGroup.addActor(iconBody)
        triangleGroup.addActor(valueBody)
        triangleGroup.addActor(valueMind)
        triangleGroup.addActor(valueSpirit)

        addActor(triangleGroup)
        addActor(dialogGroup)

        dialogGroup.onClick {
            dialogGroup.isVisible = false
            dialogGroup.alpha = 0f
        }
        iconBody.onClick {
            dialogLabel.setText("BODY\nPhysical strength and vitality. Health is increased by 10% per BODY. Typically scales physical and fire damage")
            dialogGroup.isVisible = true
            animateDialogGroup(25f, 158f)
        }
        iconSpirit.onClick {
            dialogLabel.setText("SPIRIT\nMental qualities and morale. Crit. chance is increased by 5% per SPIRIT. Typically scales dark and light damage")
            dialogGroup.isVisible = true
            animateDialogGroup(5f, 108f)
        }
        iconMind.onClick {
            dialogLabel.setText("MIND\nKnowledge and wisdom. Ultimate fills 5% faster per MIND. Typically scales shock and cold damage")
            dialogGroup.isVisible = true
            animateDialogGroup(45f, 108f)
        }
    }

    private fun animateDialogGroup(tx: Float, ty: Float) {
        dialogGroup.addAction(
            Actions.parallel(
                Actions.alpha(1f, 1f),
                Actions.moveTo(tx, ty, 1f)
            )
        )
    }

    fun updateAttributes(attributes: CharacterAttributes) {
        this.attributes = attributes
        valueMind.setText("Mind: ${attributes.mind}")
        valueBody.setText("Body: ${attributes.body}")
        valueSpirit.setText("Spirit: ${attributes.spirit}")
    }
}
