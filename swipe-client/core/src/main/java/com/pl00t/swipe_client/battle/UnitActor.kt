package com.pl00t.swipe_client.battle

import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction
import com.badlogic.gdx.scenes.scene2d.actions.RotateByAction
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.game7th.swipe.game.SbCharacterDisplayEffect
import com.pl00t.swipe_client.Resources
import ktx.actors.along
import ktx.actors.repeatForever
import ktx.actors.then

class UnitActor(
    private val r: Resources,
    val id: Int,
    health: Int,
    maxHealth: Int,
    var effects: List<SbCharacterDisplayEffect>,
    val texture: String,
    val team: Int,
    val w: Float,//character width
    val s: Float,//character scale
    val position: Int,
) : Group() {

    val characterImage: Image
    val healthBar: UnitHealthBarActor
    private var health: Int = health
    private var maxHealth: Int = maxHealth

    var popupDelay: Float = 0f

    init {
        characterImage = r.image(Resources.units_atlas, texture).apply {
            this.scaleX = if (team == 0) 1f else -1f
            this.width = s * w
            this.height = s * w * 1.66f
        }
        setSize(s * w, s * w * 1.66f)
        addActor(characterImage)
        healthBar = UnitHealthBarActor(r, w * 0.6f, w * 0.2f, health, maxHealth).apply {
            x = if (team == 0) w * 0.2f else -w * 1.2f
            y = -5f
        }
        addActor(healthBar)
        val breath = ScaleToAction().apply {
            setScale(0.99f * characterImage.scaleX, 1.01f * characterImage.scaleY)
            duration = 2f
        }.then(ScaleToAction().apply {
            setScale(characterImage.scaleX, characterImage.scaleY)
            duration = 2f
        }).repeatForever()

        characterImage.addAction(breath)
    }

    override fun act(delta: Float) {
        super.act(delta)
        popupDelay -= delta
        if (popupDelay < 0f) popupDelay = 0f
    }

    fun animateAppear() {
        val tx = x
        x = if (team == 0) -2 * w else (stage?.width ?: 800f) + 2 * w
        val move = MoveToAction().apply {
            duration = 0.2f
            setPosition(tx, y)
            interpolation = SwingOut(2f)
        }
        val rotate = RotateByAction().apply {
            amount = if (team == 0) -20f else 20f
            duration = 0.15f
        }.then(RotateByAction().apply {
            amount = if (team == 0) 20f else -20f
            duration = 0.05f
        })
        addAction(move.along(rotate))
    }
}
