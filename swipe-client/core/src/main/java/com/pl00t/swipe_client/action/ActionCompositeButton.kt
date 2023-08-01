package com.pl00t.swipe_client.action

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.R

class ActionCompositeButton(
    private val r: R
): Group() {

    private val icon = r.image(R.ux_atlas, "action_icon_settings")
    private val label = r.smallLabel("Настройки").apply { setAlignment(Align.center) }

    init {
        addActor(icon)
        addActor(label)
    }

    override fun sizeChanged() {
        super.sizeChanged()
        icon.apply {
            this.width = this@ActionCompositeButton.width - 20f
            this.height = this@ActionCompositeButton.width - 20f
            y = this@ActionCompositeButton.height - this@ActionCompositeButton.width + 20f
            x = 10f
        }
        label.apply {
            this.width = this@ActionCompositeButton.width
            this.height = icon.y
        }
    }
}
