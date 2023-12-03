package com.pl00t.swipe_client.map

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.home.ReloadableScreen
import com.pl00t.swipe_client.screen.map.LinkActor
import com.pl00t.swipe_client.services.levels.FrontActModel
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.services.profile.Debug
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.ux.HoverAction
import com.pl00t.swipe_client.ux.TutorialHover
import com.pl00t.swipe_client.ux.bounds
import com.pl00t.swipe_client.ux.dialog.DialogScriptActor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class MapWindow(
    private val r: Resources,
    private val act: SwipeAct,
    private val onLocationClicked: (String) -> Unit,
    private val navigateParty: () -> Unit,
    private val navigateInventory: () -> Unit,
    private val navigateAtlas: () -> Unit,
) : Group(), ReloadableScreen {

    private val mapSize = r.height - 190f

    private lateinit var rootGroup: Group
    private lateinit var mapImage: Image
    private lateinit var scrollPane: ScrollPane
    private var bottomActionPanel: BottomActionPanel? = null
    private var windowTitle: WindowTitleActor? = null
    private lateinit var linkActor: LinkActor
    private lateinit var mapIconsGroup: Group
    private var mapScale = 1f
    private val mapIconSize = r.height / 12f
    private val mapSmallIconSize = r.height / 15f

    lateinit var actionInventory: ActionCompositeButton

    init {
        setSize(r.width, r.height)

        r.loadMusic("theme_global")
        r.onLoad {
            r.music("theme_global").apply {
                volume = 0.25f
                isLooping = true
                if (!Debug.NoMusic) play()
            }
            KtxAsync.launch {
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

                reload()
            }
        }
    }

    private suspend fun checkTutorial(model: FrontActModel) {
        r.profileService.getTutorial().let { tutorial ->
            if (!tutorial.acti1IntroPassed) {
                val dialog = DialogScriptActor(r, r.profileService.getDialogScript("act1Intro")) {
                    addActor(TutorialHover(r, mapIconsGroup.findActor<Actor>("c1").bounds(), UiTexts.Tutorials.Act1Intro, HoverAction.HoverClick(true) {
                        KtxAsync.launch {
                            r.profileService.saveTutorial(tutorial.copy(acti1IntroPassed = true))
                            onLocationClicked("c1")
                        }
                    }))
                }
                addActor(dialog)
            }
//            else if (!tutorial.act1c1_15IntroPassed) {
//                addActor(TutorialHover(r, mapIconsGroup.findActor<Actor>("c1_5").bounds(), UiTexts.Tutorials.Act1c1_5, HoverAction.HoverClick(true) {
//                    r.profileService.saveTutorial(tutorial.copy(acti1IntroPassed = true))
//                    onLocationClicked("c1_5")
//                }))
//            }
        }

//        if (act == SwipeAct.ACT_1 && model.levels[2].enabled && !r.profileService.getTutorial().a1HeroOpened) {
//            addActor(TutorialHover(r, actionInventory.bounds(), UiTexts.Tutorials.Act1Hero1, HoverAction.HoverClick(true) {
//                navigateParty()
//            }))
//        }
    }

    override fun reload() {
        KtxAsync.launch {
            r.music("theme_global").apply {
                volume = 0.25f
                isLooping = true
                if (!Debug.NoMusic) play()
            }
            val actModel = r.profileService.getAct(act)
            rootGroup.clearChildren()
            addMapImage()
            loadMap(actModel)

            addBottomPanel()
            addWindowTitle()

            checkTutorial(actModel)
        }

    }

    private fun addWindowTitle() {
        KtxAsync.launch {
            val actModel = r.profileService.getAct(act)
            addTitle(actModel)
        }
    }

    private suspend fun addBottomPanel() {
        bottomActionPanel?.remove()
        val profile = r.profileService.getProfile()
        val actions = listOf(
            ActionCompositeButton(r, Action.Stash, Mode.SingleLine(UiTexts.NavItems.value(r.l)), !profile.inventoryUnlocked).apply {
                onClick {
                    navigateInventory()
                }
            },
            ActionCompositeButton(r, Action.Party, Mode.SingleLine(UiTexts.NavParty.value(r.l)), !profile.partyUnlocked).apply {
                this@MapWindow.actionInventory = this
                onClick {
                    navigateParty()
                }
            },
        )
        bottomActionPanel = BottomActionPanel(r, actions, 4)
        addActor(bottomActionPanel)
    }

    private suspend fun addTitle(actModel: FrontActModel) {
        windowTitle?.remove()
        val actionAtlas = if (r.profileService.getProfile().atlasUnlocked) ActionCompositeButton(r, Action.Atlas, Mode.NoText).apply {
            setSize(80f, 80f)
            onClick {
                navigateAtlas()
            }
        } else null
        windowTitle = WindowTitleActor(r, actModel.title.value(r.l), actionAtlas, null, 4).apply {
            y = r.height - this.height
        }

        addActor(windowTitle)
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

        val lastThreeIds = actModel.levels.takeLast(3)
        var tix = 0f
        var tiy = 0f

        actModel.levels.forEach { level ->
            val iconSize = when (level.type) {
                LevelType.RAID -> mapIconSize
                LevelType.CAMPAIGN -> mapSmallIconSize
                LevelType.BOSS -> mapIconSize
                LevelType.ZEPHYR_SHOP -> mapIconSize
                LevelType.CRYSTAL_MINE -> mapIconSize
            }

            val iconX = level.x * mapScale
            val iconY = level.y * mapScale

            if (lastThreeIds.contains(level)) {
                tix += iconX
                tiy += iconY
            }

            val isBoss = level.type == LevelType.BOSS && !r.profileService.isFreeRewardAvailable(act, level.locationId)
            val type = when (level.type) {
                LevelType.BOSS -> if (isBoss) LevelType.BOSS else LevelType.CAMPAIGN
                else -> level.type
            }
            val levelActor = MapLevelActor(r, level.copy(type = type)).apply {
                setSize(iconSize, iconSize)
                x = iconX - iconSize / 2f
                y = iconY - iconSize / 2f
                if (!level.enabled) {
                    touchable = Touchable.disabled
                    alpha = 0.5f
                }
                name = level.locationId
            }

            levelActor.onClick {
                onLocationClicked(level.locationId)
            }

            mapIconsGroup.addActor(levelActor)
        }

        val focusX = tix / lastThreeIds.size - scrollPane.width / 2f
        val focusY = tiy / lastThreeIds.size - scrollPane.height / 2f
        scrollPane.scrollTo(focusX, focusY,r.width,r.height, true, true)
    }

    private fun addMapImage() {
        val region = r.region(Resources.actAtlas(act), "map")
        mapImage = r.image(Resources.actAtlas(act), "map").apply {
            height = mapSize
            width = mapSize
        }
        mapScale = mapSize / region.originalHeight
        rootGroup.addActor(mapImage)

    }
}
