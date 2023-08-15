package com.pl00t.swipe_client.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import ktx.actors.alpha
import kotlin.math.max
import kotlin.math.min

class UnitHealthBarActor(
    private val r: Resources,
    val w: Float,
    val h: Float,
    health: Int,
    maxHealth: Int
): Group() {

    val background: Image = r.image(Resources.ux_atlas, "background_error").apply {
        width = w
        height = h
        alpha = 0.5f
    }
    val shadow: Image = r.image(Resources.ux_atlas, "background_black").apply {
        width = w
        height = h
        alpha = 0.5f
    }
    val foregound: Image = r.image(Resources.ux_atlas, "background_error").apply {
        width = w - 4f
        height = h - 4f
        x = 2f
        y = 2f
    }
    val healthText: Label = r.regularWhite(health.toString()).apply {
        width = w
        height = h
        setAlignment(Align.center)
    }

    private var health = health
    private var maxHealth = maxHealth
    val _maxWidth = w - 14f

    init {
        setSize(w, h)
        addActor(background)
        addActor(foregound)
        addActor(shadow)
        addActor(healthText)
        updateHealth(health, maxHealth)
    }

    fun updateHealth(health: Int) = updateHealth(health, this.maxHealth)

    fun updateHealth(health: Int, maxHealth: Int) {
        if (this.health == health && this.maxHealth == maxHealth) return
        this.health = min(maxHealth, max(0, health))
        this.maxHealth = maxHealth
        val progress = health.toFloat() / maxHealth
        foregound.addAction(SizeToAction().apply {
            setSize(_maxWidth * progress, foregound.height)
            duration = 1f
        })
        healthText.setText(health.toString())
    }
}
