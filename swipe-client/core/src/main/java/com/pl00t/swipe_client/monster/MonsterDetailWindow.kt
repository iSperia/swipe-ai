package com.pl00t.swipe_client.monster

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.game7th.swipe.game.SbMonsterConfiguration
import com.game7th.swipe.mapAsMonster
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class MonsterDetailWindow(
    private val r: R,
    private val model: SbMonsterConfiguration,
    private val onClose: () -> Unit
): Group() {

    lateinit var title: WindowTitleActor
    lateinit var bottomPanel: BottomActionPanel

    private var root = Group()
    private var attributesActor: AttributesActor? = null

    init {
        setSize(r.width, r.height)
        val background = r.image(R.ux_atlas, "background_solid").apply { setSize(r.width, r.height) }
        val backgroundShadow = r.image(R.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(background)
        addActor(backgroundShadow)

        addActor(root)

        addTitle()
        addBottomPanel()

        showAttributes()
    }

    private fun addTitle() {
        val closeButton = ActionCompositeButton(r, Action.CLOSE, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        title = WindowTitleActor(r, model.name.value(r.l), closeButton, null, 2).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun addBottomPanel() {
        val actions = listOf(
            ActionCompositeButton(r, Action.STORY, Mode.SingleLine(UiTexts.ButtonStory.value(r.l)))
                .apply { onClick { showStory() } },
            ActionCompositeButton(r, Action.SKILLSET, Mode.SingleLine(UiTexts.ButtonSkillset.value(r.l)))
                .apply { onClick { showSkillset() } },
            ActionCompositeButton(r, Action.STATS, Mode.SingleLine(UiTexts.ButtonStats.value(r.l)))
                .apply { onClick { showAttributes() } },
        )
        bottomPanel = BottomActionPanel(r, actions, 2)
        addActor(bottomPanel)
    }

    private fun showAttributes() {
        KtxAsync.launch {
            if (attributesActor == null) {
                attributesActor = AttributesActor(r, model.mapAsMonster(), model.skin).apply {
                    y = 110f
                }
                root.addActor(attributesActor)
            }
            attributesActor?.apply {
                alpha = 0f
                addAction(Actions.alpha(1f, 0.4f))
            }
        }
    }

    private fun showSkillset() {
        hideAttributes()
    }

    private fun showStory() {
        hideAttributes()
    }

    private fun hideAttributes() {
        attributesActor?.addAction(Actions.alpha(0f, 0.4f))
    }
}
