package com.pl00t.swipe_client.action

import com.badlogic.gdx.scenes.scene2d.Group
import com.pl00t.swipe_client.R

class BottomActionPanel(
    private val r: R,
    private val actions: List<ActionCompositeButton>,
    private val backgroundRarity: Int,
) : Group() {

    private val background = r.image(R.ux_atlas, "item_background", backgroundRarity).apply {
        setSize(480f, 110f)
    }
    private val shadow = r.image(R.ux_atlas, "background_transparent50").apply { setSize(480f, 110f) }
    private val topLine = r.image(R.ux_atlas, "background_white").apply {
        width = 480f
        height = 1f
        y = 109f
    }

    init {
        setSize(480f, 110f)
        addActor(background)
        addActor(shadow)

        addActions()

        addActor(topLine)
    }

    private fun addActions() {
        val shift = (r.width - actions.size * 80f) / 2f
        actions.forEachIndexed { index, actionButton ->
            actionButton.apply {
                width = 80f
                height = 110f
                x = r.width - (index + 1) * this.width - shift
            }.let {
                addActor(it)
            }
        }
    }
}
