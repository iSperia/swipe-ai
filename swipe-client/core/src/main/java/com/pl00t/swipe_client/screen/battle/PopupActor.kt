package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align

class PopupActor(
    private val _w: Float,
    private val _h: Float,
    private val text: String,
    private val icons: List<String>,
    private val iconsAtlas: TextureAtlas
) : Group() {

//    private val label = Fonts.createWhiteCaption(text, _h).apply {
//        width = _w
//        height = _h
//        setAlignment(Align.left)
//        x = _h * 0.5f * (icons.size + 1)
//    }

    init {
//        addActor(label)
        icons.forEachIndexed { index, icon ->
            iconsAtlas.findRegion(icon).let { iconTexture ->
                val image = Image(iconTexture)
                image.x = _h * 0.5f * index
                image.width = _h
                image.height = _h
                addActor(image)
            }
        }
    }
}
