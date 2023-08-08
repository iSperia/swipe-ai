package com.pl00t.swipe_client.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources

class UnitHealthBarActor(
    private val r: Resources,
    val w: Float,
    val h: Float,
    health: Int,
    maxHealth: Int
): Group() {

    val background: Image = Image(r.atlas(Resources.battle_atlas).createPatch("healthbar_bg")).apply {
        width = w
        height = h
        setScaling(Scaling.stretch)
    }
    val foregound: Image = r.image(Resources.battle_atlas, "healthbar_fg").apply {
        width = w - 14f
        height = h - 14f
        setScaling(Scaling.stretch)
        x = 7f
        y = 7f
    }
    val healthText: Label = r.regular24White(health.toString()).apply {
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
        if (this.health == health && this.maxHealth == maxHealth) return
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
