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
import com.game7th.swipe.game.SbBattleFieldDisplayEffect
import com.game7th.swipe.game.SbDisplayEvent
import com.game7th.swipe.game.SbTileFieldDisplayEffect
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
        (0..3).forEach { i ->
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

    private fun processEvent(event: SbDisplayEvent) {
        when (event) {
            is SbDisplayEvent.SbWave -> {
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

            is SbDisplayEvent.SbCreateCharacter -> {
                createUnit(event)
            }

            is SbDisplayEvent.SbCreateTile -> {
                createTile(event)
            }

            is SbDisplayEvent.SbMoveTile -> {
                processMoveTileEvent(event)
            }

            is SbDisplayEvent.SbDestroyTile -> {
                processDestroyTile(event)
            }

            is SbDisplayEvent.SbUpdateCharacter -> {
                val personage = event.personage
                unitsGroup.findActor<UnitActor>(personage.id.toString())?.let { actor ->
                    actor.healthBar.updateHealth(personage.health, personage.maxHealth)
                    if (personage.id == 0) {
                        ultimateActor.updateUltimateProgress(personage.ultimateProgress.toFloat() / personage.maxUltimateProgress)
                    }
                }
            }

            is SbDisplayEvent.SbShowPopup -> {
                unitsGroup.findActor<UnitActor>(event.characterId.toString())?.let { unitActor ->
                    val popupActor = PopupActor(
                        event.text,
                        event.icons,
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

            is SbDisplayEvent.SbDestroyCharacter -> {
                unitsGroup.findActor<UnitActor>(event.id.toString())?.let { actor ->
                    val action = Actions.sequence(
                        Actions.alpha(0f, 0.4f),
                        Actions.removeActor()
                    )
                    actor.addAction(action)
                }
            }

            is SbDisplayEvent.SbShowTarotEffect -> {
                when (event.effect) {
                    is SbBattleFieldDisplayEffect.TarotSimpleAttack -> processTargetedAnimation(event.effect as SbBattleFieldDisplayEffect.TarotSimpleAttack)
                    is SbBattleFieldDisplayEffect.TarotDirectedAoe -> processDirectedAnimation(event.effect as SbBattleFieldDisplayEffect.TarotDirectedAoe)
                    is SbBattleFieldDisplayEffect.TarotUltimate -> processUltimateAnimation(event.effect as SbBattleFieldDisplayEffect.TarotUltimate)
                    is SbBattleFieldDisplayEffect.TarotStatic -> processStaticAnimation(event.effect as SbBattleFieldDisplayEffect.TarotStatic)
                }
            }

            is SbDisplayEvent.SbShowTileFieldEffect -> {
                when (event.effect) {
                    is SbTileFieldDisplayEffect.TarotOverPosition -> processTarotOverAnimation(
                        (event.effect as SbTileFieldDisplayEffect.TarotOverPosition).x,
                        (event.effect as SbTileFieldDisplayEffect.TarotOverPosition).y,
                        (event.effect as SbTileFieldDisplayEffect.TarotOverPosition).skin)
                }
            }

            else -> Unit
        }
    }

    private fun processTarotOverAnimation(
        x: Int,
        y: Int,
        skin: String
    ) {
        val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(skin)).apply {
            height = tileSize
            width = tileSize * 0.66f
            this.x = tileSize * x + (tileSize - this.width) / 2f
            this.y = tileSize * y
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

    private fun processUltimateAnimation(
        animation: SbBattleFieldDisplayEffect.TarotUltimate
    ) {
        val bg = Image(commonAtlas(Atlases.COMMON_UX).findRegion("black_pixel").require()).apply {
            width = root.width
            height = height() - 480f
            alpha = 0f
        }
        bg.addAction(
            Actions.sequence(
                Actions.alpha(1f, 0.5f),
                Actions.delay(2f),
                Actions.alpha(0f, 0.5f),
                Actions.removeActor()
            )
        )
        val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(animation.skin).require()).apply {
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
            Actions.sequence(
                Actions.parallel(
                    Actions.alpha(0f, 0.5f),
                    Actions.scaleBy(5f, 5f, 0.5f),
                    Actions.moveBy(0f, -locationHeight, 0.5f)
                ),
                Actions.removeActor()
            )
        ))
        ultimateEffectsGroup.addActor(bg)
        ultimateEffectsGroup.addActor(tarot)
    }

    private fun processStaticAnimation(
        animation: SbBattleFieldDisplayEffect.TarotStatic
    ) {
        val sourceUnit = unitsGroup.findActor<UnitActor>(animation.at.toString()) ?: return
        //ok, we have some crazy tarot stuff
        val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(animation.skin).require()).apply {
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
        animation: SbBattleFieldDisplayEffect.TarotDirectedAoe
    ) {
        val sourceUnit = unitsGroup.findActor<UnitActor>(animation.from.toString()) ?: return
        val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(animation.skin).require()).apply {
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
        animation: SbBattleFieldDisplayEffect.TarotSimpleAttack
    ) {
        val sourceUnit = unitsGroup.findActor<UnitActor>(animation.from.toString()) ?: return
        //ok, we have some crazy tarot stuff
        val tarot = Image(commonAtlas(Atlases.COMMON_TAROT).findRegion(animation.skin.toString()).require()).apply {
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
        val actions = animation.to.let { listOf(it) }.mapNotNull { targetId ->
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

    private fun processDestroyTile(event: SbDisplayEvent.SbDestroyTile) {
        val actor = tilesGroup[event.z].findActor<TileActor>(event.tileId.toString())
        actor?.addAction(
            Actions.sequence(
                Actions.delay(0.2f),
                Actions.run { actor.arcVisible = false },
                Actions.parallel(
                    Actions.scaleBy(0.2f, 0.2f, 0.1f),
                    Actions.alpha(0f, 0.1f)
                ),
                Actions.removeActor()
            )
        )
    }

    private fun processMoveTileEvent(event: SbDisplayEvent.SbMoveTile) {
        tilesGroup[event.z].findActor<TileActor>(event.tileId.toString())?.let { tileActor ->
            val tx = event.tox * tileSize
            val ty = event.toy * tileSize
            tileActor.updateXY(event.tox, event.toy)
            if (event.targetTile != null) {
                tilesGroup[event.targetTile!!.z].findActor<TileActor>(event.targetTile!!.id.toString())
                    ?.let { targetTile ->
                        targetTile.addAction(
                            Actions.sequence(
                                Actions.delay(0.3f),
                                Actions.run {
                                    targetTile.updateSectors(event.targetTile!!.progress)
                                }
                            )
                        )
                    }
            }
            tileActor.addAction(Actions.moveTo(tx, ty, 0.3f, SwingOut(1.6f)))
            if (event.remainder == null) {
                tileActor.addAction(
                    Actions.sequence(
                        Actions.delay(0.2f),
                        Actions.removeActor()
                    )
                )
            } else {
                if (event.remainder!!.x != event.tox || event.remainder!!.y != event.toy) {
                    val btx = event.remainder!!.x * tileSize
                    val bty = event.remainder!!.y * tileSize
                    tileActor.addAction(Actions.sequence(
                        Actions.delay(0.2f),
                        Actions.moveTo(btx, bty, 0.09f),
                        Actions.run {
                            tileActor.updateSectors(event.remainder!!.progress)
                        }
                    ))
                }
            }
        }
    }

    private fun createTile(event: SbDisplayEvent.SbCreateTile) {
        val tile = TileActor(
            sectors = event.tile.progress,
            maxSectors = event.tile.maxProgress,
            size = tileSize,
            strokeWidth = 9f,
            cardTexture = event.tile.skin,
            taBattle = commonAtlas(Atlases.COMMON_BATTLE),
            taTarot = commonAtlas(Atlases.COMMON_TAROT),
            polygonBatch = polygonSpriteBatch,
            gridX = event.tile.x,
            gridY = event.tile.y,
            type = event.tile.type,
        )
        tile.name = event.tile.id.toString()
        placeTile(tile)
        tilesGroup[event.tile.z].addActor(tile)
        tile.animateAppear()
    }

    private fun createUnit(event: SbDisplayEvent.SbCreateCharacter) {
        val unit = UnitActor(
            id = event.personage.id,
            health = event.personage.health,
            maxHealth = event.personage.maxHealth,
            effects = event.personage.effects,
            context = this,
            skin = skin,
            texture = event.personage.skin,
            team = event.personage.team,
            w = characterWidth,
            s = event.personage.scale,
            position = if (event.personage.team == 0) leftUnitsCount++ else rightUnitsCount++
        )
        unit.name = event.personage.id.toString()
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
