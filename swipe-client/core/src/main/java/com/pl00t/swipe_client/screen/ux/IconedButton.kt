package com.pl00t.swipe_client.screen.ux

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.pl00t.swipe_client.ux.Fonts

class IconedButton(
    val w: Float,
    val h: Float,
    val text: String,
    val icon: String,
    val coreTextureAtlas: TextureAtlas,
    val textureAtlas: TextureAtlas,
) : Group() {

    val background = Image(coreTextureAtlas.findRegion("button_bg"))
    val iconImage = Image(textureAtlas.findRegion(icon))
    val captionText = Fonts.createWhiteCaption(text, h)

    init {
        background.apply {
            width = w
            height = h
        }
        iconImage.apply {
            width = h
            height = h
        }
        captionText.apply {
            width = w - h
            height = h
            x = h
        }
        addActor(background)
        addActor(iconImage)
        addActor(captionText)
    }
}
