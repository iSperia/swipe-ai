package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.actions.ScaleByAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.pl00t.swipe_client.screen.StageScreen
import com.pl00t.swipe_client.services.battle.BattleDecorations
import com.pl00t.swipe_client.services.battle.BattleService
import com.pl00t.swipe_client.services.battle.logic.BattleEvent
import com.pl00t.swipe_client.services.levels.FrontLevelDetails
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ktx.async.KtxAsync
import kotlin.math.max
import kotlin.random.Random

class BattleScreen(
    amCore: AssetManager,
    private val battleService: BattleService
) : StageScreen(amCore) {

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

    private val polygonSpriteBatch = PolygonSpriteBatch()

    private val _panelHei = root.width * 1.1f
    private val _locationHei = root.height - _panelHei * 0.9f
    private val _tileSize = root.width * 0.17f
    private val _tileLeftOffset = (root.width - 5 * _tileSize)/2f
    private val _tileBottomOffset = _tileSize
    private val _characterWidth = root.width / 3f

    private var leftUnitsCount = 0
    private var rightUnitsCount = 0

    override fun show() {
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
        gPanel.addActor(gTileBgs)
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
                else -> Unit
            }
        }
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
        placeUnit(unit)
        gUnits.addActor(unit)
    }

    private fun placeUnit(unit: UnitActor) {
        unit.x = if (unit.team == 0) {
            0.6f * _characterWidth * unit.position
        } else {
            root.width - 0.6f * _characterWidth * unit.position
        }
        println("Placed $unit at [${unit.x}:${unit.y}]")
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

    override fun render(delta: Float) {
        super.render(delta)

        root.act()
    }
}
