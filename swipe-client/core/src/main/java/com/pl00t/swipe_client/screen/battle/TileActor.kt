package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.AlphaAction
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.actions.RotateByAction
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import ktx.actors.along
import ktx.actors.alpha

class TileActor(
    private var sectors: Int,
    private var maxSectors: Int,
    private val size: Float,
    private val strokeWidth: Float,
    private val cardTexture: String,
    private val taBattle: TextureAtlas,
    private val taTarot: TextureAtlas,
    private val polygonBatch: PolygonSpriteBatch,
    var gridX: Int,
    var gridY: Int,
) : Group() {

    private val tProgressActive = taBattle.findRegion("progress_active")
    private val tProgressInactive = taBattle.findRegion("progress_inactive")

    lateinit var iTarot: Image

    private val bufferVector = Vector2()

    init {
        iTarot = Image(taTarot.findRegion(cardTexture)).apply {
            this.height = size * 0.9f
            this.width = this.height * 0.66f
            this.setOrigin(Align.center)
            this.x = (size - this.width) / 2f
            this.y = (size - this.height) /2f
            println("${this.x}:${this.y}")
            rotation = -5f
        }
        addActor(iTarot)
        iTarot.addAction(RepeatAction().apply {
            count = -1
            action = SequenceAction(
                RotateByAction().apply {
                    amount = 10f
                    duration = 2f
                },
                RotateByAction().apply {
                    amount = -10f
                    duration = 2f
                }
            )
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {

        val totalSectors = maxSectors
        var drawnSectors = 0
        var angle = -170f
        var sectorAngle = 160f / totalSectors

        super.draw(batch, parentAlpha)

        batch.end()
        polygonBatch.begin()
        bufferVector.set(0f, 0f)
        localToStageCoordinates(bufferVector)

        while (drawnSectors < totalSectors) {
            polygonBatch.strokeArc(
                strokeWidth = strokeWidth,
                x = bufferVector.x + size / 2,
                y = bufferVector.y + size / 2,
                radius = size * 0.45f,
                start = angle,
                degrees = sectorAngle,
                sampling = 2f,
                if (drawnSectors < sectors) tProgressActive else tProgressInactive
            )
            drawnSectors++
            angle += sectorAngle
        }
        polygonBatch.end()
        batch.begin()
    }

    fun animateAppear() {
        iTarot.alpha = 0f
        iTarot.setScale(2f)
        iTarot.rotation = 175f
        val action = AlphaAction().apply {
            alpha = 1f
            duration = 0.4f
        }.along(RotateByAction().apply {
            amount = -180f
            duration = 0.3f
        }).along(ScaleToAction().apply {
            duration = 0.4f
            setScale(1f)
            interpolation = Interpolation.SwingOut(2f)
        })
        iTarot.addAction(action)
    }
}
