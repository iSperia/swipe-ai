package com.pl00t.swipe_client.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.services.levels.FrontLevelModel
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.ux.require
import kotlinx.coroutines.launch
import ktx.actors.onExit
import ktx.actors.onTouchDown
import ktx.async.KtxAsync

class MapLevelActor(
    private val r: Resources,
    private val model: FrontLevelModel
) : Group() {

    private val backgroundImage: Image
    private val foregroundTop: Image
    private val foregroundBottom: Image
    private var foregroundBoss: Image? = null
    private var foregroundCheckmark: Image? = null

    init {
        backgroundImage = Image(r.atlas(Resources.actAtlas(model.act)).findRegion("${model.locationBackground}_preview").require()).apply { setOrigin(Align.center) }
        foregroundTop = r.image(Resources.ux_atlas, "fg_level_top").apply { setOrigin(Align.center) }
        foregroundBottom = r.image(Resources.ux_atlas, "fg_level_bottom").apply { setOrigin(Align.center) }

        addActor(backgroundImage)
        addActor(foregroundTop)

        if (model.type == LevelType.BOSS) {
            foregroundBoss = r.image(Resources.units_atlas, model.waves[0][0].skin).apply { setOrigin(Align.bottom) }
            addActor(foregroundBoss)
        }
        KtxAsync.launch { loadCheckmark() }

        setOrigin(Align.center)

        onTouchDown {
            listOfNotNull(backgroundImage, foregroundTop, foregroundBottom, foregroundCheckmark).forEach {
                it.addAction(Actions.scaleTo(0.9f, 0.9f, 0.2f))
            }
            foregroundBoss?.addAction(Actions.scaleTo(0.85f, 0.85f, 0.2f))
        }
        onExit {
            listOfNotNull(backgroundImage, foregroundTop, foregroundBottom, foregroundCheckmark).forEach {
                it.addAction(Actions.scaleTo(1f, 1f, 0.2f))
            }
            foregroundBoss?.addAction(Actions.scaleTo(1f, 1f, 0.2f))
        }
    }

    private suspend fun loadCheckmark() {
        if (model.type == LevelType.CAMPAIGN && !r.profileService.isFreeRewardAvailable(
                model.act,
                model.locationId
            )
        ) {
            foregroundCheckmark = r.image(Resources.ux_atlas, "fg_complete").apply { setOrigin(Align.center) }
            addActor(foregroundCheckmark)
        }
        addActor(foregroundBottom)
        sizeChanged()
    }

    override fun sizeChanged() {
        super.sizeChanged()

        val size = this@MapLevelActor.width

        backgroundImage.apply {
            this.width = size * 0.9f
            this.height = size * 0.9f
            this.x = size * 0.05f
            this.y = size * 0.05f
            setOrigin(Align.center)
        }
        foregroundTop.apply {
            this.width = size
            this.height = size
            setOrigin(Align.center)
        }
        foregroundBottom.apply {
            this.width = size
            this.height = size
            setOrigin(Align.center)
        }
        foregroundBoss?.apply {
            this.height = size * 1.2f
            this.width = this.height * 0.66f
            this.x = (size - this.width) / 2f
            this.y = 10f
            setOrigin(Align.bottom)
        }
        foregroundCheckmark?.apply {
            this.height = size * 0.9f
            this.width = size * 0.9f
            this.x = size * 0.05f
            this.y = size * 0.05f
            setOrigin(Align.center)
        }
    }
}
