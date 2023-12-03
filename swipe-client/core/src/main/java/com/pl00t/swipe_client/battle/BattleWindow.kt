package com.pl00t.swipe_client.battle

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Interpolation.SwingIn
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.swipe.game.*
import com.game7th.swipe.gestures.SimpleDirectionGestureDetector
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.animation.AnimationActor
import com.pl00t.swipe_client.services.battle.BattleDecorations
import com.pl00t.swipe_client.services.battle.EncounterResultModel
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
import ktx.actors.repeat
import ktx.async.KtxAsync

class BattleWindow(
    private val r: Resources,
    private val hideBattleScreen: (EncounterResultModel.BattleResult) -> Unit
) : Group(), SimpleDirectionGestureDetector.DirectionListener {
    lateinit var decorations: BattleDecorations

    lateinit var panelGroup: Group
    lateinit var locationGroup: Group
    lateinit var unitsGroup: Group
    lateinit var healthbarGroup: Group
    lateinit var tarotEffectsGroup: Group
    lateinit var ultimateEffectsGroup: Group
    lateinit var popupsGroup: Group
    lateinit var ultimateActor: UltimateProgressActor
    lateinit var tileBackgroundsGroup: Group
    lateinit var tilesGroup: MutableList<Group>
    lateinit var tileFieldEffectsGroup: Group

    lateinit var gestureDetector: SimpleDirectionGestureDetector

    private val polygonSpriteBatch = PolygonSpriteBatch()

    private var leftUnitsCount = 0
    private var rightUnitsCount = 0
    private var tilesDirty = false

    lateinit var music: Music

    private val tileSize = 80f
    val locationHeight = r.height - 510f
    val characterWidth = 100f

    lateinit var actId: SwipeAct
    lateinit var levelId: String

    private var ignoreSwipe = false

    init {
        gestureDetector = SimpleDirectionGestureDetector(this)
        r.inputMultiplexer.addProcessor(gestureDetector)

        KtxAsync.launch {
            decorations = r.battleService.getDecorations()
            r.loadAtlas(Resources.battle_atlas)
            r.loadAtlas(Resources.ux_atlas)
            r.loadAtlas(Resources.skills_atlas)
            r.loadMusic(decorations.music)
            SbSoundType.values().filter { it.battle }.forEach {
                r.loadSound(it)
            }

            r.onLoad {
                r.music("theme_global").stop()
                amLoaded()
            }
        }
    }

    private fun amLoaded() {
        panelGroup = Group()
        locationGroup = Group().apply { setSize(r.width, r.height - 510f) }
        unitsGroup = Group()
        healthbarGroup = Group()
        tarotEffectsGroup = Group()
        ultimateEffectsGroup = Group()
        popupsGroup = Group()
        locationGroup.y = 510f
        popupsGroup.y = tarotEffectsGroup.y

        music = r.music(decorations.music)
        music.isLooping = true
        if (!Debug.NoMusic) music.play()

        KtxAsync.launch {

            val panelImage = Image(r.atlas(Resources.battle_atlas).createPatch("panelBg")).apply {
                width = 480f
                height = 510f
            }
            panelGroup.addActor(panelImage)
            tileBackgroundsGroup = Group().apply {
                setPosition(40f, 60f)
                setSize(5 * tileSize, 5 * tileSize)
            }
            tileFieldEffectsGroup = Group().apply {
                setPosition(40f, 60f)

            }
            panelGroup.addActor(tileBackgroundsGroup)
            tilesGroup = mutableListOf()
            (0..4).forEach { i ->
                val tilesLayer = Group().apply {
                    x = 40f
                    y = 60f
                }
                panelGroup.addActor(tilesLayer)
                tilesGroup.add(tilesLayer)
            }
            panelGroup.addActor(tileFieldEffectsGroup)
            addActor(locationGroup)
            addActor(panelGroup)

            actId = r.battleService.getActId()
            levelId = r.battleService.getLevelId()

            val locationImage = r.image(Resources.actAtlas(actId), decorations.background).apply {
                width = 480f
                height = r.height - 510f
                setScaling(Scaling.stretch)
            }
            locationGroup.addActor(locationImage)
            locationGroup.addActor(unitsGroup)
            locationGroup.addActor(tarotEffectsGroup)
            locationGroup.addActor(popupsGroup)
            locationGroup.addActor(ultimateEffectsGroup)
            locationGroup.addActor(healthbarGroup)

            addTileBackgrounds()

            ultimateActor = UltimateProgressActor(r).apply {
                x = 90f
                y = 10f
                touchable = Touchable.enabled
            }
            ultimateActor.onClick {
                println("Ultimate clicked ${ultimateActor.progress}")
                if (ultimateActor.progress == 1f) {
                    KtxAsync.launch { r.battleService.processUltimate() }
                }
            }
            panelGroup.addActor(ultimateActor)

            val dialog = r.profileService.getDialogScript("$actId.$levelId")
            if (dialog.replicas.isNotEmpty()) {
                ignoreSwipe = true
                val dialog = DialogScriptActor(r, dialog) {
                    connectBattle()
                    ignoreSwipe = false
                    checkTutorials()
                }
                addActor(dialog)
            } else {
                connectBattle()
            }
        }
    }

    private fun connectBattle() {
        KtxAsync.launch { observeBattleEvents() }
        KtxAsync.launch { observeEndBattle() }
    }

    private suspend fun observeBattleEvents() {
        r.battleService.events().collect { event ->
            processEvent(event)
        }
    }

    private suspend fun observeEndBattle() {
        r.battleService.battleEnd().collect { event ->
            delay(2_000L)
            processBattleEvent(event)
        }
    }

    private fun processBattleEvent(event: EncounterResultModel.BattleResult) {
        println("battle end: $event")

        addAction(Actions.sequence(
            Actions.delay(1f),
            Actions.run { dispose(event) }
        ))
    }

    private fun processEvent(event: SbDisplayEvent) {
        when (event) {
            is SbDisplayEvent.SbWave -> {
                KtxAsync.launch {
                    if (event.wave == 2 && !r.profileService.getTutorial().a1c3w2) {
                        ignoreSwipe = true
                        addActor(DialogScriptActor(r, r.profileService.getDialogScript("ACT_1.c3.w2")) {
                            ignoreSwipe = false
                            r.profileService.saveTutorial(r.profileService.getTutorial().copy(a1c3w2 = true))
                        })
                    }
                }
                rightUnitsCount = 0
                val wave = r.regular24Focus(UiTexts.WaveTemplate.value(r.l).replace("$", event.wave.toString())).apply {
                    setSize(400f, 60f)
                    setOrigin(Align.center)
                    setAlignment(Align.center)
                    setFontScale(1.6f)
                }
                val bg = r.image(Resources.ux_atlas, "background_black").apply {
                    setSize(wave.width, wave.height)
                    alpha = 0.6f
                }
                val g = Group().apply {
                    setPosition(40f, locationGroup.y + characterWidth * 1.66f)
                }
                g.addActor(bg)
                g.addActor(wave)
                addActor(g)
                g.addAction(
                    Actions.sequence(
                        Actions.alpha(0f),
                        Actions.moveBy(0f, -40f),
                        Actions.parallel(
                            Actions.alpha(1f, 0.3f),
                            Actions.moveBy(0f, 40f, 0.3f, SwingOut(1.6f))
                        ),
                        Actions.sequence(
                            Actions.alpha(0.6f, 0.05f),
                            Actions.alpha(1f, 0.05f)
                        ).repeat(8),
                        Actions.parallel(
                            Actions.moveBy(0f, 100f, 2f),
                            Actions.alpha(0f, 2f)
                        ),
                        Actions.removeActor()
                    )
                )
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
                    healthbarGroup.findActor<UnitHealthBarActor>(personage.id.toString())?.let { healthBar ->
                        healthBar.updateHealth(personage.health, personage.maxHealth)
                    }
                    if (personage.id == 0) {
                        ultimateActor.updateUltimateProgress(personage.ultimateProgress.toFloat() / personage.maxUltimateProgress)
                    }
                }
            }

            is SbDisplayEvent.SbShowPopup -> {
                unitsGroup.findActor<UnitActor>(event.characterId.toString())?.let { unitActor ->
                    val popupActor = PopupActor(
                        r,
                        event.text,
                        event.icons,
                    ).apply {
                        x = unitActor.x - if (unitActor.team == 0) 0f else characterWidth
                        y = unitActor.y + characterWidth * 1.66f * unitActor.s * 0.8f
                    }
                    popupsGroup.alpha = 0f
                    popupsGroup.addActor(popupActor)
                    val action = Actions.sequence(
                        Actions.delay(unitActor.popupDelay),
                        Actions.run { popupsGroup.alpha = 1f },
                        Actions.moveBy(0f, characterWidth / 3f, 1f),
                        Actions.alpha(0f, 0.6f),
                        Actions.removeActor()
                    )
                    unitActor.popupDelay += 0.5f
                    popupActor.addAction(action)
                }
                event.sound?.let { r.playSound(it) }
            }

            is SbDisplayEvent.SbDestroyCharacter -> {
                unitsGroup.findActor<UnitActor>(event.id.toString())?.let { actor ->
                    healthbarGroup.findActor<UnitHealthBarActor>(event.id.toString())?.let {
                        it.addAction(Actions.sequence(
                            Actions.alpha(0f, 0.4f),
                            Actions.removeActor()
                        ))
                    }
                    r.playSound(if (actor.team == 0) SbSoundType.DEATH_HERO else SbSoundType.DEATH)
                    val action = Actions.sequence(
                        Actions.alpha(0f, 0.4f),
                        Actions.removeActor()
                    )
                    actor.addAction(action)
                }
            }

            is SbDisplayEvent.SbShowTarotEffect -> {
                when (event.effect) {
                    is SbBattleFieldDisplayEffect.SimpleAttackEffect -> processTargetedAnimation(event.effect as SbBattleFieldDisplayEffect.SimpleAttackEffect)
                    is SbBattleFieldDisplayEffect.DirectedAoeEffect -> processDirectedAnimation(event.effect as SbBattleFieldDisplayEffect.DirectedAoeEffect)
                    is SbBattleFieldDisplayEffect.UltimateEffect -> processUltimateAnimation(event.effect as SbBattleFieldDisplayEffect.UltimateEffect)
                    is SbBattleFieldDisplayEffect.TarotStatic -> processStaticAnimation(event.effect as SbBattleFieldDisplayEffect.TarotStatic)
                }
                event.sound?.let { r.playSound(it) }
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
        val tarot = r.image(Resources.skills_atlas, skin).apply {
            height = tileSize
            width = tileSize
            this.x = tileSize * x
            this.y = tileSize * y
            setOrigin(Align.center)
            setScale(1.6f, 1.6f)
            alpha = 0f
        }
        tarot.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.alpha(0.3f, 0.2f),
                    Actions.scaleTo(1.2f, 1.2f, 0.2f)
                ),
                Actions.sequence(
                    Actions.alpha(0.4f, 0.05f),
                    Actions.alpha(0.3f, 0.05f)
                ).repeat(6),
                Actions.alpha(0f, 0.2f),
                Actions.removeActor()
            )
        )
        tileFieldEffectsGroup.addActor(tarot)
    }

    private fun processUltimateAnimation(
        animation: SbBattleFieldDisplayEffect.UltimateEffect
    ) {
        val bg = r.image(Resources.ux_atlas, "background_black").apply {
            width = r.width
            height = r.height - 480f
            y = 480f
            alpha = 0f
        }
        bg.addAction(Actions.sequence(
            Actions.alpha(0.8f, 0.5f),
            Actions.delay(0.5f),
            Actions.alpha(0f, 0.5f),
            Actions.removeActor()
        ))
        val animationActor = AnimationActor(r.atlas(Resources.battle_atlas), "Air07fill", 15f, Color(animation.color)).apply {
            setSize(614f, 345f)
            setPosition(-67f, -30f)
        }
        val tile = r.image(Resources.skills_atlas, animation.skin).apply {
            setPosition(140f, -200f)
            setSize(200f, 200f)
        }
        tile.addAction(Actions.sequence(
            Actions.alpha(0f),
            Actions.parallel(
                Actions.alpha(1f, 0.3f),
                Actions.moveBy(0f, 250f, 0.3f),
            ),
            Actions.sequence(
                Actions.alpha(0.4f, 0.05f),
                Actions.alpha(1f, 0.05f)
            ).repeat(6),
            Actions.parallel(
                Actions.alpha(0f, 0.3f),
                Actions.moveBy(0f, 250f, 0.3f)
            ),
            Actions.removeActor()
        ))
        tarotEffectsGroup.addActor(bg)
        tarotEffectsGroup.addActor(tile)
        tarotEffectsGroup.addActor(animationActor)
    }

    private fun processStaticAnimation(
        animation: SbBattleFieldDisplayEffect.TarotStatic
    ) {
        val sourceUnit = unitsGroup.findActor<UnitActor>(animation.at.toString()) ?: return
        //ok, we have some crazy tarot stuff
        val tarot = r.image(Resources.skills_atlas, animation.skin).apply {
            x = sourceUnit.x + characterWidth * 0.1f
            y = sourceUnit.y + characterWidth * 1.3f
            width = characterWidth * 0.8f
            height = characterWidth * 0.8f
            setOrigin(Align.center)
        }
        tarot.scaleX = 0.1f
        tarot.scaleY = 0.1f
        tarot.alpha = 0f
        val action = Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(1f, 1f, 0.6f),
                Actions.alpha(0.8f, 0.6f),
                Actions.moveBy(0f, -characterWidth * 0.2f, 0.6f)
            ),
            Actions.parallel(
                Actions.repeat(
                    8, Actions.sequence(
                        Actions.alpha(0.9f, 0.05f),
                        Actions.alpha(1f, 0.05f),
                    )
                ),
                Actions.moveBy(0f, characterWidth / 7f, 0.8f),
                Actions.scaleBy(0.05f, -0.05f, 0.6f)
            ),
            Actions.alpha(0f, 0.1f),
            Actions.removeActor()
        )
        tarotEffectsGroup.addActor(tarot)
        tarot.addAction(action)
    }

    private fun processDirectedAnimation(
        animation: SbBattleFieldDisplayEffect.DirectedAoeEffect
    ) {
        val sourceUnit = unitsGroup.findActor<UnitActor>(animation.from.toString()) ?: return
        val size = characterWidth * 0.5f
        //ok, we have some crazy tarot stuff
        val tarot = r.image(Resources.skills_atlas, animation.skin).apply {
            x = sourceUnit.x + (characterWidth * 1.4f - size) / 2f
            y = sourceUnit.y + (characterWidth * 1.4f * 1.66f - size) / 2f
            width = size
            height = size
            setOrigin(Align.center)
            setScale(2f)
            alpha = 0f
        }
        tarotEffectsGroup.addActor(tarot)
        tarot.addAction(Actions.sequence(
            Actions.parallel(
                Actions.alpha(0.8f, 0.2f),
                Actions.scaleTo(1.5f, 1.5f, 0.2f),
                Actions.moveBy(if (animation.team == 0) characterWidth else -characterWidth, 0f, 0.2f)
            ),
            Actions.run {
                val animationActor = AnimationActor(r.atlas(Resources.battle_atlas), "energy26", 30f, animation.color?.let { Color(it) },animation.team == 1).apply {
                    val actorSize = 5f * characterWidth
                    setPosition(if (animation.team == 0) sourceUnit.x + characterWidth * 0.7f else (sourceUnit.x - actorSize), (characterWidth * 1.4f * 1.6f * sourceUnit.s - actorSize) * 0.35f)
                    setSize(actorSize, actorSize)
                }
                tarotEffectsGroup.addActor(animationActor)
            },
            Actions.sequence(
                Actions.alpha(0.5f, 0.05f),
                Actions.alpha(0.8f, 0.05f)
            ).repeat(8),
            Actions.alpha(0f, 0.2f),
            Actions.removeActor()
        ))
    }

    private fun processTargetedAnimation(
        animation: SbBattleFieldDisplayEffect.SimpleAttackEffect
    ) {
        val sourceUnit = unitsGroup.findActor<UnitActor>(animation.from.toString()) ?: return
        val size = characterWidth * 0.5f
        //ok, we have some crazy tarot stuff
        val tarot = r.image(Resources.skills_atlas, animation.skin).apply {
            x = sourceUnit.x + (characterWidth * 1.4f - size) / 2f
            y = sourceUnit.y + (characterWidth * 1.4f * 1.66f - size) / 2f
            width = size
            height = size
            setOrigin(Align.center)
            setScale(2f)
            alpha = 0f
        }
        tarotEffectsGroup.addActor(tarot)
        unitsGroup.findActor<UnitActor>(animation.to.toString())?.let { targetActor ->
            val tox = targetActor.x + (characterWidth * 1.4f - size) / 2f
            val toy = targetActor.y + (characterWidth * 1.4f * 1.66f - size) / 2f
            tarot.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.moveTo(tox, toy, 0.2f, SwingIn(1.5f)),
                    Actions.alpha(0.5f, 0.2f),
                    Actions.scaleTo(1f, 1f, 0.2f, SwingIn(1.5f))
                ),
                Actions.run {
                    when (animation.effect) {
                        SkillEffectType.DEFAULT_ATTACK -> {
                            val sparks = AnimationActor(r.atlas(Resources.battle_atlas), "sparks04", 15f, null, targetActor.team == 1).apply {
                                setPosition(targetActor.x + - characterWidth * 0.1f, toy, Align.right)
                                setSize(characterWidth * 1.5f, characterWidth * 1.5f)
                            }
                            tarotEffectsGroup.addActor(sparks)
                        }
                    }
                },
                Actions.parallel(
                    Actions.alpha(0f, 0.2f),
                    Actions.scaleTo(2f, 2f, 0.2f, SwingOut(1.5f))
                ),
                Actions.removeActor()
            ))
        }
    }

    private fun processDestroyTile(event: SbDisplayEvent.SbDestroyTile) {
        r.playSound(SbSoundType.TILE_COMPLETE)
        val actor = tilesGroup[event.z].findActor<TileActor>(event.tileId.toString())
        actor?.addAction(
            Actions.sequence(
                Actions.delay(0.1f),
                Actions.run {
                    val animationActor = AnimationActor(r.atlas(Resources.battle_atlas), "liquid", 60f).apply {
                        val padding = tileSize * 0.1f
                        setPosition(actor.x - padding, actor.y - padding)
                        setSize(tileSize + 2 * padding, tileSize + 2 * padding)
                    }
                    tileFieldEffectsGroup.addActor(animationActor)
                },
                Actions.delay(0.1f),
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
            if (event.targetTile != null) {
                r.playSound(SbSoundType.TILE_JOIN)
                tilesGroup[event.targetTile!!.z].findActor<TileActor>(event.targetTile!!.id.toString())
                    ?.let { targetTile ->
                        targetTile.addAction(
                            Actions.sequence(
                                Actions.delay(0.35f),
                                Actions.run {
                                    targetTile.updateSectors(event.targetTile!!.progress)
                                }
                            )
                        )
                    }
            }
            tileActor.addAction(Actions.moveTo(tx, ty, 0.3f, Interpolation.SwingOut(1.6f)))
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
                    tileActor.addAction(
                        Actions.sequence(
                        Actions.delay(0.3f),
                        Actions.moveTo(btx, bty, 0.05f),
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
            r = r,
            sectors = event.tile.progress,
            maxSectors = event.tile.maxProgress,
            size = tileSize,
            strokeWidth = 9f,
            cardTexture = event.tile.skin,
            polygonBatch = polygonSpriteBatch,
            effects = event.tile.fgEffects,
            type = event.tile.type,
        )
        tile.name = event.tile.id.toString()
        placeTile(tile, event.tile.x, event.tile.y)
        tilesGroup[event.tile.z].addActor(tile)
        tile.animateAppear()

        checkTileTutorial(event)
    }

    private fun createUnit(event: SbDisplayEvent.SbCreateCharacter) {
        val unit = UnitActor(
            r = r,
            id = event.personage.id,
            health = event.personage.health,
            maxHealth = event.personage.maxHealth,
            effects = event.personage.effects,
            texture = event.personage.skin,
            team = event.personage.team,
            w = characterWidth,
            s = event.personage.scale,
            position = if (event.personage.team == 0) leftUnitsCount++ else rightUnitsCount++
        )
        unit.name = event.personage.id.toString()
        placeUnit(unit)
        val healthBar = UnitHealthBarActor(
            r = r,
            w = 94f,
            h = 20f,
            health = event.personage.health,
            maxHealth = event.personage.maxHealth,
            rarity = event.personage.rarity
        ).apply {
            x = unit.x + 3f
            y = unit.y + 3f
            name = unit.name
        }

        unitsGroup.addActor(unit)
        healthbarGroup.addActor(healthBar)
        unit.animateAppear()
    }

    private fun placeTile(tile: Actor, x: Int, y: Int) {
        tile.x = tileSize * x
        tile.y = tileSize * y
    }

    private fun placeUnit(unit: UnitActor) {
        unit.x = if (unit.team == 0) {
            characterWidth * unit.position
        } else {
            r.width - characterWidth * (unit.position + 1)
        }
    }

    private fun addTileBackgrounds() {
        (0 until 25).forEach { index ->
            val x = index % 5
            val y = index / 5

            val bg = r.image(Resources.battle_atlas, "tile_bg").apply {
                this.x = x * tileSize
                this.y = y * tileSize
                this.width = tileSize
                this.height = tileSize
            }
            tileBackgroundsGroup.addActor(bg)
        }
    }

    fun dispose(result: EncounterResultModel.BattleResult) {
        hideBattleScreen(result)
        music.stop()
        r.inputMultiplexer.removeProcessor(gestureDetector)
    }

    private fun processSwipe(dx: Int, dy: Int) {
        if (!ignoreSwipe) KtxAsync.launch { r.battleService.processSwipe(dx, dy) }
    }

    override fun onLeft() {
        processSwipe(-1, 0)
    }

    override fun onRight() {
        processSwipe(1, 0)
    }

    override fun onUp() {
        processSwipe(0, 1)
    }

    override fun onDown() {
        processSwipe(0, -1)
    }

    private suspend fun checkTutorials() {
        if (!r.profileService.getTutorial().c1BattleIntroPassed) {
            ignoreSwipe = true
            addActor(TutorialHover(r, locationGroup.bounds(), UiTexts.Tutorials.A1C1.T1, HoverAction.HoverClick(false) {
                val characterActor = unitsGroup.findActor<UnitActor>("0")
                val healthbar = healthbarGroup.findActor<UnitHealthBarActor>("0")
                addActor(TutorialHover(r, characterActor.bounds(), UiTexts.Tutorials.A1C1.T2, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, healthbar.bounds(), UiTexts.Tutorials.A1C1.T3, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, unitsGroup.findActor<UnitActor>("1").bounds(), UiTexts.Tutorials.A1C1.T4, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, tileBackgroundsGroup.bounds(), UiTexts.Tutorials.A1C1.T5, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, tilesGroup[SbTile.LAYER_TILE].getChild(0).bounds(), UiTexts.Tutorials.A1C1.T6, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, tilesGroup[SbTile.LAYER_TILE].getChild(2).bounds(), UiTexts.Tutorials.A1C1.T7, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, tilesGroup[SbTile.LAYER_TILE].getChild(3).bounds(), UiTexts.Tutorials.A1C1.T8, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, tileBackgroundsGroup.bounds(), UiTexts.Tutorials.A1C1.T9, HoverAction.HoverSwipe(0, 1) {
                ignoreSwipe = false; processSwipe(0, 1); ignoreSwipe = true
                KtxAsync.launch {
                delay(500)
                addActor(TutorialHover(r, tileBackgroundsGroup.bounds(), UiTexts.Tutorials.A1C1.T10, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, tilesGroup[SbTile.LAYER_TILE].getChild(4).bounds(), UiTexts.Tutorials.A1C1.T11, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, tilesGroup[SbTile.LAYER_TILE].getChild(0).bounds().apply { width *= 3 }, UiTexts.Tutorials.A1C1.T12, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, tileBackgroundsGroup.bounds(), UiTexts.Tutorials.A1C1.T13, HoverAction.HoverSwipe(-1, 0) {
                ignoreSwipe = false; processSwipe(-1, 0); ignoreSwipe = true
                KtxAsync.launch {
                delay(500)
                addActor(
                TutorialHover(r, unitsGroup.findActor<UnitActor>("1").bounds().apply { x -= width }, UiTexts.Tutorials.A1C1.T14, HoverAction.HoverClick(false) {
                    ignoreSwipe = false
                    KtxAsync.launch { r.profileService.saveTutorial(r.profileService.getTutorial().copy(c1BattleIntroPassed = true)) }
                }))}}))}))}))}))}}))}))}))}))}))}))}))}))
            }))
        }
    }

    private fun checkTileTutorial(event: SbDisplayEvent.SbCreateTile) {
        KtxAsync.launch {
            if (event.tile.skin == "VALERIAN_SIGIL_OF_RENEWAL_BG" && !r.profileService.getTutorial().battleSigilOfRenewalPassed) {
                r.profileService.saveTutorial(r.profileService.getTutorial().copy(battleSigilOfRenewalPassed = true))
                addActor(TutorialHover(r, tilesGroup[SbTile.LAYER_BACKGROUND].findActor<Actor>(event.tile.id.toString()).bounds(), UiTexts.Tutorials.Battle.SigilOfRenewal, HoverAction.HoverClick(false) {
                }))
            } else if (event.tile.skin == "COMMON_WEAKNESS" && !r.profileService.getTutorial().battleWeaknessPassed) {
                r.profileService.saveTutorial(r.profileService.getTutorial().copy(battleWeaknessPassed = true))
                addActor(TutorialHover(r, tilesGroup[SbTile.LAYER_BACKGROUND].findActor<Actor>(event.tile.id.toString()).bounds(), UiTexts.Tutorials.Battle.Weakness, HoverAction.HoverClick(false) {
                }))
            } else if (event.tile.skin == "COMMON_POISON" && !r.profileService.getTutorial().battlePoisonPassed) {
                r.profileService.saveTutorial(r.profileService.getTutorial().copy(battlePoisonPassed = true))
                addActor(TutorialHover(r, tilesGroup[SbTile.LAYER_TILE].findActor<Actor>(event.tile.id.toString()).bounds(), UiTexts.Tutorials.Battle.Poison, HoverAction.HoverClick(false) {
                }))
            }
        }
    }
}
