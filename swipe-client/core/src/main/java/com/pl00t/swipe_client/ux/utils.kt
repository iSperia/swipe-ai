package com.pl00t.swipe_client.ux

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import ktx.actors.alpha
import ktx.actors.repeatForever
import java.util.Arrays

fun AtlasRegion?.require() = if (this == null) throw IllegalStateException("No region") else this

private val BOUNDS = Vector2()
fun Actor.bounds(): Rectangle {
    BOUNDS.set(0f, 0f)
    val coords = localToStageCoordinates(BOUNDS)
    return Rectangle(coords.x, coords.y, width, height)
}

fun Actor.path(vararg index: Int): Actor {
    println(this.javaClass.name)
    println((this as Group).getChild(index[0]).javaClass.name)
    if (index.size == 1) {
        return (this as Group).getChild(index[0])
    } else {
        return (this as Group).getChild(index[0]).path(*index.filterIndexed { index, i -> index > 0 }.toIntArray())
    }
}

fun createTutorial(r: Resources, x: Float, y: Float, width: Float, height: Float): Image {
    return r.image(Resources.ux_atlas, "tutorial").apply {
        setSize(width * 61f, height * 61f)
        setPosition(x - width * 30, y - height * 30)
        setOrigin(Align.center)
        setScale(10f)
        addAction(Actions.sequence(
            Actions.scaleTo(1f, 1f, 0.3f),
            Actions.sequence(
                Actions.scaleTo(1.02f, 1.02f, 0.6f),
                Actions.scaleTo(0.98f, 0.98f, 0.6f)
            ).repeatForever()
        ))
        alpha = 0.8f
    }
}
