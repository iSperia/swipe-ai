package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.game.SbDisplayTileType
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.ux.require
import ktx.actors.along
import ktx.actors.alpha
import ktx.actors.repeatForever
import ktx.actors.then

class TileActor(
    var sectors: Int,
    private var maxSectors: Int,
    private val size: Float,
    private val strokeWidth: Float,
    private val cardTexture: String,
    private val context: SwipeContext,
    private val polygonBatch: PolygonSpriteBatch,
    val type: SbDisplayTileType,
) : Group() {

    private val taTarot = context.commonAtlas(Atlases.COMMON_TAROT)
    private val taSkills = context.commonAtlas(Atlases.COMMON_SKILLS)
    private val taUx = context.commonAtlas(Atlases.COMMON_UX)

    lateinit var skillImage: Image

    init {
        when (type) {
            SbDisplayTileType.TAROT -> {
                skillImage = generateSkill()
                addActor(skillImage)

                val gemSize = size / 5f
                val totalGemSize = gemSize * maxSectors * 0.8f
                val sx = (size - totalGemSize) / 2f
                (0 until maxSectors).forEach { i ->
                    val gem = Image(taUx.findRegion(if (i < sectors) "gem_active" else  "gem_inactive")).apply {
                        x = sx + gemSize * 0.8f * i
                        y = 1f
                        width = gemSize
                        height = gemSize
                    }
                    addActor(gem)
                }
                updateSectors(1)
            }
            SbDisplayTileType.BACKGROUND -> {
                skillImage = generateBackground()
                addActor(skillImage)
            }
        }

    }

    private fun generateBackground() = Image(taTarot.findRegion(cardTexture).require()).apply {
        width = size
        height = size
        this.setOrigin(Align.center)
    }

    private fun generateSkill() = Image(taSkills.findRegion(cardTexture).require()).apply {
            this.height = size * 0.9f
            this.width = size * 0.9f
            this.setOrigin(Align.center)
            this.x = (size - this.width) / 2f
            this.y = (size - this.height) / 2f
        }

    fun updateSectors(target: Int) {
        if (sectors != target) {
            sectors = target
            (0 until maxSectors).forEach { i ->
                (getChild(1 + i) as Image).let { gem ->
                    gem.drawable = TextureRegionDrawable(taUx.findRegion(if (i < sectors) "gem_active" else "gem_inactive"))
                }
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {

        super.draw(batch, parentAlpha)
    }

    fun animateAppear() {
        when (type) {
            SbDisplayTileType.TAROT -> {
                skillImage.alpha = 0f
                skillImage.setScale(0f)
                skillImage.rotation = -30f
                val action = Actions.delay(0.1f)
                    .then(Actions.alpha(1f, 0.4f)
                        .along(Actions.rotateBy(30f, 0.3f))
                        .along(Actions.scaleTo(1f, 1f, 0.4f, SwingOut(1.6f))))
                    .then(Actions.run {
                        skillImage.addAction(Actions.sequence(
                            Actions.scaleTo(1.05f, 1.05f, 2f),
                            Actions.scaleTo(1f, 1f, 2f)
                        ).repeatForever())
                    })
                skillImage.addAction(action)
            }
            SbDisplayTileType.BACKGROUND -> {
                skillImage.alpha = 0f
                skillImage.setScale(1.2f)
                skillImage.addAction(Actions.parallel(
                    Actions.alpha(1f, 0.4f),
                    Actions.scaleTo(1f, 1f, 0.4f)
                ))
            }
        }
    }
}
