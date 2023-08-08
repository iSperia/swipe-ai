package com.pl00t.swipe_client.battle

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.game.SbDisplayTileType
import com.pl00t.swipe_client.Resources
import ktx.actors.along
import ktx.actors.alpha
import ktx.actors.repeatForever
import ktx.actors.then

class TileActor(
    private val r: Resources,
    var sectors: Int,
    private var maxSectors: Int,
    private val size: Float,
    private val strokeWidth: Float,
    private val cardTexture: String,
    private val polygonBatch: PolygonSpriteBatch,
    private val effects: List<String>,
    val type: SbDisplayTileType,
) : Group() {

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
                    val gem = r.image(Resources.battle_atlas, if (i < sectors) "gem_active" else  "gem_inactive").apply {
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

        effects.forEach { skin ->
            val effect = r.image(Resources.skills_atlas, skin).apply {
                setSize(size, size)
            }
            addActor(effect)
        }

    }

    private fun generateBackground() = r.image(Resources.skills_atlas, cardTexture).apply {
        width = size
        height = size
        this.setOrigin(Align.center)
    }

    private fun generateSkill() = r.image(Resources.skills_atlas, cardTexture).apply {
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
                    gem.drawable = TextureRegionDrawable(r.atlas(Resources.battle_atlas).findRegion(if (i < sectors) "gem_active" else "gem_inactive"))
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
