package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.services.battle.logic.TileType
import com.pl00t.swipe_client.ux.require
import ktx.actors.along
import ktx.actors.alpha

class TileActor(
    var sectors: Int,
    private var maxSectors: Int,
    private val size: Float,
    private val strokeWidth: Float,
    private val cardTexture: String,
    private val taBattle: TextureAtlas,
    private val taTarot: TextureAtlas,
    private val polygonBatch: PolygonSpriteBatch,
    var gridX: Int,
    var gridY: Int,
    val type: TileType,
) : Group() {

    private val progressActiveTexture = taBattle.findRegion("progress_active").require()
    private val progressInactiveTexture = taBattle.findRegion("progress_inactive").require()

    lateinit var tarotImage: Image

    private val bufferVector = Vector2()

    public var arcVisible = true

    init {
        when (type) {
            TileType.TAROT -> {
                tarotImage = generateTarot()
                addActor(tarotImage)
                tarotImage.addAction(RepeatAction().apply {
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
            TileType.BACKGROUND -> {
                tarotImage = generateBackground()
                addActor(tarotImage)
            }
        }

    }

    private fun generateBackground() = Image(taTarot.findRegion(cardTexture).require()).apply {
        width = size
        height = size
        this.setOrigin(Align.center)
    }

    private fun generateTarot() = Image(taTarot.findRegion(cardTexture).require()).apply {
            this.height = size * 0.9f
            this.width = this.height * 0.66f
            this.setOrigin(Align.center)
            this.x = (size - this.width) / 2f
            this.y = (size - this.height) / 2f
            rotation = -5f
        }

    fun increaseSectors(target: Int) {
        if (sectors < target) {
            sectors = target
        }
    }

    fun decreaseSectors(target: Int) {
        if (sectors > target) {
            sectors = target
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {

        val totalSectors = maxSectors
        var drawnSectors = 0
        var angle = -170f
        var sectorAngle = 160f / totalSectors

        super.draw(batch, parentAlpha)

        if (arcVisible) {
            batch.end()
            polygonBatch.begin()
            polygonBatch.projectionMatrix = batch.projectionMatrix
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
                    if (drawnSectors < sectors) progressActiveTexture else progressInactiveTexture
                )
                drawnSectors++
                angle += sectorAngle
            }
            polygonBatch.end()
            batch.begin()
        }
    }

    fun animateAppear() {
        when (type) {
            TileType.TAROT -> {
                tarotImage.alpha = 0f
                tarotImage.setScale(2f)
                tarotImage.rotation = 175f
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
                tarotImage.addAction(action)
            }
            TileType.BACKGROUND -> {
                tarotImage.alpha = 0f
                tarotImage.setScale(1.2f)
                tarotImage.addAction(Actions.parallel(
                    Actions.alpha(1f, 0.4f),
                    Actions.scaleTo(1f, 1f, 0.4f)
                ))
            }
        }
    }

    fun updateXY(tx: Int, ty: Int) {
        this.gridX = tx
        this.gridY = ty
    }

    fun animateMerge(t: TileActor?, targetStack: Int, stackLeft: Int) {
        arcVisible = false
        if (t != null) {
            val dx = t.x - x
            val dy = t.y - y
            if (stackLeft <= 0) {
                arcVisible = false
                val action = Actions.sequence(
                    MoveToAction().apply {
                        duration = 0.05f
                        setPosition(t.x, t.y)
                    },
                    RunnableAction().apply { setRunnable { t.sectors = targetStack } },
                    Actions.removeActor()
                )
                addAction(action)
            } else {
                var tarotCopy = generateTarot()
                val action = Actions.sequence(
                    MoveByAction().apply {
                        setAmount(dx, dy)
                        duration = 0.05f
                    },
                    RunnableAction().apply { setRunnable { t.sectors = targetStack } },
                    Actions.removeActor()
                )
                addActor(tarotCopy)
                tarotCopy.addAction(action)
            }
        } else {
            sectors = stackLeft
        }
    }
}
