package com.pl00t.swipe_client.action

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import ktx.actors.alpha

class BottomActionPanel(
    private val r: Resources,
    private val actions: List<ActionCompositeButton>,
    private val backgroundRarity: Int,
) : Group() {

    private val texture = r.image(Resources.ux_atlas, "texture_panel").apply {
        setSize(480f, 110f)
        setScale(1f, -1f)
        y = 110f
        setScaling(Scaling.fillY)
    }
    private val background = r.image(Resources.ux_atlas, "item_background", backgroundRarity).apply {
        setSize(480f, 110f)
        alpha = 0.5f
    }
    private val shadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(480f, 110f) }
    private val topLine = r.image(Resources.ux_atlas, "background_black").apply {
        width = 480f
        height = 1f
        y = 109f
    }

    init {
        setSize(480f, 110f)
        addActor(texture)
        addActor(background)
        addActor(shadow)

        addActions()

        addActor(topLine)
    }

    private fun addActions() {
        val sizePerAction = r.width / actions.size
        actions.forEachIndexed { index, actionButton ->
            actionButton.apply {
                setSize(sizePerAction, 110f)
                x = index * this.width
            }.let {
                addActor(it)
            }
        }
    }
}
