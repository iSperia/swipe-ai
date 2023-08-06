package com.pl00t.swipe_client.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.screen.map.LinkActor
import com.pl00t.swipe_client.services.levels.FrontActModel
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.services.profile.SwipeAct
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync

class MapWindow(
    private val r: R,
    private val act: SwipeAct,
    private val onLocationClicked: (String) -> Unit,
    private val navigateParty: () -> Unit,
    private val navigateInventory: () -> Unit,
) : Group() {

    private val mapSize = r.height - 190f

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
        rootGroup = Group().apply {
            width = mapSize
            height = mapSize
        }
        scrollPane = ScrollPane(rootGroup).apply {
            width = r.width
            height = mapSize
            y = 110f
        }

        addActor(scrollPane)

        addMapImage()
        addWindowTitle()
        addBottomPanel()
    }

    private fun addWindowTitle() {
        KtxAsync.launch {
            val actModel = r.profileService.getAct(act)
            addTitle(actModel)
            loadMap(actModel)
        }
    }

    private fun addBottomPanel() {
        val actions = listOf(
            ActionCompositeButton(r, Action.Shop, Mode.SingleLine(UiTexts.NavShop.value(r.l))),
            ActionCompositeButton(r, Action.Stash, Mode.SingleLine(UiTexts.NavItems.value(r.l))).apply {
                onClick {
                    navigateInventory()
                }
            },
            ActionCompositeButton(r, Action.Party, Mode.SingleLine(UiTexts.NavParty.value(r.l))).apply {
                onClick {
                    navigateParty()
                }
            },
        )
        val bottomPanel = BottomActionPanel(r, actions, 4)
        addActor(bottomPanel)
    }

    private fun addTitle(actModel: FrontActModel) {
        val settings = ActionCompositeButton(r, Action.Vault, Mode.NoText).apply {
            setSize(80f, 80f)
            onClick { r.router.navigateMap() }
        }

        actWindowTitle = WindowTitleActor(r, actModel.title.value(r.l), null, null, 4).apply {
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

            levelActor.onClick {
                onLocationClicked(level.locationId)
            }

            mapIconsGroup.addActor(levelActor)
        }
    }

    private fun addMapImage() {
        val region = r.region(R.actAtlas(act), "map")
        mapImage = r.image(R.actAtlas(act), "map").apply {
            height = mapSize
            width = mapSize
        }
        mapScale = mapSize / region.originalHeight
        rootGroup.addActor(mapImage)

    }
}
