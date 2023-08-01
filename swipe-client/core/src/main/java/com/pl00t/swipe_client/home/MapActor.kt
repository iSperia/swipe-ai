package com.pl00t.swipe_client.home

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.screen.map.LinkActor
import com.pl00t.swipe_client.services.levels.FrontActModel
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.ux.WindowTitleActor
import kotlinx.coroutines.launch
import ktx.async.KtxAsync

class MapActor(
    private val r: R,
    private val act: SwipeAct
) : Group() {

    private lateinit var actWindowTitle: WindowTitleActor
    private lateinit var rootGroup: Group
    private lateinit var mapImage: Image
    private lateinit var scrollPane: ScrollPane
    private lateinit var linkActor: LinkActor
    private lateinit var mapIconsGroup: Group
    private var mapScale = 1f
    private val mapIconSize = r.height / 12f
    private val mapSmallIconSize = r.height / 15f

    init {
        val background = r.image(R.ux_atlas, "solid_window_background").apply {
            width = r.width
            height = r.height
            setScaling(Scaling.fill)
        }


        rootGroup = Group().apply {
            width = r.height
            height = r.height
        }
        scrollPane = ScrollPane(rootGroup).apply {
            width = r.width
            height = r.height
        }

        addActor(background)
        addActor(scrollPane)

        addMapImage()
        addWindowTitle()

        val settings = ActionCompositeButton(r).apply {
            setSize(70f, 70f)
            x = actWindowTitle.x + actWindowTitle.width - 75f
            y = actWindowTitle.y + 5f
        }
        addActor(settings)
    }

    private fun addWindowTitle() {
        KtxAsync.launch {
            val actModel = r.profileService.getAct(act)
            addTitle(actModel)
            loadMap(actModel)
        }
    }

    private fun addTitle(actModel: FrontActModel) {
        actWindowTitle = WindowTitleActor(r, actModel.title.value(r.l)).apply {
            y = r.height - this.height
        }

        addActor(actWindowTitle)
    }

    private suspend fun loadMap(actModel: FrontActModel) {
        linkActor = LinkActor(actModel, 0.003f * rootGroup.height).apply {
            width = mapImage.width
            height = mapImage.height
            touchable = Touchable.disabled
        }
        rootGroup.addActor(linkActor)

        mapIconsGroup = Group().apply {
            width = mapImage.width
            height = mapImage.height
            touchable = Touchable.childrenOnly
        }
        rootGroup.addActor(mapIconsGroup)

        actModel.levels.forEach { level ->
            val iconSize = when (level.type) {
                LevelType.RAID -> mapIconSize
                LevelType.CAMPAIGN -> mapSmallIconSize
                LevelType.BOSS -> mapIconSize
            }

            val iconX = level.x * mapScale
            val iconY = level.y * mapScale

            val levelActor = MapLevelActor(r, level).apply {
                setSize(iconSize, iconSize)
                x = iconX - iconSize / 2f
                y = iconY - iconSize / 2f
            }

            mapIconsGroup.addActor(levelActor)
        }
    }

    private fun addMapImage() {
        val region = r.region(R.actAtlas(act), "map")
        mapImage = r.image(R.actAtlas(act), "map").apply {
            height = r.height
            width = r.height
        }
        mapScale = r.height / region.originalHeight
        rootGroup.addActor(mapImage)

    }
}
