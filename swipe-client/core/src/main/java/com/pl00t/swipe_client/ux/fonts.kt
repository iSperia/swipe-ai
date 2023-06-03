package com.pl00t.swipe_client.ux

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle

fun AssetManager.fntTitle() = get("fonts/cinzel.fnt", BitmapFont::class.java)
fun AssetManager.fntDescription() = get("fonts/notepad.fnt", BitmapFont::class.java)

object Fonts {
    lateinit var lsWindowTitle: LabelStyle
    lateinit var lsTitle: LabelStyle
    lateinit var lsDescription: LabelStyle

    fun init(amCore: AssetManager) {
        lsWindowTitle = LabelStyle(amCore.fntTitle(), Colors.ACCENT_COLOR)
        lsTitle = LabelStyle(amCore.fntTitle(), Colors.MAIN_COLOR)
        lsDescription = LabelStyle(amCore.fntDescription(), Colors.MAIN_COLOR)
    }

    fun createWindowTitle(text: String, lineHeight: Float): Label = Label(text, lsWindowTitle).apply {
        this.width = width
        this.height = height
        this.fontScaleX = lineHeight * 0.014f
        this.fontScaleY = lineHeight * 0.014f
    }
}
