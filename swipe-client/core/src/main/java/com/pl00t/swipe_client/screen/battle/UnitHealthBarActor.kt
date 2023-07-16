package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.ux.require

class UnitHealthBarActor(
    val w: Float,
    val h: Float,
    val context: SwipeContext,
    val skin: Skin,
    health: Int,
    maxHealth: Int
): Group() {

    val background: Image = Image(context.commonAtlas(Atlases.COMMON_BATTLE).createPatch("healthbar_bg")).apply {
        width = w
        height = h
        setScaling(Scaling.stretch)
    }
    val foregound: Image = Image(context.commonAtlas(Atlases.COMMON_BATTLE).findRegion("healthbar_fg").require()).apply {
        width = w - 14f
        height = h - 14f
        setScaling(Scaling.stretch)
        x = 7f
        y = 7f
    }
    val healthText: Label = Label("$health", skin, "text_small").apply {
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

    fun updateHealth(health: Int) = updateHealth(health, this.maxHealth)

    fun updateHealth(health: Int, maxHealth: Int) {
        this.health = health
        this.maxHealth = maxHealth
        val progress = health.toFloat() / maxHealth
        foregound.addAction(SizeToAction().apply {
            setSize(_maxWidth * progress, foregound.height)
            duration = 1f
        })
        healthText.setText(health.toString())
    }
}
