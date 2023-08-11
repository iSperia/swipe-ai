package com.pl00t.swipe_client.ux.dialog

import com.badlogic.gdx.scenes.scene2d.Group
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.services.levels.DialogScript
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.async.KtxAsync

class DialogScriptActor(
    private val r: Resources,
    private val script: DialogScript,
    private val onFinish: suspend () -> Unit
) : Group() {

    private var index = 0
    private var actor: DialogActor? = null

    init {
        val bg = r.image(Resources.ux_atlas, "background_black").apply {
            setSize(r.width, r.height)
            alpha = 0.1f
        }
        addActor(bg)
        showReplica()
    }

    private fun showReplica() {
        if (index < script.replicas.size) {
            actor = DialogActor(r, script.replicas[index]) {
                index++
                showReplica()
            }
            addActor(actor)
        } else {
            KtxAsync.launch {
                onFinish()
                remove()
            }
        }
    }
}
