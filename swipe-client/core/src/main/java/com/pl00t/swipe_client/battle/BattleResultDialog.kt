package com.pl00t.swipe_client.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.services.battle.BattleResult
import com.pl00t.swipe_client.ux.ItemCellActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class BattleResultDialog(
    private val r: R,
    private val result: BattleResult,
    private val onClose: () -> Unit,
    private val onItemClick: (String) -> Unit,
) : Group() {

    private val content = Table().apply {
        width = r.width
    }
    private val scrollPane = ScrollPane(content).apply {
        setSize(r.width, r.height - 190f)
        y = 110f
    }
    lateinit var title: WindowTitleActor
    lateinit var bottomPanel: BottomActionPanel
    lateinit protected var background: Image
    lateinit protected var backgroundShadow: Image

    init {
        background = r.image(R.ux_atlas, "texture_screen").apply { setSize(r.width, r.height); alpha = 0.5f; color = r.skin().getColor("rarity_${if (result.victory) 4 else 0}") }
        backgroundShadow = r.image(R.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(background)
        addActor(backgroundShadow)

        addActor(scrollPane)
        addTitle()
        addBottomPanel()

        loadData()

    }

    private fun addTitle() {
        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        title = WindowTitleActor(r, if (result.victory) UiTexts.BattleVictory.value(r.l) else UiTexts.BattleDefeat.value(r.l),
            closeButton, null, if (result.victory) 4 else 0).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun addBottomPanel() {
        KtxAsync.launch {
            val actions = listOf<ActionCompositeButton>()
            bottomPanel = BottomActionPanel(r, actions, if (result.victory) 4 else 0)
            addActor(bottomPanel)
        }
    }

    private fun loadData() {
        content.clearChildren()

        content.add(r.image(R.ux_atlas, "background_black").apply { alpha = 0.5f; setSize(480f, 1f) }).size(480f, 1f).colspan(4).row()
        val imageBackground = if (result.victory) "background_victory" else "background_defeat"
        content.add(r.image(R.ux_atlas, imageBackground).apply { setSize(480f, 240f) }).size(480f, 240f).colspan(4).row()
        content.add(r.image(R.ux_atlas, "background_black").apply { alpha = 0.5f; setSize(480f, 1f) }).size(480f, 1f).colspan(4).row()

        if (result.exp != null) {
            val group = Group().apply {
                setSize(480f, 100f)
            }
            val skin = r.image(R.units_atlas, result.exp.skin).apply {
                setSize(60f, 100f)
            }
            val name = r.regular24Focus(result.exp.name.value(r.l)).apply {
                setSize(410f, 30f)
                setPosition(70f, 70f)
                setAlignment(Align.left)
            }
            val expCount = r.regular24White(UiTexts.ExpBoost.value(r.l).replace("$", result.exp.expBoost.toString())).apply {
                setSize(410f, 30f)
                setPosition(70f, 40f)
                setAlignment(Align.left)
            }
            group.addActor(name)
            group.addActor(expCount)
            group.addActor(skin)

            content.add(group).size(480f, 100f).padTop(10f).padBottom(10f).colspan(4).row()
        }

        result.freeRewards.forEachIndexed { i, reward ->
            val actor = ItemCellActor(r, reward).apply {
                if (reward.currency != null) {
                    touchable = Touchable.disabled
                } else {
                    onClick { onItemClick(reward.item!!.id) }
                }
            }
            content.add(actor).size(120f, 160f)
            if (i % 4 == 3) content.row()
        }
        content.add().growX()
        content.row()

        content.row()
        content.add().growY()
    }

}
