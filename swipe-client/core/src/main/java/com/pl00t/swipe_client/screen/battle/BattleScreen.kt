package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.swipe.gestures.SimpleDirectionGestureDetector
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.screen.Router
import com.pl00t.swipe_client.screen.StageScreen
import com.pl00t.swipe_client.services.battle.BattleDecorations
import com.pl00t.swipe_client.services.battle.BattleResult
import com.pl00t.swipe_client.services.battle.BattleService
import com.game7th.swipe.battle.BattleEvent
import com.game7th.swipe.battle.processor.TarotAnimation
import com.pl00t.swipe_client.services.levels.DialogEntryModel
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.ux.require
import kotlinx.coroutines.launch
import ktx.actors.*
import ktx.async.KtxAsync
import kotlin.random.Random

class BattleScreen(
    private val actId: SwipeAct,
    private val levelId: String,
    amCore: AssetManager,
    inputMultiplexer: InputMultiplexer,
    private val levelService: LevelService,
    private val battleService: BattleService,
    private val profileService: ProfileService,
    private val router: Router,
) : StageScreen(amCore, inputMultiplexer), SimpleDirectionGestureDetector.DirectionListener {

    lateinit var decorations: BattleDecorations

    lateinit var battleAssetManager: AssetManager

    lateinit var panelGroup: Group
    lateinit var locationGroup: Group
    lateinit var unitsGroup: Group
    lateinit var tarotEffectsGroup: Group
    lateinit var ultimateEffectsGroup: Group
    lateinit var popupsGroup: Group
    lateinit var ultimateActor: UltimateProgressActor
    lateinit var tileBackgroundsGroup: Group
    lateinit var tilesGroup: MutableList<Group>

    lateinit var gestureDetector: SimpleDirectionGestureDetector

    private val polygonSpriteBatch = PolygonSpriteBatch()

    private var leftUnitsCount = 0
    private var rightUnitsCount = 0
    private var tilesDirty = false

    lateinit var skin: Skin

    private val tileSize = 80f
    val locationHeight = height() - 510f
    val characterWidth = 160f

    private val preBattleDialogs: MutableList<DialogEntryModel> = mutableListOf()

    override fun show() {
        gestureDetector = SimpleDirectionGestureDetector(this)
        multiplexer.addProcessor(root)
        multiplexer.addProcessor(gestureDetector)
        Gdx.input.inputProcessor = multiplexer
        KtxAsync.launch {
            decorations = battleService.createBattle(actId, levelId)
            battleAssetManager = AssetManager().apply {
                load(Atlases.COMMON_BATTLE, TextureAtlas::class.java)
                load(Atlases.ACT(SwipeAct.ACT_1), TextureAtlas::class.java)
                load(Atlases.COMMON_UNITS, TextureAtlas::class.java)
                load(Atlases.COMMON_TAROT, TextureAtlas::class.java)
                load(Atlases.COMMON_UX, TextureAtlas::class.java)
            }
            loadAm(battleAssetManager) { amLoaded() }
        }
    }

    private fun amLoaded() {
        panelGroup = Group()
        locationGroup = Group()
        unitsGroup = Group()
        tarotEffectsGroup = Group()
        ultimateEffectsGroup = Group()
        popupsGroup = Group()
        locationGroup.y = 510f
        popupsGroup.y = tarotEffectsGroup.y

        skin = Skin(Gdx.files.internal("styles/ui.json")).apply {
            addRegions(commonAtlas(Atlases.COMMON_UX))
        }

        val panelImage = Image(commonAtlas(Atlases.COMMON_BATTLE).createPatch("panelBg")).apply {
            width = 480f
            height = 510f
        }
        panelGroup.addActor(panelImage)
        tileBackgroundsGroup = Group().apply {
            x = 40f
            y = 60f
        }
        panelGroup.addActor(tileBackgroundsGroup)
        tilesGroup = mutableListOf()
        (0..10).forEach { i ->
            val tilesLayer = Group().apply {
                x = 40f
                y = 60f
            }
            panelGroup.addActor(tilesLayer)
            tilesGroup.add(tilesLayer)
        }
        root.addActor(locationGroup)
        root.addActor(panelGroup)

        val locationImage = Image(commonAtlas(Atlases.ACT(actId)).findRegion(decorations.background).require()).apply {
            width = 480f
            height = root.height - 480f
            setScaling(Scaling.fit)
        }
        locationGroup.addActor(locationImage)
        locationGroup.addActor(unitsGroup)
        locationGroup.addActor(tarotEffectsGroup)
        locationGroup.addActor(popupsGroup)
        locationGroup.addActor(ultimateEffectsGroup)

        addTileBackgrounds()

        ultimateActor = UltimateProgressActor(this, skin).apply {
            x = 105f
            y = 10f
            touchable = Touchable.enabled
        }
        ultimateActor.onClick {
            println("Ultimate clicked ${ultimateActor.progress}")
            if (ultimateActor.progress == 1f) {
                KtxAsync.launch { battleService.processUltimate() }
            }
        }
        panelGroup.addActor(ultimateActor)

        KtxAsync.launch {
            val level = levelService.getLevelDetails(actId, levelId)
            preBattleDialogs.addAll(level.dialog)
            resolvePreBattleDialogs()
        }
    }

    private fun resolvePreBattleDialogs() {
        if (preBattleDialogs.isEmpty()) {
            connectBattle()
        } else {
            val nextDialog = preBattleDialogs.removeFirst()
            val actor = BattleDialogActor(nextDialog, this, skin, this::resolvePreBattleDialogs)
            root.addActor(actor)
        }
    }

    private fun connectBattle() {
        KtxAsync.launch { observeBattleEvents() }
        KtxAsync.launch { observeEndBattle() }
    }

    private suspend fun observeBattleEvents() {
        battleService.events().collect { event ->
            processEvent(event)
        }
    }

    private suspend fun observeEndBattle() {
        battleService.battleEnd().collect { event ->
            processBattleEvent(event)
        }
    }

    private fun processBattleEvent(event: BattleResult) {
        println("battle end: $event")
        //we are finished, remove multiplexor
        multiplexer.removeProcessor(gestureDetector)

        val endActor = BattleFinishActor(actId, levelId, profileService, event, this@BattleScreen, skin, router).apply {
            x = 40f
            y = (height() - 720f) / 2f
        }
        root.addActor(endActor)
    }

    private fun processEvent(event: BattleEvent) {
        when (event) {
            is BattleEvent.WaveEvent -> {
                rightUnitsCount = 0
                val wave = Label("Wave ${event.wave}", skin, "wave_caption").apply {
                    width = root.width
                    height = root.height * 0.2f
                    y = root.height * 0.2f
                    alpha = 0f
                    setAlignment(Align.bottom)
                }
                root.addActor(wave)
                wave.addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.alpha(1f, 0.2f),
                        Actions.moveBy(0f, root.height * 0.6f, 2f)
                    ),
                    Actions.alpha(0f, 0.5f),
                    Actions.removeActor()
                ))
            }

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
                        processTargetedAnimation(event.animation as TarotAnimation.TarotFromSourceTargets, event)
                    }

                    is TarotAnimation.TarotFromSourceDirected -> {
                        processDirectedAnimation(event.animation as TarotAnimation.TarotFromSourceDirected, event)
                    }

                    is TarotAnimation.TarotAtSourceRotate -> {
                        processStaticAnimation(event.animation as TarotAnimation.TarotAtSourceRotate, event)
                    }

                    else -> {}
                }
            }

            is BattleEvent.UnitHealthEvent -> {
                unitsGroup.findActor<UnitActor>(event.unitId.toString())?.healthBar?.updateHealth(event.health)
            }

            is BattleEvent.UnitDeathEvent -> {
                unitsGroup.findActor<UnitActor>(event.unitId.toString())?.let { actor ->
                    val action = Actions.sequence(
                        Actions.alpha(0f, 0.4f),
                        Actions.removeActor()
                    )
                    actor.addAction(action)
                }
            }

            is BattleEvent.UnitPopupEvent -> {
                unitsGroup.findActor<UnitActor>(event.unitId.toString())?.let { unitActor ->
                    val popupActor = PopupActor(
                        event.popup.text,
                        event.popup.icons,
                        this@BattleScreen,
                        skin,
                    ).apply {
                        x = unitActor.x - if (unitActor.team == 0) 0f else characterWidth
                        y = unitActor.y + characterWidth * 1.66f * unitActor.s * 0.8f
                    }
                    popupsGroup.alpha = 0f
                    popupsGroup.addActor(popupActor)
                    val action = Actions.sequence(
                        Actions.delay(unitActor.popupDelay),
                        Actions.run { popupsGroup.alpha = 1f },
                        Actions.moveBy(0f, characterWidth / 3f, 1.5f),
                        Actions.alpha(0f, 0.2f),
                        Actions.removeActor()
                    )
                    unitActor.popupDelay += 0.4f
                    popupActor.addAction(action)
                }
            }

            is BattleEvent.UltimateProgressEvent -> {
                ultimateActor.updateUltimateProgress(event.progress.toFloat() / event.maxProgress)
            }
            is BattleEvent.UltimateEvent -> {
                val bg = Image(commonAtlas(Atlases.COMMON_UX).findRegion("black_pixel").require()).apply {
                    width = root.width
                    height = height() - 480f
                    alpha = 0f
                }
                bg.addAction(Actions.sequence(
                    Actions.alpha(1f, 0.5f),
                    Actions.delay(2f),
                    Actions.alpha(0f, 0.5f),
                    Actions.removeActor()
                ))
                val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(event.skin.toString()).require()).apply {
                    width = characterWidth
                    height = characterWidth * 1.66f
                    x = (480f - this.width) / 2f
                    y = (locationHeight - this.height) / 2f - locationHeight
                    setOrigin(Align.center)
                    setScale(5f)
                    alpha = 0f
                }
                tarot.addAction(Actions.sequence(
                    Actions.delay(0.5f),
                    Actions.parallel(
                        Actions.moveBy(0f, locationHeight, 0.8f),
                        Actions.alpha(1f, 0.6f),
                        Actions.scaleTo(1f, 1f, 0.8f)
                    ),
                    Actions.sequence(
                        Actions.scaleBy(0.1f, 0.1f, 0.1f),
                        Actions.scaleBy(-0.1f, -0.1f, 0.1f)
                    ).repeat(6),
                    Actions.parallel(
                        Actions.sequence(
                            Actions.parallel(
                                Actions.alpha(0f, 0.5f),
                                Actions.scaleBy(5f, 5f, 0.5f),
                                Actions.moveBy(0f, -locationHeight, 0.5f)
                            ),
                            Actions.removeActor()
                        ),
                        Actions.run {
                            event.events.forEach { processEvent(it) }
                        }
                    ),
                ))
                ultimateEffectsGroup.addActor(bg)
                ultimateEffectsGroup.addActor(tarot)
            }
            is BattleEvent.TileEffectEvent -> {
                val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(event.skin.toString()).require()).apply {
                    height = tileSize
                    width = tileSize * 0.66f
                    x = tileSize * event.x + (tileSize - this.width) / 2f
                    y = tileSize * event.y
                    setOrigin(Align.center)
                    rotation = 45f
                    setScale(4f, 4f)
                    alpha = 0f
                }
                tarot.addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.rotateBy(-60f, 0.4f),
                        Actions.scaleTo(1.5f, 1.5f, 0.4f),
                        Actions.alpha(1f)
                    ),
                    Actions.sequence(
                        Actions.scaleTo(1.55f, 1.55f, 0.03f),
                        Actions.scaleTo(1.44f, 1.44f, 0.03f)
                    ).repeat(3),
                    Actions.alpha(0f, 0.2f),
                    Actions.removeActor()
                ))
                tilesGroup.last().addActor(tarot)
            }

            else -> Unit
        }
    }

    private fun processStaticAnimation(
        animation: TarotAnimation.TarotAtSourceRotate,
        event: BattleEvent.AnimateTarotEvent
    ) {
        val sourceUnit = unitsGroup.findActor<UnitActor>(animation.at.toString()) ?: return
        //ok, we have some crazy tarot stuff
        val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(event.animation.skin.toString()).require()).apply {
            x = sourceUnit.x + if (sourceUnit.team == 0) characterWidth * 0.1f else -characterWidth * 1.1f
            y = sourceUnit.y + characterWidth * 0.35f
            width = characterWidth * 0.8f
            height = characterWidth * 0.8f * 1.66f
            setOrigin(Align.center)
        }
        tarot.scaleX = 0.1f
        tarot.scaleY = 0.1f
        tarot.alpha = 0f
        val action = Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(1f, 1f, 0.3f),
                Actions.alpha(0.8f, 0.3f),
                Actions.moveBy(0f, -characterWidth * 0.2f, 0.3f)
            ),
            Actions.parallel(
                Actions.repeat(
                    4, Actions.sequence(
                        Actions.alpha(0.9f, 0.05f),
                        Actions.alpha(1f, 0.05f),
                    )
                ),
                Actions.moveBy(0f, characterWidth / 7f, 0.4f),
                Actions.scaleBy(0.05f, -0.05f, 0.3f)
            ),
            Actions.alpha(0f, 0.05f),
            Actions.removeActor()
        )
        tarotEffectsGroup.addActor(tarot)
        tarot.addAction(action)
    }

    private fun processDirectedAnimation(
        animation: TarotAnimation.TarotFromSourceDirected,
        event: BattleEvent.AnimateTarotEvent
    ) {
        val sourceUnit = unitsGroup.findActor<UnitActor>(animation.from.toString()) ?: return
        val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(event.animation.skin.toString()).require()).apply {
            x = sourceUnit.x + if (sourceUnit.team == 0) characterWidth * 0.1f else -characterWidth * 1.1f
            y = sourceUnit.y + characterWidth * 0.35f
            width = characterWidth * 0.8f
            height = characterWidth * 0.8f * 1.66f
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
                Actions.moveBy(if (sourceUnit.team == 0) characterWidth else -characterWidth, 0.3f)
            ),
            Actions.parallel(
                Actions.scaleTo(0.1f, 10f, 0.1f),
                Actions.alpha(0f, 0.2f)
            ),
            Actions.removeActor()
        )
        tarot.addAction(action)
        tarotEffectsGroup.addActor(tarot)
    }

    private fun processTargetedAnimation(
        animation: TarotAnimation.TarotFromSourceTargets,
        event: BattleEvent.AnimateTarotEvent
    ) {
        val sourceUnit = unitsGroup.findActor<UnitActor>(animation.from.toString()) ?: return
        //ok, we have some crazy tarot stuff
        val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(event.animation.skin.toString()).require()).apply {
            x = sourceUnit.x + if (sourceUnit.team == 0) characterWidth * 0.1f else -characterWidth * 1.1f
            y = sourceUnit.y + characterWidth * 0.15f
            width = characterWidth * 0.8f
            height = characterWidth * 0.8f * 1.66f
            setOrigin(Align.center)
        }
        tarot.setScale(0.1f)
        tarot.rotation = 180f
        tarot.alpha = 0f
        tarotEffectsGroup.addActor(tarot)
        val actions = animation.targets.mapNotNull { targetId ->
            unitsGroup.findActor<UnitActor>(targetId.toString())?.let { targetActor ->
                val rx = targetActor.x + if (targetActor.team == 0) characterWidth * 0.1f else -characterWidth * 1.1f
                val ry = targetActor.y + characterWidth * 0.3f * Random.nextFloat()
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
        val actor = tilesGroup[event.layer].findActor<TileActor>(event.id.toString())
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
        val tile1 = tilesGroup[event.layer].findActor<TileActor>(event.id.toString())
        val tile2 = tilesGroup[event.layer].findActor<TileActor>(event.to.toString())
        if (tile1 == null || tile2 == null) return
        tile1.updateXY(event.tox, event.toy)
        tile1.zIndex = 0
        tile1.addAction(
            MoveToAction().apply {
                duration = 0.25f
                setPosition(event.ttox * tileSize, event.ttoy * tileSize)
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
                            setPosition(event.tox * tileSize, event.toy * tileSize)
                        })
                    }
                }
            })
        )
    }

    private fun processMoveTileEvent(event: BattleEvent.MoveTileEvent) {
        tilesGroup[event.layer].findActor<TileActor>(event.id.toString())?.let { tileActor ->
            val tx = event.tox * tileSize
            val ty = event.toy * tileSize
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
            size = tileSize,
            strokeWidth = 9f,
            cardTexture = event.skin.toString(),
            taBattle = commonAtlas(Atlases.COMMON_BATTLE),
            taTarot = commonAtlas(Atlases.COMMON_TAROT),
            polygonBatch = polygonSpriteBatch,
            gridX = event.x,
            gridY = event.y,
            type = event.type,
        )
        tile.name = event.id.toString()
        placeTile(tile)
        tilesGroup[event.layer].addActor(tile)
        tile.animateAppear()
    }

    private fun createUnit(event: BattleEvent.CreateUnitEvent) {
        val unit = UnitActor(
            id = event.id,
            health = event.health,
            maxHealth = event.maxHealth,
            effects = event.effects,
            context = this,
            skin = skin,
            texture = event.skin.toString(),
            team = event.team,
            w = characterWidth,
            s = event.scale,
            position = if (event.team == 0) leftUnitsCount++ else rightUnitsCount++
        )
        unit.name = event.id.toString()
        placeUnit(unit)
        unitsGroup.addActor(unit)
        unit.animateAppear()
    }

    private fun placeTile(tile: TileActor) {
//        tile.clearActions()
        tile.x = tileSize * tile.gridX
        tile.y = tileSize * tile.gridY
    }

    private fun placeUnit(unit: UnitActor) {
        unit.x = if (unit.team == 0) {
            0.6f * characterWidth * unit.position
        } else {
            root.width - 0.6f * characterWidth * unit.position
        }
    }

    private fun addTileBackgrounds() {
        (0 until 25).forEach { index ->
            val x = index % 5
            val y = index / 5

            val bg = Image(commonAtlas(Atlases.COMMON_BATTLE).findRegion("tile_bg").require()).apply {
                this.x = x * tileSize
                this.y = y * tileSize
                this.width = tileSize
                this.height = tileSize
            }
            tileBackgroundsGroup.addActor(bg)
        }
    }

    override fun hide() = dispose()

    override fun dispose() {
        super.dispose()
        multiplexer.removeProcessor(gestureDetector)
        battleAssetManager.dispose()
    }

    override fun onLeft() = processSwipe(-1, 0)
    override fun onRight() = processSwipe(1, 0)
    override fun onUp() = processSwipe(0, 1)
    override fun onDown() = processSwipe(0, -1)

    override fun profileService() = profileService

    private fun processSwipe(dx: Int, dy: Int) {
        KtxAsync.launch { battleService.processSwipe(dx, dy) }
    }
}
