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
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.gestures.SimpleDirectionGestureDetector
import com.pl00t.swipe_client.screen.StageScreen
import com.pl00t.swipe_client.services.battle.BattleDecorations
import com.pl00t.swipe_client.services.battle.BattleService
import com.pl00t.swipe_client.services.battle.logic.BattleEvent
import com.pl00t.swipe_client.services.battle.logic.processor.TarotAnimation
import com.pl00t.swipe_client.services.levels.FrontLevelDetails
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ktx.actors.along
import ktx.actors.alpha
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
    lateinit var gTarotEffects: Group
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
        gTarotEffects = Group()
        gUnits.y = _panelHei * 0.11f
        gLocation.y = _panelHei * 0.9f
        gTarotEffects.y = _panelHei * 0.11f

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
        gLocation.addActor(gTarotEffects)

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
                    processMoveTileEvent(event)
                }
                is BattleEvent.MergeTileEvent -> {
                    processMergeTileEvent(event)
                }
                is BattleEvent.DestroyTileEvent -> {
                    proessDestroyEvent(event)
                }
                is BattleEvent.AnimateTarotEvent -> {
                    when (event.animation) {
                        is TarotAnimation.TarotFromSourceTargets -> {
                            processTargetedAnimation(event.animation, event)
                        }
                        is TarotAnimation.TarotFromSourceDirected -> {
                            processDirectedAnimation(event.animation, event)
                        }
                        is TarotAnimation.TarotAtSourceRotate -> {
                            processStaticAnimation(event.animation, event)
                        }
                        else -> {}
                    }
                }
                else -> Unit
            }
        }
    }

    private fun processStaticAnimation(
        animation: TarotAnimation.TarotAtSourceRotate,
        event: BattleEvent.AnimateTarotEvent
    ) {
        val sourceUnit = gUnits.findActor<UnitActor>(animation.at.toString())
        //ok, we have some crazy tarot stuff
        val tarot = Image(taTarot.findRegion(event.animation.skin.toString())).apply {
            x = sourceUnit.x + if (sourceUnit.team == 0) _characterWidth * 0.1f else -_characterWidth * 1.1f
            y = sourceUnit.y + _characterWidth * 0.35f
            width = _characterWidth * 0.8f
            height = _characterWidth * 0.8f * 1.66f
            setOrigin(Align.center)
        }
        tarot.scaleX = 0.1f
        tarot.scaleY = 0.1f
        tarot.alpha = 0f
        val action = Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(1f, 1f, 0.3f),
                Actions.alpha(0.8f, 0.3f),
                Actions.moveBy(0f, -_characterWidth * 0.2f, 0.3f)
            ),
            Actions.parallel(
                Actions.repeat(
                    4, Actions.sequence(
                        Actions.alpha(0.9f, 0.05f),
                        Actions.alpha(1f, 0.05f),
                    )
                ),
                Actions.moveBy(0f, _characterWidth / 7f, 0.2f),
                Actions.scaleBy(0.05f, -0.05f, 0.2f)
            ),
            Actions.alpha(0f, 0.05f),
            Actions.removeActor()
        )
        gTarotEffects.addActor(tarot)
        tarot.addAction(action)
    }

    private fun processDirectedAnimation(
        animation: TarotAnimation.TarotFromSourceDirected,
        event: BattleEvent.AnimateTarotEvent
    ) {
        val sourceUnit = gUnits.findActor<UnitActor>(animation.from.toString())
        val tarot = Image(taTarot.findRegion(event.animation.skin.toString())).apply {
            x = sourceUnit.x + if (sourceUnit.team == 0) _characterWidth * 0.1f else -_characterWidth * 1.1f
            y = sourceUnit.y + _characterWidth * 0.35f
            width = _characterWidth * 0.8f
            height = _characterWidth * 0.8f * 1.66f
            setOrigin(Align.center)
        }
        tarot.alpha = 0f
        tarot.rotation = 270f
        val action = Actions.sequence(
            Actions.parallel(
                Actions.alpha(1f, 0.2f),
                Actions.rotateBy(-270f, 0.2f)
            ),
            Actions.parallel(
                Actions.rotateBy(if (sourceUnit.team == 0) -90f else 90f, 0.3f),
                Actions.moveBy(if (sourceUnit.team == 0) _characterWidth else -_characterWidth, 0.3f)
            ),
            Actions.parallel(
                Actions.scaleTo(0.1f, 10f, 0.1f),
                Actions.alpha(0f, 0.2f)
            ),
            Actions.removeActor()
        )
        tarot.addAction(action)
        gTarotEffects.addActor(tarot)
    }

    private fun processTargetedAnimation(
        animation: TarotAnimation.TarotFromSourceTargets,
        event: BattleEvent.AnimateTarotEvent
    ) {
        val sourceUnit = gUnits.findActor<UnitActor>(animation.from.toString())
        //ok, we have some crazy tarot stuff
        val tarot = Image(taTarot.findRegion(event.animation.skin.toString())).apply {
            x = sourceUnit.x + if (sourceUnit.team == 0) _characterWidth * 0.1f else -_characterWidth * 1.1f
            y = sourceUnit.y + _characterWidth * 0.15f
            width = _characterWidth * 0.8f
            height = _characterWidth * 0.8f * 1.66f
            setOrigin(Align.center)
        }
        tarot.setScale(0.1f)
        tarot.rotation = 180f
        tarot.alpha = 0f
        gTarotEffects.addActor(tarot)
        val actions = animation.targets.map { targetId ->
            gUnits.findActor<UnitActor>(targetId.toString())?.let { targetActor ->
                val rx = targetActor.x + if (targetActor.team == 0) _characterWidth * 0.1f else -_characterWidth * 1.1f
                val ry = targetActor.y + _characterWidth * 0.3f * Random.nextFloat()
                val angle = if (targetActor.team == 0) 30f else -30f
                Actions.sequence(
                    Actions.parallel(
                        Actions.moveTo(rx, ry, 0.2f, SwingOut(1.6f)),
                        Actions.rotateBy(angle, 0.06f, SwingOut(1.6f))
                    ),
                    Actions.rotateTo(-angle / 5f, 0.1f)
                )
            }
        }
        val action = Actions.sequence(
            Actions.parallel(
                Actions.rotateTo(0f, 0.2f, SwingOut(1.6f)),
                Actions.alpha(1f, 0.2f),
                Actions.scaleTo(1f, 1f)
            ),
            SequenceAction().apply { actions.forEach { a -> addAction(a) } },
            Actions.alpha(0f, 0.1f),
            Actions.removeActor()
        )
        tarot.addAction(action)
    }

    private fun proessDestroyEvent(event: BattleEvent.DestroyTileEvent) {
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

    private fun processMergeTileEvent(event: BattleEvent.MergeTileEvent) {
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

    private fun processMoveTileEvent(event: BattleEvent.MoveTileEvent) {
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
            taBattle = taBattle,
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
