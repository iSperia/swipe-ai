package com.pl00t.swipe_client.ux

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle

fun AssetManager.fntTitle() = get("fonts/cinzel.fnt", BitmapFont::class.java)
fun AssetManager.fntDescription() = get("fonts/notepad.fnt", BitmapFont::class.java)

object Fonts {
    lateinit var lsWindowTitle: LabelStyle
    lateinit var lsTitle: LabelStyle
    lateinit var lsDescription: LabelStyle
    lateinit var lsDescriptionAccent: LabelStyle
    lateinit var lsWhiteCaption: LabelStyle
    lateinit var lsWhiteTitle: LabelStyle

    fun init(amCore: AssetManager) {
        lsWindowTitle = LabelStyle(amCore.fntTitle(), Colors.ACCENT_COLOR)
        lsTitle = LabelStyle(amCore.fntTitle(), Colors.MAIN_COLOR)
        lsDescription = LabelStyle(amCore.fntDescription(), Colors.MAIN_COLOR)
        lsDescriptionAccent = LabelStyle(amCore.fntDescription(), Colors.ACCENT_COLOR)
        lsWhiteCaption = LabelStyle(amCore.fntDescription(), Color.WHITE)
        lsWhiteTitle = LabelStyle(amCore.fntTitle(), Color.WHITE)
    }

    fun createWindowTitle(text: String, lineHeight: Float): Label = Label(text, lsWindowTitle).apply {
        this.fontScaleX = lineHeight * 0.016f
        this.fontScaleY = lineHeight * 0.016f
    }

    fun createCaption(text: String, lineHeight: Float): Label = Label(text, lsDescription).apply {
        this.fontScaleX = lineHeight * 0.016f
        this.fontScaleY = lineHeight * 0.016f
    }

    fun createCaptionAccent(text: String, lineHeight: Float): Label = Label(text, lsDescriptionAccent).apply {
        this.fontScaleX = lineHeight * 0.016f
        this.fontScaleY = lineHeight * 0.016f
    }

    fun createWhiteTitle(text: String, lineHeight: Float): Label = Label(text, lsWhiteTitle).apply {
        this.fontScaleX = lineHeight * 0.016f
        this.fontScaleY = lineHeight * 0.016f
    }
    fun createWhiteCaption(text: String, lineHeight: Float): Label = Label(text, lsWhiteCaption).apply {
        this.fontScaleX = lineHeight * 0.016f
        this.fontScaleY = lineHeight * 0.016f
    }
}
