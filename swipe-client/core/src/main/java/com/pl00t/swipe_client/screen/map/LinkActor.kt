package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.pl00t.swipe_client.services.levels.FrontActModel
import com.pl00t.swipe_client.ux.Colors

class LinkActor(
    private val act: FrontActModel,
    private val lineWidth: Float,
) : Actor() {

    private val renderer = ShapeRenderer().apply {
        color = Colors.BLACK_TRANSPARENT
        setAutoShapeType(true)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        batch.end()
        renderer.begin()
        renderer.transformMatrix = batch.transformMatrix
        renderer.projectionMatrix = batch.projectionMatrix
        renderer.set(ShapeRenderer.ShapeType.Filled)

        act.links.forEach { link ->
            val l1 = act.levels.first { it.id == link.n1 }
            val l2 = act.levels.first { it.id == link.n2 }

            if (l1.enabled && l2.enabled) {
                val x1 = l1.x * width / 1024f
                val x2 = l2.x * width / 1024f
                val y1 = l1.y * height / 1024f
                val y2 = l2.y * height / 1024f

                renderer.rectLine(x1, y1, x2, y2, lineWidth)
            }

        }

        renderer.end()
        batch.begin()
    }


}
