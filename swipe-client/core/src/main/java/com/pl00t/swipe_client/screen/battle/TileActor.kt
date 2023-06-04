package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.actions.RotateByAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image

class TileActor(
    private var sectors: Int,
    private var maxSectors: Int,
    private val size: Float,
    private val strokeWidth: Float,
    private val cardTexture: String,
    private val taBattle: TextureAtlas,
    private val taPersonage: TextureAtlas,
    private val polygonBatch: PolygonSpriteBatch
) : Group() {

    private val tProgressActive = taBattle.findRegion("progress_active")
    private val tProgressInactive = taBattle.findRegion("progress_inactive")

    lateinit var iTarot: Image

    private val bufferVector = Vector2()

    init {
        iTarot = Image(taPersonage.findRegion(cardTexture)).apply {
            this.height = size * 0.9f
            this.width = this.height * 0.66f
            x = (size - this.width) / 2f
            y = (size - this.height) /2f
            originX = this.width / 2f
            originY = this.height /2f
            rotation = -10f
        }
        addActor(iTarot)
        iTarot.addAction(RepeatAction().apply {
            count = -1
            action = SequenceAction(
                RotateByAction().apply {
                    amount = 20f
                    duration = 1.5f
                },
                RotateByAction().apply {
                    amount = -20f
                    duration = 1.5f
                }
            )
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {

//        batch.end()
//        polygonBatch.begin()

        bufferVector.set(0f, 0f)
        localToStageCoordinates(bufferVector)

        val totalSectors = maxSectors
        var drawnSectors = 0
        var angle = -170f
        var sectorAngle = 160f / totalSectors
//        while (angle + sectorAngle <= 180f) {
//            polygonBatch.strokeArc(
//                strokeWidth = strokeWidth,
//                x = bufferVector.x + size / 2,
//                y = bufferVector.y + size / 2,
//                radius = size * 0.45f,
//                start = angle,
//                degrees = sectorAngle,
//                sampling = 2f,
//                if (drawnSectors < sectors) tProgressActive else tProgressInactive
//            )
//            drawnSectors++
//            angle += sectorAngle
//        }

//        polygonBatch.end()
//        batch.begin()

        super.draw(batch, parentAlpha)

        batch.end()
        polygonBatch.begin()
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
}
