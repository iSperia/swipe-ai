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
import com.pl00t.swipe_client.services.FrontLevelDetails
import kotlin.math.max
import kotlin.random.Random

class BattleScreen(
    amCore: AssetManager,
    private val levelDetails: FrontLevelDetails,
) : StageScreen(amCore) {

    lateinit var amBattle: AssetManager
    lateinit var taBattle: TextureAtlas
    lateinit var taMap: TextureAtlas
    lateinit var taValerion: TextureAtlas
    lateinit var taThornstalker: TextureAtlas

    lateinit var gPanel: Group
    lateinit var gLocation: Group
    lateinit var gTileBgs: Group

    private val polygonSpriteBatch = PolygonSpriteBatch()

    private val _panelHei = root.width * 1.1f
    private val _locationHei = root.height - _panelHei * 0.9f
    private val _tileSize = root.width * 0.17f
    private val _tileLeftOffset = (root.width - 5 * _tileSize)/2f
    private val _tileBottomOffset = _tileSize
    private val _characterWidth = root.width / 3f

    override fun show() {
        amBattle = AssetManager().apply {
            load("atlases/battle.atlas", TextureAtlas::class.java)
            load("atlases/map.atlas", TextureAtlas::class.java)
            load("atlases/charValerion.atlas", TextureAtlas::class.java)
            load("atlases/char_thronstalker.atlas", TextureAtlas::class.java)
        }
        loadAm(amBattle, this::amLoaded)
    }

    private fun amLoaded() {
        taBattle = amBattle.get("atlases/battle.atlas", TextureAtlas::class.java)
        taMap = amBattle.get("atlases/map.atlas", TextureAtlas::class.java)
        taValerion = amBattle.get("atlases/charValerion.atlas", TextureAtlas::class.java)
        taThornstalker = amBattle.get("atlases/char_thronstalker.atlas", TextureAtlas::class.java)

        gPanel = Group()
        gLocation = Group()
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
        val locationImage = Image(taMap.findRegion(levelDetails.locationId)).apply {
            width = locationSize
            height = locationSize
            x = - (locationSize - root.width) / 2f
        }
        gLocation.addActor(locationImage)

        addTileBackgrounds()

        val ultimateProgress = UltimateProgressActor(taBattle, _tileSize * 5f, _tileSize * 0.7f).apply {
            x = (root.width - _tileSize * 5f) / 2f
            y = _tileSize * 0.2f
        }
        gPanel.addActor(ultimateProgress)

        val texture = taValerion.findRegion("character_valerian")
        val texture2 = taThornstalker.findRegion("character_thalendros")
        val texture3 = taThornstalker.findRegion("character_corrupted_dryad")
        val character = Image(texture).apply {
            width = _characterWidth
            height = _characterWidth * texture.originalHeight / texture.originalWidth
            x = 0f
            y = _panelHei * 0.1f
        }
        val character2 = Image(texture2).apply {
            width = _characterWidth * 1.7f
            height = _characterWidth * 1.7f * texture2.originalHeight / texture.originalWidth
            scaleX = -1f
            x = root.width
            y = _panelHei * 0.1f
        }
        val character3 = Image(texture3).apply {
            width = _characterWidth * 0.9f
            height = _characterWidth * 0.9f * texture2.originalHeight / texture.originalWidth
            scaleX = -1f
            x = root.width - _characterWidth * 0.75f
            y = _panelHei * 0.1f
        }
        character.addAction(RepeatAction().apply {
            this.count = Int.MAX_VALUE
            this.action = SequenceAction(
                ScaleByAction().apply {
                    this.amountX = 0.03f
                    this.amountY = 0.03f
                    this.duration = 2.4f
                },
                ScaleByAction().apply {
                    this.amountX = -0.03f
                    this.amountY = -0.03f
                    this.duration = 2.4f
                }
            )
        })
        character2.addAction(RepeatAction().apply {
            this.count = Int.MAX_VALUE
            this.action = SequenceAction(
                ScaleByAction().apply {
                    this.amountX = 0.03f
                    this.amountY = 0.03f
                    this.duration = 2.4f
                },
                ScaleByAction().apply {
                    this.amountX = -0.03f
                    this.amountY = -0.03f
                    this.duration = 2.4f
                }
            )
        })
        character3.addAction(RepeatAction().apply {
            this.count = Int.MAX_VALUE
            this.action = SequenceAction(
                ScaleByAction().apply {
                    this.amountX = 0.03f
                    this.amountY = 0.03f
                    this.duration = 2.4f
                },
                ScaleByAction().apply {
                    this.amountX = -0.03f
                    this.amountY = -0.03f
                    this.duration = 2.4f
                }
            )
        })
        gLocation.addActor(character)
        gLocation.addActor(character2)
//        gLocation.addActor(character3)

        val cards = listOf("tarot_valerion_ability_sword", "tarot_valerion_ability_beam", "tarot_valerion_ability_sigil")
        (0 until 25).shuffled().take(8).forEach { index ->
            val x = (index % 5) * _tileSize + gTileBgs.x
            val y = (index / 5) * _tileSize + gTileBgs.y
            val tileActor = TileActor(
                sectors = Random.nextInt(0, 5),
                maxSectors = Random.nextInt(2, 6),
                size = _tileSize,
                strokeWidth = _tileSize / 8,
                taBattle = taBattle,
                polygonBatch = polygonSpriteBatch,
                cardTexture = cards.random(),
                taPersonage = taValerion
            ).apply {
                this.x = x
                this.y = y
            }
            gPanel.addActor(tileActor)
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

    override fun render(delta: Float) {
        super.render(delta)

        root.act()
    }
}
