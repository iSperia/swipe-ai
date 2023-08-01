package com.pl00t.swipe_client.map

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.monster.MonsterShortDetailsCell
import com.pl00t.swipe_client.services.levels.FrontLevelModel
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync

class CampaignLevelWindow(
    private val r: R,
    private val model: FrontLevelModel,
    private val onClose: () -> Unit,
    private val onMonsterClicked: (String) -> Unit,
) : Group() {

    lateinit var title: WindowTitleActor

    private val content: Table = Table()
    private val scrollPane = ScrollPane(content).apply {
        width = r.width
        height = r.height - 190f
        y = 110f
    }

    init {
        setSize(r.width, r.height)
        val background = r.image(R.ux_atlas, "background_solid").apply { setSize(r.width, r.height) }
        val backgroundShadow = r.image(R.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
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
            ActionCompositeButton(r, Action.ATTACK, Mode.SingleLine(UiTexts.ButtonAttack.value(r.l))).apply {
                onClick {
                    KtxAsync.launch {
                        r.battleService.createBattle(model.act, model.locationId, -1)
                        r.router.navigateBattle()
                    }
                }
            }
        )
        val panel = BottomActionPanel(r, actions, 2)
        addActor(panel)
    }

    private fun addTitle() {
        val closeButton = ActionCompositeButton(r, Action.CLOSE, Mode.NoText).apply {
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
        val drawable = r.atlas(R.actAtlas(model.act)).findRegion(model.locationBackground).let {
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
        }
        content.add(r.image(R.ux_atlas, "background_white").apply { setSize(480f, 1f)}).colspan(3).size(480f, 1f).row()
        content.add(image).size(480f, 240f).colspan(3).row()
        content.add(r.image(R.ux_atlas, "background_white").apply { setSize(480f, 1f) }).colspan(3).size(480f, 1f).row()
    }

    private fun addLore() {
        val loreLabel = r.labelLore(model.locationDescription.value(r.l))
        content.add(loreLabel).width(460f).pad(10f).colspan(3).row()
        content.add(r.image(R.ux_atlas, "background_white").apply { setSize(480f, 1f) }).colspan(3).size(480f, 1f).row()
    }

    private fun addWaves() {
        val needWaves = model.waves.size > 1
        model.waves.forEachIndexed { waveIndex, wave ->
            if (needWaves) {
                content.add(r.labelFocusedCaption(UiTexts.WaveTemplate.value(r.l).replace("$", (1 + waveIndex).toString()))).colspan(3).row()
            }
            wave.forEach { monster ->
                content.add(MonsterShortDetailsCell(r, monster).apply {
                    onClick { onMonsterClicked(monster.skin) }
                }).size(150f, 310f).pad(4f)
            }
            content.row()
        }
    }

}
