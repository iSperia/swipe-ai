package com.pl00t.swipe_client.ux

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Align

fun createButton(
    width: Float,
    height: Float,
    text: String,
    uxAtlas: TextureAtlas,
    amCore: AssetManager
): TextButton {

    val textButton = TextButton(text, TextButton.TextButtonStyle(
        NinePatchDrawable(uxAtlas.createPatch("button_simple")),
        NinePatchDrawable(uxAtlas.createPatch("button_pressed")),
        NinePatchDrawable(uxAtlas.createPatch("button_simple")),
        amCore.fntDescription()
    ).apply {
        this.fontColor = Color.WHITE
        this.downFontColor = Colors.RARITY_3
    }).apply {
        this.width = width
        this.height = height
        this.labelCell.padBottom(height * 0.08f).expandX()
        this.label.setFontScale(height * 0.016f, height * 0.016f)
    }

    return textButton
}
