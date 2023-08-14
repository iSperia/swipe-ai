package com.pl00t.swipe_client.home

import com.badlogic.gdx.math.Interpolation.ElasticIn
import com.badlogic.gdx.math.Interpolation.ElasticOut
import com.badlogic.gdx.math.Interpolation.ExpIn
import com.badlogic.gdx.math.Interpolation.ExpOut
import com.badlogic.gdx.math.Interpolation.Pow
import com.badlogic.gdx.math.Interpolation.SwingIn
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import ktx.actors.alpha

interface ReloadableScreen {
    fun reload()
}

class StackDelegate(private val root: Group) {

    private val stack = mutableListOf<Actor>()

    fun showScreen(actor: Actor) {
        root.addActor(actor)
        actor.setOrigin(Align.bottom)
        stack.add(actor)
        actor.alpha = 0f
        actor.setScale(1.3f)
        actor.addAction(Actions.parallel(
            Actions.alpha(1f, 0.25f),
            Actions.scaleTo(1f, 1f, 0.5f, Pow(2))
        ))
    }

    fun moveBack() {
        val actor = stack.removeLastOrNull() ?: return
        actor.addAction(Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(1.3f, 1.3f, 0.5f, Pow(2)),
                Actions.delay(0.3f, Actions.alpha(0f, 0.2f))
            ),
            Actions.removeActor()
        ))
        stack.lastOrNull()?.let { a ->
            if (a is ReloadableScreen) {
                a.reload()
            }
        }
    }
}
