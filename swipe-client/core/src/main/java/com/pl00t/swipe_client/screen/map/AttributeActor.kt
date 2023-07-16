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
        width = 256f
        height = 320f
        val suffix = when (mode) {
            Mode.PERCENT -> "%"
            Mode.ABSOLUTE -> ""
        }
        triangleGroup = Group().apply {
            width = 256f
            height = 356f
        }
        val triangle = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("triangle_attributes")).apply {
            y = 30f
            width = 256f
            height = 256f
            setScaling(Scaling.stretch)
        }
        val iconBody = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("attribute_body")).apply {
            width = 100f
            height = 100f
            setScaling(Scaling.stretch)
            x = 127f - 50f + triangle.x
            y = 227f - 50f + triangle.y
        }
        valueBody = Label("Body: ${attributes.body}$suffix", skin, "damage_popup").apply {
            x = iconBody.x
            width = 100f
            height = 20f
            setAlignment(Align.center)
            y = iconBody.y + 100f
        }

        val iconSpirit = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("attribute_spirit")).apply {
            width = 100f
            height = 100f
            setScaling(Scaling.stretch)
            x = 44f - 50f + triangle.x
            y = 78f - 50f + triangle.y
        }
        valueSpirit = Label("Spirit: ${attributes.spirit}$suffix", skin, "damage_popup").apply {
            x = iconSpirit.x
            width = 100f
            setAlignment(Align.center)
            y = iconSpirit.y - 20f
            height = 20f
        }


        val iconMind = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("attribute_mind")).apply {
            width = 100f
            height = 100f
            setScaling(Scaling.stretch)
            x = 215 - 50f + triangle.x
            y = 78 - 50f + triangle.y
        }
        valueMind = Label("Mind: ${attributes.mind}$suffix", skin, "damage_popup").apply {
            x = iconMind.x
            width = 100f
            setAlignment(Align.center)
            y = iconMind.y - 20f
            height = 20f
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
            width = 256f
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
            animateDialogGroup(0f, iconBody.y - 120f)
        }
        iconSpirit.onClick {
            dialogLabel.setText("SPIRIT\nMental qualities and morale. Crit. chance is increased by 5% per SPIRIT. Typically scales dark and light damage")
            dialogGroup.isVisible = true
            animateDialogGroup(0f, iconMind.y + 100f)
        }
        iconMind.onClick {
            dialogLabel.setText("MIND\nKnowledge and wisdom. Ultimate fills 5% faster per MIND. Typically scales shock and cold damage")
            dialogGroup.isVisible = true
            animateDialogGroup(0f, iconMind.y + 100f)
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
