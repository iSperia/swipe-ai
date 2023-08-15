package com.pl00t.swipe_client.animation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Array

class AnimationActor(
    atlas: TextureAtlas,
    prefix: String,
    private val frameRate: Float = 60f,
    private val tint: Color? = null,
    private val flipX: Boolean = false
) : Actor() {

    private val animation: Animation<Drawable>

    private var time = 0f

    init {
        val array = atlas.findRegions(prefix).map { TextureRegionDrawable(it).let {
            it.region.flip(flipX, false)
            if (tint != null) {
                it.tint(tint)
            } else {
                it
            }
        }}.toTypedArray()
        animation = Animation<Drawable>(1/frameRate, Array(array))
    }

    override fun act(delta: Float) {
        super.act(delta)
        time += delta

        if (time > animation.animationDuration) remove()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        val region = animation.getKeyFrame(time)

        region.draw(batch, x, y, width * scaleX, height * scaleY)
//        batch.draw(region, x, y, width * scaleX, height * scaleY)
    }
}
