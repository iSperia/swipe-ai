package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction
import com.badlogic.gdx.scenes.scene2d.actions.RotateByAction
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.battle.logic.Effect
import ktx.actors.along
import ktx.actors.repeatForever
import ktx.actors.then

class UnitActor(
    val id: Int,
    health: Int,
    maxHealth: Int,
    var effects: List<Effect>,
    val context: SwipeContext,
    val skin: Skin,
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
        val region = context.commonAtlas(Atlases.COMMON_UNITS).findRegion(texture)
        characterImage = Image(region).apply {
            this.scaleX = if (team == 0) 1f else -1f
            this.width = s * w
            this.height = s * w * region.originalHeight.toFloat() / region.originalWidth.toFloat()
        }
        addActor(characterImage)
        healthBar = UnitHealthBarActor(w * 0.6f, w * 0.2f, context, skin, health, maxHealth).apply {
            x = if (team == 0) w * 0.2f else -w * 1.2f
            y = -5f
        }
        addActor(healthBar)
        val breath = ScaleToAction().apply {
            setScale(0.98f * characterImage.scaleX, 1.02f * characterImage.scaleY)
            duration = 1.5f
        }.then(ScaleToAction().apply {
            setScale(characterImage.scaleX, characterImage.scaleY)
            duration = 1f
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
