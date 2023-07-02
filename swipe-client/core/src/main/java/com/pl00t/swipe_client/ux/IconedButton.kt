package com.pl00t.swipe_client.ux

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align

class IconedButton(
    val w: Float,
    val h: Float,
    val text: String,
    val icon: String,
    val coreTextureAtlas: TextureAtlas,
    val textureAtlas: TextureAtlas,
    val align: Int = Align.right,
) : Group() {

    val background = Image(coreTextureAtlas.createPatch("button_simple"))
    val iconImage = Image(textureAtlas.findRegion(icon))
//    val captionText = Fonts.createWhiteCaption(text, h)
    val padding = h * 0.1f

    init {
        background.apply {
            width = w
            height = h
        }
        iconImage.apply {
            width = h
            height = h
            x = if (this@IconedButton.align == Align.left) 0f else w - h
        }
//        captionText.apply {
//            width = w - h - padding
//            height = h
//            setAlignment(align)
//            x = if (align == Align.left) h + padding else 0f
//        }
        addActor(background)
        addActor(iconImage)
//        addActor(captionText)
    }

    fun updateText(text: String) {
//        captionText.setText(text)
    }
}
