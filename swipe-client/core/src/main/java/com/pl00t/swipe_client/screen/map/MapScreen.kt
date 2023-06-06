package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe.model.LevelType
import com.pl00t.swipe_client.screen.StageScreen
import com.pl00t.swipe_client.services.levels.FrontLevelModel
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.ux.Fonts
import com.pl00t.swipe_client.ux.hideToBehindAndRemove
import com.pl00t.swipe_client.ux.raiseFromBehind
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync
import ktx.log.debug
import kotlin.math.max
import kotlin.math.min


class MapScreen(
    amCore: AssetManager,
    inputMultiplexer: InputMultiplexer,
    private val levelService: LevelService,
) : StageScreen(amCore, inputMultiplexer), GestureDetector.GestureListener {

    lateinit var amMap: AssetManager
    lateinit var taMap: TextureAtlas
    lateinit var taCharacter: TextureAtlas

    lateinit var mapActor: Group
    lateinit var mapImage: Image
    lateinit var linkActor: LinkActor
    lateinit var mapIconsGroup: Group
    private var levelDetailsActor: LevelDetailsActor? = null

    lateinit var actTitle: Label

    private val gestureDetector = GestureDetector(this)

    private val _mapIconSizeSmall = root.height / 15f
    private val _mapIconSize = root.height / 10f
    private val _windowTitleHeight = root.height * 0.08f
    private var _mapScale = 1f

    override fun show() {
        debug("MapScreen") { "map screen is shown" }
        amMap = AssetManager().apply {
            load("atlases/map.atlas", TextureAtlas::class.java)
            load("atlases/charValerion.atlas", TextureAtlas::class.java)
        }
        loadAm(amMap, this::mapLoaded)
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
        taMap = amMap.get("atlases/map.atlas", TextureAtlas::class.java)
        taCharacter = amMap.get("atlases/charValerion.atlas", TextureAtlas::class.java)

        actTitle = Fonts.createWindowTitle("Kingdom Of Harmony", _windowTitleHeight).apply {
            x = 0f
            y = root.height - _windowTitleHeight
            width = root.width
            height = _windowTitleHeight
            setAlignment(Align.center)
        }

        mapActor = Group()
        initMapImage()
        root.addActor(mapActor)
        root.addActor(actTitle)

        KtxAsync.launch {
            val act = levelService.getAct("")
            linkActor = LinkActor(act, 0.003f * root.height).apply {
                width = mapImage.width
                height = mapImage.height
            }
            mapActor.addActor(linkActor)
            mapIconsGroup = Group()
            mapActor.addActor(mapIconsGroup)

            act.levels.forEach { level ->
                val texture = when (level.type) {
                    LevelType.RAID -> taMap.findRegion("map_icon_farm")
                    LevelType.CAMPAIGN -> taMap.findRegion("map_icon_shield")
                    LevelType.BOSS -> taMap.findRegion("map_icon_boss")
                }
                val iconSize = when (level.type) {
                    LevelType.RAID -> _mapIconSize
                    LevelType.CAMPAIGN -> _mapIconSizeSmall
                    LevelType.BOSS -> _mapIconSize
                }
                val iconX = level.x * _mapScale
                val iconY = level.y * _mapScale
                val icon = Image(texture).apply {
                    originX = 0.5f
                    originY = 0.5f
                    x = iconX - iconSize / 2f
                    y = iconY - iconSize / 2f
                    name = level.id
                    width = iconSize
                    height = iconSize
                    alpha = if (level.enabled) 1f else 0.5f
                }
                icon.onClick {
                    showLevelDetails(level)
                }
                mapIconsGroup.addActor(icon)
            }
        }
    }

    private fun showLevelDetails(level: FrontLevelModel) {
        KtxAsync.launch {
            val details = levelService.getLevelDetails(level.id)
            if (levelDetailsActor == null) {
                levelDetailsActor = LevelDetailsActor(details.locationId, details.locationTitle,
                    root.width, root.width, taCore, taMap).apply {
                    this.raiseFromBehind(root.width)
                }
                root.addActor(levelDetailsActor)
            } else {
                levelDetailsActor?.hideToBehindAndRemove(root.width)
                levelDetailsActor = null
            }
        }

    }

    private fun initMapImage() {
        val region = taMap.findRegion("map_act1")
        mapImage = Image(region).apply {
            height = root.height
            width = root.height
        }
        _mapScale = root.height / region.originalHeight
        mapActor.addActor(mapImage)
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
        mapActor.x = max(root.width - mapImage.imageWidth, min(0f, mapActor.x + deltaX))
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
}
