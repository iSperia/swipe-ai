package com.pl00t.swipe_client.screen.navpanel

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align

class NavigationButton(
    private val style: String,
    private val label: String,
    private val skin: Skin,
) : TextButton(label, skin.get(style, TextButtonStyle::class.java)) {

    init {
        this.getLabel().setAlignment(Align.bottom)
    }

}
