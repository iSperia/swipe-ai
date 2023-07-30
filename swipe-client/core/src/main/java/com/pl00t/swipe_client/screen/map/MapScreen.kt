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
import com.pl00t.swipe_client.screen.reward.RewardDialog
import com.pl00t.swipe_client.screen.shop.ShopActor
import com.pl00t.swipe_client.services.items.ItemService
import com.pl00t.swipe_client.services.profile.CollectedReward
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.ux.LabelFactory
import com.pl00t.swipe_client.ux.hideToBehindAndRemove
import com.pl00t.swipe_client.ux.raiseFromBehind
import com.pl00t.swipe_client.ux.require
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync
import ktx.log.debug

interface MapScreenRouter {
    fun showSelectKingdom()
    fun showInventory()
    fun activeCharacterChanged()
    fun showHeroesList()

    fun showShop()
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
    private val mapIconSize = root.height / 12f
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

        mapTitle = LabelFactory.createScreenTitle(
            context = this,
            skin = skin,
            text = "Королевство Гармонии"
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
                val iconSize = when (level.type) {
                    LevelType.RAID -> mapIconSize
                    LevelType.CAMPAIGN -> mapSmallIconSize
                    LevelType.BOSS -> mapIconSize
                }

                val iconX = level.x * _mapScale
                val iconY = level.y * _mapScale

                println(level.locationBackground)
                val bg = Image(commonAtlas(Atlases.ACT(level.act)).findRegion("${level.locationBackground}_preview").require()).apply {
                    width = iconSize
                    height = iconSize
                }
                val fgTop = Image(commonAtlas(Atlases.COMMON_UX).findRegion("fg_level_top").require()).apply {
                    width = bg.width
                    height = bg.height
                }
                val fgBottom = Image(commonAtlas(Atlases.COMMON_UX).findRegion("fg_level_bottom").require()).apply {
                    width = bg.width
                    height = bg.height
                }
                val group = Group().apply {
                    width = mapIconSize
                    height = mapIconSize
                    x = iconX - iconSize / 2f
                    y = iconY - iconSize / 2f
                }

                group.addActor(bg)
                group.addActor(fgTop)

                if (level.type == LevelType.BOSS) {
                    val fgBoss = Image(commonAtlas(Atlases.COMMON_UNITS).findRegion(level.waves[0][0].skin)).apply {
                        height = iconSize * 1.2f
                        width = this.height * 0.66f
                        x = (iconSize - this.width) / 2f
                        y = 5f
                    }
                    group.addActor(fgBoss)
                }
                if (level.type == LevelType.CAMPAIGN && !profileService.isFreeRewardAvailable(level.act, level.locationId)) {
                    val fgCheckmark = Image(commonAtlas(Atlases.COMMON_UX).findRegion("fg_complete")).apply {
                        height = iconSize * 0.9f
                        width = iconSize * 0.9f
                        x = iconSize * 0.05f
                        y = iconSize * 0.05f
                    }
                    group.addActor(fgCheckmark)
                } else {
                    if (level.enabled) {
                        group.onClick {
                            showLevelDetails(level)
                        }
                    }
                }
                group.addActor(fgBottom)

                mapIconsGroup.addActor(group)
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

    private fun onAttackClicked(locationId: String, tier: Int) {
        router.navigateBattle(SwipeAct.ACT_1, locationId, tier)
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

    override fun showShop() {
        val shopActor = ShopActor(this@MapScreen, skin, this)
        root.addActor(shopActor)
        shopActor.raiseFromBehind(600f)
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
                listOf(ItemBrowserAction.CLOSE, ItemBrowserAction.DUST)
            },
            actionsHandler = { action, item ->
                when (action) {
                    ItemBrowserAction.CLOSE -> inventoryBroswerActor?.hideToBehindAndRemove(760f)
                    ItemBrowserAction.DUST -> {
                        KtxAsync.launch {
                            val rewards = profileService.dustItem(item.id)
                            val dialog = RewardDialog(rewards.rewards.map { reward ->
                                profileService.getCurrency(reward.type).let { currency ->
                                    CollectedReward.CountedCurrency(
                                        currency = reward.type,
                                        amount = reward.amount,
                                        title = currency.name,
                                        description = currency.lore,
                                        rarity = currency.rarity
                                    )
                                }
                            }, this@MapScreen, skin, "Close") {}.apply {
                                x = 40f
                                y = (this@MapScreen.height() - 720f)/2f
                            }
                            root.addActor(dialog)
                            this@MapScreen.inventoryBroswerActor?.reloadData()
                            this@MapScreen.inventoryBroswerActor?.selectItem(null)
                        }
                    }
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

    override fun levelService() = levelService
}
