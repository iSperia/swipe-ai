package com.pl00t.swipe_client.ux

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction

fun Actor.raiseFromBehind(height: Float) {
    addAction(MoveByAction().apply {
        amountY = height
        duration = 0.2f
    })
    this.y -= height
}

fun Actor.hideToBehindAndRemove(height: Float) {
    addAction(
        SequenceAction(
            MoveByAction().apply {
                amountY = -height
                duration = 0.2f
            },
            RunnableAction().apply {
                setRunnable { this.target.remove() }
            }
        ))
}
