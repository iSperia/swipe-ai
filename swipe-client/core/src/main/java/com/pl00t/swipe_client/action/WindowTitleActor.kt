package com.pl00t.swipe_client.action

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import ktx.actors.alpha

class WindowTitleActor(
    private val r: Resources,
    private val text: String,
    private val actionLeft: ActionCompositeButton?,
    private val actionRight: ActionCompositeButton?,
    private val backgroundRarity: Int,
): Group() {

    val texture = r.image(Resources.ux_atlas, "texture_panel").apply {
        width = 480f
        height = 80f
        setScaling(Scaling.fillY)
    }
    val background = r.image(Resources.ux_atlas, "item_background", backgroundRarity).apply {
        width = 480f
        height = 80f
        alpha = 0.4f
    }
    val backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply {
        width = 480f
        height = 80f
    }
    val topLine = r.image(Resources.ux_atlas, "background_black").apply {
        width = 480f
        height = 1f
        y = 79f
    }
    val bottomLine = r.image(Resources.ux_atlas, "background_black").apply {
        width = 480f
        height = 1f
    }

    val textLabel = r.labelWindowTitle(text).apply {
        width = background.width - 160f
        height = background.height
        x = 80f
        wrap = true
        setAlignment(Align.center)
    }

    init {
        width = background.width
        height = background.height
        addActor(texture)
        addActor(background)
        addActor(backgroundShadow)
        addActor(textLabel)

        actionLeft?.let {
            addActor(it)
        }
        actionRight?.let {
            it.x = this@WindowTitleActor.width - actionRight.width
            addActor(it)
        }


        addActor(bottomLine)
        addActor(topLine)
    }
}
