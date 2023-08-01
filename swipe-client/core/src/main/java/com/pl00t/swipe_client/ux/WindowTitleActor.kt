package com.pl00t.swipe_client.ux

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.R

class WindowTitleActor(
    private val r: R,
    private val text: String
): Group() {

    val background = r.image(R.ux_atlas, "background_transparent50").apply {
        width = 480f
        height = 80f
    }

    val textLabel = r.windowTitle(text).apply {
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
        addActor(textLabel)
    }
}
