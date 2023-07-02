package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.battle.UnitSkin
import com.pl00t.swipe_client.services.levels.FrontLevelDetails
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.ScreenTitle
import ktx.actors.onClick

interface LevelDetailsCallback {
    fun processMonsterClicked(skin: UnitSkin)
}

class LevelDetailsActor(
    private val levelDetails: FrontLevelDetails,
    private val context: SwipeContext,
    private val skin: Skin,
    private val attackAction: (String) -> Unit
): Group(), LevelWaveCallback {

    val backgroundImage: Image
    val locationForeground: Image
    val startButton: TextButton
    val locationTitle: Group
    val scroll: ScrollPane

    var callback: LevelDetailsCallback? = null

    init {
        val line = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_line")).apply {
            y = 598f
            width = 480f
            height = 4f
        }
        backgroundImage = Image(context.commonAtlas(Atlases.ACT(levelDetails.act)).findRegion(levelDetails.locationBackground)).apply {
            setScaling(Scaling.fill)
            width = 480f
            height = 600f
        }
        locationForeground = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("black_pixel")).apply {
            width = 480f
            height = 600f
            setScaling(Scaling.stretch)
        }
        locationTitle = ScreenTitle.createScreenTitle(context, skin, levelDetails.locationTitle).apply {
            x = 60f
            y = 570f
        }

        val panel = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_bg")).apply {
            width = 480f
            height = 60f
        }
        startButton = Buttons.createActionButton("To Battle!", skin).apply {
            x = 305f
            y = 14f
        }
        startButton.onClick { attackAction(levelDetails.locationId) }

        val table = Table()
        table.width = 360f

        val needWaveLabel = levelDetails.waves.size > 1

        val loreLabel = Label(levelDetails.locationDescription, skin, "lore_small").apply {
            wrap = true
            width = 360f
            setAlignment(Align.topLeft)
        }
        table.add(loreLabel).colspan(3).width(360f).padBottom(30f).padTop(30f)
        table.row()

        levelDetails.waves.forEachIndexed { index, wave ->
            if (needWaveLabel) {
                table.add(Label("Wave ${index + 1}", skin, "wave_caption").apply {
                    width = 360f
                    setAlignment(Align.center)
                }).colspan(3)
                table.row()
            }
            wave.forEach { monster ->
                val group = Group().apply {
                    width = 120f
                    height = 180f
                }
                val label = Label("${monster.name}\nlvl. ${monster.level}", skin, "text_small").apply {
                    wrap = true
                    width = 110f
                    height = 40f
                    x = 5f
                    setAlignment(Align.center)
                }
                val monsterImage = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion(monster.skin.toString())).apply {
                    width = 120f
                    height = 180f
                }
                monsterImage.onClick {
                    processMonsterClicked(monster.skin)
                }
                group.addActor(monsterImage)
                group.addActor(label)
                table.add(group)
            }
            table.row()
        }



        scroll = ScrollPane(table).apply {
            width = 360f
            height = 510f
            x = 60f
            y = 60f
        }

        addActor(backgroundImage)
        addActor(locationForeground)
        addActor(line)
        addActor(panel)
        addActor(startButton)
        addActor(locationTitle)
        addActor(scroll)
    }

    override fun processMonsterClicked(skin: UnitSkin) {
        callback?.processMonsterClicked(skin)
    }
}
