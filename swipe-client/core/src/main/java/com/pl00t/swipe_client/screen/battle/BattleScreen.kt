package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Interpolation.ExpIn
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.game7th.swipe.gestures.SimpleDirectionGestureDetector
import com.pl00t.swipe_client.screen.StageScreen
import com.pl00t.swipe_client.services.battle.BattleDecorations
import com.pl00t.swipe_client.services.battle.BattleService
import com.pl00t.swipe_client.services.battle.logic.BattleEvent
import com.pl00t.swipe_client.services.levels.FrontLevelDetails
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ktx.actors.along
import ktx.actors.then
import ktx.async.KtxAsync
import kotlin.math.max
import kotlin.random.Random

class BattleScreen(
    amCore: AssetManager,
    inputMultiplexer: InputMultiplexer,
    private val battleService: BattleService
) : StageScreen(amCore, inputMultiplexer), SimpleDirectionGestureDetector.DirectionListener {

    lateinit var decorations: BattleDecorations

    lateinit var amBattle: AssetManager
    lateinit var taBattle: TextureAtlas
    lateinit var taMap: TextureAtlas
    lateinit var taUnits: TextureAtlas
    lateinit var taTarot: TextureAtlas

    lateinit var gPanel: Group
    lateinit var gLocation: Group
    lateinit var gUnits: Group
    lateinit var gTileBgs: Group
    lateinit var gTiles: Group

    lateinit var gestureDetector: SimpleDirectionGestureDetector

    private val polygonSpriteBatch = PolygonSpriteBatch()

    private val _panelHei = root.width * 1.1f
    private val _locationHei = root.height - _panelHei * 0.9f
    private val _tileSize = root.width * 0.17f
    private val _tileStrokeWidth = _tileSize * 0.12f
    private val _tileLeftOffset = (root.width - 5 * _tileSize)/2f
    private val _tileBottomOffset = _tileSize
    private val _characterWidth = root.width / 3f

    private var leftUnitsCount = 0
    private var rightUnitsCount = 0
    private var tilesDirty = false

    override fun show() {
        gestureDetector = SimpleDirectionGestureDetector(this)
        multiplexer.addProcessor(root)
        multiplexer.addProcessor(gestureDetector)
        Gdx.input.inputProcessor = multiplexer
        KtxAsync.launch {
            decorations = battleService.createMockBattle()
            amBattle = AssetManager().apply {
                load("atlases/battle.atlas", TextureAtlas::class.java)
                load("atlases/map.atlas", TextureAtlas::class.java)
                load("atlases/units.atlas", TextureAtlas::class.java)
                load("atlases/tarot.atlas", TextureAtlas::class.java)
            }
            loadAm(amBattle) { amLoaded() }
        }
    }

    private fun amLoaded() {
        taBattle = amBattle.get("atlases/battle.atlas", TextureAtlas::class.java)
        taMap = amBattle.get("atlases/map.atlas", TextureAtlas::class.java)
        taUnits = amBattle.get("atlases/units.atlas", TextureAtlas::class.java)
        taTarot = amBattle.get("atlases/tarot.atlas", TextureAtlas::class.java)

        gPanel = Group()
        gLocation = Group()
        gUnits = Group()
        gUnits.y = _panelHei * 0.11f
        gLocation.y = _panelHei * 0.9f

        val panelImage = Image(taBattle.createPatch("panelBg")).apply {
            x = 0f
            y = 0f
            width = root.width
            height = _panelHei
        }
//        val panelImage = Image(taBattle.findRegion("panel_bg")).apply {
//            x = 0f
//            y = 0f
//            width = root.width
//            height = _panelHei
//        }
        gPanel.addActor(panelImage)
        gTileBgs = Group().apply {
            x = _tileLeftOffset
            y = _tileBottomOffset
        }
        gTiles = Group().apply {
            x = _tileLeftOffset
            y = _tileBottomOffset
        }
        gPanel.addActor(gTileBgs)
        gPanel.addActor(gTiles)
        root.addActor(gLocation)
        root.addActor(gPanel)

        val locationSize = max(root.height - _panelHei * 0.9f, max(root.width, _locationHei))
        val locationImage = Image(taMap.findRegion(decorations.background)).apply {
            width = locationSize
            height = locationSize
            x = - (locationSize - root.width) / 2f
        }
        gLocation.addActor(locationImage)
        gLocation.addActor(gUnits)

        addTileBackgrounds()

        val ultimateProgress = UltimateProgressActor(taBattle, _tileSize * 5f, _tileSize * 0.7f).apply {
            x = (root.width - _tileSize * 5f) / 2f
            y = _tileSize * 0.2f
        }
        gPanel.addActor(ultimateProgress)

        KtxAsync.launch { observeBattleEvents() }
    }

    private suspend fun observeBattleEvents() {
        battleService.events().collect { event ->
            println("BE: $event")
            when (event) {
                is BattleEvent.CreateUnitEvent -> {
                    createUnit(event)
                }
                is BattleEvent.CreateTileEvent -> {
                    createTile(event)
                }
                is BattleEvent.MoveTileEvent -> {
                    gTiles.findActor<TileActor>(event.id.toString())?.let { tileActor ->
                        val tx = event.tox * _tileSize
                        val ty = event.toy * _tileSize
//                        placeTile(tileActor)
                        tileActor.updateXY(event.tox, event.toy)
                        tileActor.addAction(
                            MoveToAction().apply {
                                duration = 0.3f
                                interpolation = SwingOut(1.6f)
                                setPosition(tx, ty)
                            })
                    }
                }
                is BattleEvent.MergeTileEvent -> {
                    val tile1 = gTiles.findActor<TileActor>(event.id.toString())
                    val tile2 = gTiles.findActor<TileActor>(event.to.toString())
//                    placeTile(tile1)
                    tile1.updateXY(event.tox, event.toy)
                    tile1.zIndex = 0
                    tile1.addAction(
                        MoveToAction().apply {
                            duration = 0.25f
                            setPosition(event.ttox * _tileSize, event.ttoy * _tileSize)
                            interpolation = SwingOut(1.6f)
                        }.then(RunnableAction().apply {
                            setRunnable {
                                tile2.increaseSectors(event.targetStack)
                                if (event.stackLeft <= 0) {
                                    tile1.addAction(Actions.removeActor())
                                } else {
                                    tile1.decreaseSectors(event.stackLeft)
                                    tile1.addAction(MoveToAction().apply {
                                        duration = 0.05f
                                        setPosition(event.tox * _tileSize, event.toy * _tileSize)
                                    })
                                }
                            }
                        })
                    )
                }
                is BattleEvent.DestroyTileEvent -> {
                    val actor = gTiles.findActor<TileActor>(event.id.toString())
                    actor?.addAction(
                        Actions.sequence(
                            Actions.delay(0.2f),
                            Actions.run { actor.arcVisible = false },
                            Actions.parallel(
                                ScaleByAction().apply {
                                    setAmount(0.2f)
                                    duration = 0.1f
                                },
                                AlphaAction().apply {
                                    alpha = 0f
                                    duration = 0.1f
                                }
                            ),
                            Actions.removeActor()
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    private fun createTile(event: BattleEvent.CreateTileEvent) {
        val tile = TileActor(
            sectors = event.stack,
            maxSectors = event.maxStack,
            size = _tileSize,
            strokeWidth = _tileStrokeWidth,
            cardTexture = event.skin.toString(),
            taBattle = taBattle,
            taTarot = taTarot,
            polygonBatch = polygonSpriteBatch,
            gridX = event.x,
            gridY = event.y,
        )
        tile.name = event.id.toString()
        placeTile(tile)
        gTiles.addActor(tile)
        tile.animateAppear()
    }

    private fun createUnit(event: BattleEvent.CreateUnitEvent) {
        val unit = UnitActor(
            id = event.id,
            health = event.health,
            maxHealth = event.maxHealth,
            effects = event.effects,
            atlas = taUnits,
            texture = event.skin.toString(),
            team = event.team,
            w = _characterWidth,
            s = BattleScaleMapper.map(event.skin),
            position = if (event.team == 0) leftUnitsCount++ else rightUnitsCount++
        )
        unit.name = event.id.toString()
        placeUnit(unit)
        gUnits.addActor(unit)
        unit.animateAppear()
    }

    private fun placeTile(tile: TileActor) {
//        tile.clearActions()
        tile.x = _tileSize * tile.gridX
        tile.y = _tileSize * tile.gridY
    }

    private fun placeUnit(unit: UnitActor) {
        unit.x = if (unit.team == 0) {
            0.6f * _characterWidth * unit.position
        } else {
            root.width - 0.6f * _characterWidth * unit.position
        }
    }

    private fun addTileBackgrounds() {
        (0 until 25).forEach { index ->
            val x = index % 5
            val y = index / 5

            val bg = Image(taBattle.findRegion("tile_bg")).apply {
                this.x = x * _tileSize
                this.y = y * _tileSize
                this.width = _tileSize
                this.height = _tileSize
            }
            gTileBgs.addActor(bg)
        }
    }

    override fun dispose() {
        super.dispose()
        multiplexer.removeProcessor(gestureDetector)
        multiplexer.removeProcessor(root)
        taBattle.dispose()
        taUnits.dispose()
        taTarot.dispose()
        taMap.dispose()
        taCore.dispose()
    }

    override fun onLeft() = processSwipe(-1, 0)
    override fun onRight() = processSwipe(1, 0)
    override fun onUp() = processSwipe(0, 1)
    override fun onDown() = processSwipe(0, -1)

    private fun processSwipe(dx: Int, dy: Int) {
        println("swipe: $dx:$dy")
        KtxAsync.launch { battleService.processSwipe(dx, dy) }
    }
}
