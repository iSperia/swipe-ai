package com.pl00t.swipe_client.ux

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import ktx.actors.alpha
import ktx.actors.repeatForever

fun AtlasRegion?.require() = if (this == null) throw IllegalStateException("No region") else this

private val BOUNDS = Vector2()
fun Actor.bounds(): Rectangle {
    BOUNDS.set(0f, 0f)
    val coords = localToStageCoordinates(BOUNDS)
    return Rectangle(coords.x, coords.y, width, height)
}

fun createTutorial(r: Resources, x: Float, y: Float, width: Float, height: Float): Image {
    return r.image(Resources.ux_atlas, "tutorial").apply {
        setSize(width * 51f, height * 51f)
        setPosition(x - width * 25, y - height * 25)
        setOrigin(Align.center)
        setScale(10f)
        addAction(Actions.sequence(
            Actions.scaleTo(1f, 1f, 0.3f, SwingOut(1.1f)),
            Actions.sequence(
                Actions.scaleTo(1.05f, 1.05f, 0.2f),
                Actions.scaleTo(0.95f, 0.95f, 0.2f)
            ).repeatForever()
        ))
        alpha = 0.8f
    }
}
