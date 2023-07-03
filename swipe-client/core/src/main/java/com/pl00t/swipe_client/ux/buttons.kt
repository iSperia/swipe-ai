package com.pl00t.swipe_client.ux

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

object Buttons {

    fun createActionButton(
        text: String,
        skin: Skin
    ): TextButton {
        val button = TextButton(text, skin).apply {
            width = 170f
            height = 36f
            pad(5f)
            padBottom(8f)
        }
        return button
    }
}