package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import kotlin.math.abs
import kotlin.math.max

class ExperienceBar(
    private val context: SwipeContext,
    private val skin: Skin
): Actor() {

    private val activeTexture = context.commonAtlas(Atlases.COMMON_UX).findRegion("progress_active")
    private val bgTexture = context.commonAtlas(Atlases.COMMON_UX).findRegion("progress_inactive")

    private var value: Int = 0
    private var maxValue: Int = 0
    private var progress: Float = 0f

    private var lastProgress: Float = 0f
    private var displayProgress: Float = 0f
    private var lastProgressStamp: Float = 0f

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        displayProgress = progress
        if (lastProgress != progress) {
            lastProgressStamp += Gdx.graphics.deltaTime
            if (lastProgressStamp >= 0.3f) {
                lastProgress = progress
            } else {
                displayProgress = lastProgress + lastProgressStamp / 0.3f * (progress - lastProgress)
            }
        }

        batch.draw(bgTexture, x, y, width, height)
        batch.draw(activeTexture.texture, x, y, width * displayProgress, height, activeTexture.u, activeTexture.v, activeTexture.u + (activeTexture.u2 - activeTexture.u) * displayProgress, activeTexture.v2 )
    }

    fun setProgress(value: Int, maxValue: Int) {
        this.value = value
        this.maxValue = maxValue
        this.lastProgress = this.displayProgress
        this.progress = max(0.01f, value.toFloat() / maxValue)
        lastProgressStamp = 0f
    }
}
