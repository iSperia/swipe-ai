package com.pl00t.swipe_client.action

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.R

class WindowTitleActor(
    private val r: R,
    private val text: String,
    private val actionLeft: ActionCompositeButton?,
    private val actionRight: ActionCompositeButton?,
    private val backgroundRarity: Int,
): Group() {

    val background = r.image(R.ux_atlas, "item_background", backgroundRarity).apply {
        width = 480f
        height = 80f
    }
    val backgroundShadow = r.image(R.ux_atlas, "background_transparent50").apply {
        width = 480f
        height = 80f
    }
    val topLine = r.image(R.ux_atlas, "background_white").apply {
        width = 480f
        height = 1f
        y = 79f
    }
    val bottomLine = r.image(R.ux_atlas, "background_white").apply {
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
