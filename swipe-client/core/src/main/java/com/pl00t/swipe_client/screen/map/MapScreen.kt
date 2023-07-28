package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.screen.Router
import com.pl00t.swipe_client.screen.StageScreen
import com.pl00t.swipe_client.screen.navpanel.NavigationPanel
import com.pl00t.swipe_client.services.levels.FrontLevelDetails
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.levels.LevelType
import com.game7th.swipe.monsters.MonsterService
import com.pl00t.swipe_client.screen.items.ItemBrowserAction
import com.pl00t.swipe_client.screen.items.ItemBrowserActor
import com.pl00t.swipe_client.services.items.ItemService
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.ux.ScreenTitle
import com.pl00t.swipe_client.ux.hideToBehindAndRemove
import com.pl00t.swipe_client.ux.raiseFromBehind
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync
import ktx.log.debug

interface MapScreenRouter {
    fun showSelectKingdom()
    fun showInventory()
    fun activeCharacterChanged()
    fun showHeroesList()
}

class MapScreen(
    private val act: SwipeAct,
    amCore: AssetManager,
    inputMultiplexer: InputMultiplexer,
    private val profileService: ProfileService,
    private val levelService: LevelService,
    private val monsterService: MonsterService,
    private val itemService: ItemService,
    private val router: Router,
) : StageScreen(amCore, inputMultiplexer), GestureDetector.GestureListener, LevelDetailsCallback, MapScreenRouter {

    lateinit var mapAssetManager: AssetManager
    lateinit var skin: Skin

    lateinit var topBlock: Stack

    lateinit var mapActor: Group
    lateinit var mapScroll: ScrollPane
    lateinit var mapImage: Image
    lateinit var linkActor: LinkActor
    lateinit var mapIconsGroup: Group
    lateinit var navigationPanel: NavigationPanel
    private var levelDetailsActor: LevelDetailsActor? = null
    private var inventoryBroswerActor: ItemBrowserActor? = null

    lateinit var mapTitle: Group

    private val gestureDetector = GestureDetector(this)

    private val mapSmallIconSize = root.height / 15f
    private val mapIconSize = root.height / 10f
    private var _mapScale = 1f

    override fun show() {
        debug("MapScreen") { "map screen is shown" }
        mapAssetManager = AssetManager().apply {
            load(Atlases.ACT(act), TextureAtlas::class.java)
            load(Atlases.COMMON_MAP, TextureAtlas::class.java)
            load(Atlases.COMMON_UX, TextureAtlas::class.java)
            load(Atlases.COMMON_UNITS, TextureAtlas::class.java)
            load(Atlases.COMMON_TAROT, TextureAtlas::class.java)
            load(Atlases.COMMON_SKILLS, TextureAtlas::class.java)
        }
        loadAm(mapAssetManager, this::mapLoaded)
        multiplexer.addProcessor(root)
        multiplexer.addProcessor(gestureDetector)
        Gdx.input.inputProcessor = multiplexer
    }

    override fun render(delta: Float) {
        super.render(delta)
        root.act()
    }

    private fun mapLoaded() {
        debug("MapScreen") { "Map screen is loaded" }

        skin = Skin(Gdx.files.internal("styles/ui.json")).apply {
            addRegions(commonAtlas(Atlases.COMMON_UX))
        }

        mapTitle = ScreenTitle.createScreenTitle(
            context = this,
            skin = skin,
            text = "Kingdom Of Harmony"
        ).apply {
            y = root.height - 60f
            x = 60f
        }

        mapActor = Group()
        initMapImage()
        mapScroll = ScrollPane(mapActor).apply {
            width = root.width
            height = root.height
        }
        mapImage.onClick {
            levelDetailsActor?.hideToBehindAndRemove(root.width)
            levelDetailsActor = null
        }
        root.addActor(mapScroll)
        root.addActor(mapTitle)

        KtxAsync.launch {
            val act = profileService.getAct(SwipeAct.ACT_1)
            println(act)
            linkActor = LinkActor(act, 0.003f * root.height).apply {
                width = mapImage.width
                height = mapImage.height
                touchable = Touchable.disabled
            }
            mapActor.addActor(linkActor)
            mapIconsGroup = Group().apply {
                width = mapImage.width
                height = mapImage.height
                touchable = Touchable.childrenOnly
            }
            mapActor.addActor(mapIconsGroup)


            act.levels.forEach { level ->
                val texture = when (level.type) {
                    LevelType.RAID -> commonAtlas(Atlases.COMMON_MAP).findRegion("map_icon_farm")
                    LevelType.CAMPAIGN -> commonAtlas(Atlases.COMMON_MAP).findRegion("map_icon_shield")
                    LevelType.BOSS -> commonAtlas(Atlases.COMMON_MAP).findRegion("map_icon_boss")
                }
                val iconSize = when (level.type) {
                    LevelType.RAID -> mapIconSize
                    LevelType.CAMPAIGN -> mapSmallIconSize
                    LevelType.BOSS -> mapIconSize
                }
                val iconX = level.x * _mapScale
                val iconY = level.y * _mapScale
                val icon = Image(texture).apply {
                    originX = 0.5f
                    originY = 0.5f
                    x = iconX - iconSize / 2f
                    y = iconY - iconSize / 2f
                    name = level.locationId
                    width = iconSize
                    height = iconSize
                    alpha = if (level.enabled) 1f else 0.5f
                }
                if (level.enabled) {
                    icon.onClick {
                        showLevelDetails(level)
                    }
                }
                mapIconsGroup.addActor(icon)
            }
        }

        navigationPanel = NavigationPanel(this@MapScreen, skin, this@MapScreen)
        root.addActor(navigationPanel)
    }

    private fun showLevelDetails(level: FrontLevelDetails) {
        KtxAsync.launch {
            val details = levelService.getLevelDetails(SwipeAct.ACT_1, level.locationId)
            levelDetailsActor?.hideToBehindAndRemove(root.width)
            levelDetailsActor = null

            levelDetailsActor = LevelDetailsActor(
                levelDetails = details,
                attackAction = this@MapScreen::onAttackClicked,
                context = this@MapScreen,
                skin = skin).apply {
                    this.raiseFromBehind(root.width)
                }
            levelDetailsActor?.callback = this@MapScreen
            root.addActor(levelDetailsActor)
        }

    }

    private fun onAttackClicked(locationId: String) {
        router.navigateBattle(SwipeAct.ACT_1, locationId)
    }

    private fun initMapImage() {
        val region = commonAtlas(Atlases.ACT(act)).findRegion("map")
        mapImage = Image(region).apply {
            height = root.height
            width = root.height
        }
        _mapScale = root.height / region.originalHeight
        mapActor.addActor(mapImage)
        mapActor.width = mapImage.width
        mapActor.height = mapImage.height
        mapActor.touchable = Touchable.childrenOnly
    }

    override fun hide() = dispose()

    override fun dispose() {
        super.dispose()
        multiplexer.removeProcessor(root)
        multiplexer.removeProcessor(gestureDetector)
        mapAssetManager.dispose()
    }

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        return false
    }

    override fun longPress(x: Float, y: Float): Boolean {
        return false
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        return false
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
//        mapActor.x = max(root.width - mapImage.imageWidth, min(0f, mapActor.x + deltaX))
        return false
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        return false
    }

    override fun pinch(
        initialPointer1: Vector2?,
        initialPointer2: Vector2?,
        pointer1: Vector2?,
        pointer2: Vector2?
    ): Boolean {
        return true
    }

    override fun pinchStop() {
    }

    override fun processMonsterClicked(unitSkin: String) {
        KtxAsync.launch {
            monsterService.getMonster(unitSkin)?.let { config ->
                val monsterDetailActor = MonsterDetailPanel(
                    monsterConfiguration = config,
                    context = this@MapScreen,
                    skin = skin,
                    router = this@MapScreen
                )
                root.addActor(monsterDetailActor)
                monsterDetailActor.raiseFromBehind(root.height)
            }
        }
    }

    override fun showHeroesList() {
        val heroesListActor = HeroesListActor(this@MapScreen, profileService, monsterService,this@MapScreen, skin)
        root.addActor(heroesListActor)
        heroesListActor.raiseFromBehind(height())
    }

    override fun activeCharacterChanged() {
        navigationPanel.reloadActiveHeroLabel()
    }

    override fun showSelectKingdom() {
    }

    override fun showInventory() {
        this@MapScreen.inventoryBroswerActor = ItemBrowserActor(
            categoryFilter = null,
            selectedId = null,
            browserWidth = 480f,
            browserHeight = 360f,
            context = this@MapScreen,
            skin = skin,
            actionsProvider = {
                listOf(ItemBrowserAction.CLOSE)
            },
            actionsHandler = { action, item ->
                when (action) {
                    ItemBrowserAction.CLOSE -> inventoryBroswerActor?.hideToBehindAndRemove(760f)
                    else -> {}
                }
            }
        )
        inventoryBroswerActor?.raiseFromBehind(640f)
        root.addActor(inventoryBroswerActor)
    }

    override fun monsterService() = monsterService

    override fun itemService() = itemService

    override fun profileService() = profileService
}
