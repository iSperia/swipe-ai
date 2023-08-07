package com.pl00t.swipe_client.battle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.FloatArray

fun PolygonSpriteBatch.strokeArc(strokeWidth: Float, x: Float, y: Float, radius: Float, start: Float, degrees: Float, sampling: Float = 2f, texture: TextureRegion) {
    val segments = (degrees / 12).toInt()

    val color = Color.WHITE.toFloatBits()
    val verticeCount = (segments + 1) * 2
    val vertices = FloatArray(verticeCount * 5)
    val degreeDelta = degrees / segments

    for (i in 0..segments) {
        /**Close to center vertex*/
        /*x*/vertices.add(x + (radius - strokeWidth) * MathUtils.cosDeg(start + degreeDelta * i))
        /*y*/vertices.add(y + (radius - strokeWidth) * MathUtils.sinDeg(start + degreeDelta * i))
        /*c*/vertices.add(color)
        /*u*/vertices.add(texture.u + (texture.u2 - texture.u) * i.toFloat() / segments)
        /*v*/vertices.add(texture.v)

        /**Remote radius vertex*/
        /*x*/vertices.add(x + radius * MathUtils.cosDeg(start + degreeDelta * i))
        /*y*/vertices.add(y + radius * MathUtils.sinDeg(start + degreeDelta * i))
        /*c*/vertices.add(color)
        /*u*/vertices.add(texture.u + (texture.u2 - texture.u) * i.toFloat() / segments)
        /*v*/vertices.add(texture.v2)
    }

    val triangles = ShortArray(segments * 2 * 3) { index -> (index / 3 + index % 3).toShort() }
    draw(texture.texture, vertices.toArray(), 0, verticeCount * 5, triangles, 0, triangles.size)
}
