package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.ux.Fonts

class UnitHealthBarActor(
    val w: Float,
    val h: Float,
    val taBattle: TextureAtlas,
    health: Int,
    maxHealth: Int
): Group() {

    val background: Image = Image(taBattle.createPatch("healthbar_bg")).apply {
        println("w=$w")
        width = w
        height = h
        setScaling(Scaling.stretch)
    }
    val foregound: Image = Image(taBattle.findRegion("healthbar_fg")).apply {
        width = w - 14f
        height = h - 14f
        setScaling(Scaling.stretch)
        x = 7f
        y = 7f
    }
    val healthText: Label = Fonts.createWhiteCaption("$health", h).apply {
        width = w
        height = h
        setAlignment(Align.center)
    }

    private var health = health
    private var maxHealth = maxHealth
    val _maxWidth = w - 14f

    init {
        addActor(background)
        addActor(foregound)
        addActor(healthText)
        updateHealth(health, maxHealth)
    }

    fun updateHealth(health: Int, maxHealth: Int) {
        this.health = health
        this.maxHealth = maxHealth
        val progress = health.toFloat() / maxHealth
        foregound.width = _maxWidth * progress
        foregound.addAction(SizeToAction().apply {
            setSize(_maxWidth * progress, foregound.height)
            duration = 0.1f
        })
        healthText.setText(health.toString())
    }
}
