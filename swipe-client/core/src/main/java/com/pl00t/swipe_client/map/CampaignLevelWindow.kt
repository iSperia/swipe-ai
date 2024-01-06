package com.pl00t.swipe_client.map

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.analytics.AnalyticEvents
import com.pl00t.swipe_client.monster.MonsterShortDetailsCell
import com.pl00t.swipe_client.services.levels.FrontLevelModel
import com.pl00t.swipe_client.ux.HoverAction
import com.pl00t.swipe_client.ux.TutorialHover
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class CampaignLevelWindow(
    private val r: Resources,
    private val model: FrontLevelModel,
    private val onClose: () -> Unit,
    private val onMonsterClicked: (String, Int, Int) -> Unit,
    private val openBattle: () -> Unit
) : Group() {

    lateinit var title: WindowTitleActor

    private val content: Table = Table().apply {
        width = 480f
    }
    private val scrollPane = ScrollPane(content).apply {
        width = r.width
        height = r.height - 190f
        y = 110f
    }

    init {
        setSize(r.width, r.height)
        val texture = r.image(Resources.ux_atlas, "texture_screen").apply { setSize(r.width, r.height); setScaling(Scaling.fillY) }
        val background = r.image(Resources.ux_atlas, "background_solid").apply { setSize(r.width, r.height); alpha = 0.5f }
        val backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(texture)
        addActor(background)
        addActor(backgroundShadow)
        addActor(scrollPane)

        addTitle()

        addLocationImage()
        addLore()
        addWaves()
        content.row()
        content.add().growY()

        addBottomPanel()
    }

    private fun addBottomPanel() {
        val actions = listOf(
            ActionCompositeButton(r, Action.Attack, Mode.SingleLine(UiTexts.ButtonAttack.value(r.l))).apply {
                onClick {
                    KtxAsync.launch {
                        r.analytics.trackEvent(AnalyticEvents.BattleEvent.EVENT_BATTLE_START, AnalyticEvents.BattleEvent.create(model.act, model.locationId, -1))
                        r.battleService.createBattle(model.act, model.locationId, -1)
                        openBattle()
                    }
                }
            }
        )
        val panel = BottomActionPanel(r, actions, 2).apply { name = "bottom_panel" }
        addActor(panel)

        checkTutorial()
    }

    private fun addTitle() {
        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        title = WindowTitleActor(r, model.locationTitle.value(r.l), closeButton, null, 2).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun addLocationImage() {
        val drawable = r.atlas(Resources.actAtlas(model.act)).findRegion(model.locationBackground).let {
            val x1 = it.u
            val x2 = it.u2
            val y1 = it.v
            val y2 = it.v2
            val d = y2 - y1
            TextureRegionDrawable(TextureRegion(it.texture, x1, y1 + d * 0.25f, x2, y1 + d * 0.75f))
        }
        val image = Image(drawable).apply {
            width = 480f
            height = 240f
            setScaling(Scaling.stretch)
        }
        content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f)}).colspan(3).size(480f, 1f).row()
        content.add(image).size(480f, 240f).colspan(3).row()
        content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f) }).colspan(3).size(480f, 1f).row()
    }

    private fun addLore() {
    }

    private fun addWaves() {
        val needWaves = model.waves.size > 1
        model.waves.forEachIndexed { waveIndex, wave ->
            if (needWaves) {
                content.add(r.labelFocusedCaption(UiTexts.WaveTemplate.value(r.l).replace("$", (1 + waveIndex).toString())).apply {
                    setAlignment(Align.center)
                    width = 480f
                }).size(480f, 30f).colspan(3).row()
            }
            wave.forEach { monster ->
                content.add(MonsterShortDetailsCell(r, monster, true).apply {
                    onClick { onMonsterClicked(monster.skin, monster.level, monster.rarity) }
                }).size(150f, 320f).colspan(1)
            }
            content.row()
        }
    }

    private fun checkTutorial() {
        KtxAsync.launch {
            r.profileService.getTutorial().let { tutorial ->
                if (!tutorial.c1LevelDetailsPassed) {
                    addActor(TutorialHover(r,
                        Rectangle(190f, 10f, 100f, 110f),
                            UiTexts.Tutorials.C1Details, HoverAction.HoverClick(true) {
                                KtxAsync.launch {
                                    r.profileService.saveTutorial(tutorial.copy(c1LevelDetailsPassed = true))
                                    r.analytics.trackEvent(AnalyticEvents.BattleEvent.EVENT_BATTLE_START, AnalyticEvents.BattleEvent.create(model.act, model.locationId, -1))
                                    r.battleService.createBattle(model.act, model.locationId, -1)
                                    openBattle()
                                }
                    }))
                }
            }
        }
    }
}
