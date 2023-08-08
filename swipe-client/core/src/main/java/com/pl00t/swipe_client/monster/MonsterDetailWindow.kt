package com.pl00t.swipe_client.monster

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.game7th.swipe.game.FrontMonsterConfiguration
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

open class MonsterDetailWindow(
    protected val r: Resources,
    protected var model: FrontMonsterConfiguration,
    private val onClose: () -> Unit
): Group() {

    lateinit var title: WindowTitleActor
    lateinit var bottomPanel: BottomActionPanel
    lateinit protected var background: Image
    lateinit protected var backgroundShadow: Image

    protected var root = Group()
    protected var attributesActor: AttributesActor? = null
    private var loreActor: LoreActor? = null
    private var abilitiesActor: MonsterAbiltiesActor? = null

    init {
        setSize(r.width, r.height)

        val texture = r.image(Resources.ux_atlas, "texture_screen").apply { setSize(r.width, r.height); setScaling(Scaling.fillY) }
        background = r.image(Resources.ux_atlas, "background_solid").apply { setSize(r.width, r.height); alpha = 0.5f }
        backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(texture)
        addActor(background)
        addActor(backgroundShadow)

        addActor(root)

        addTitle()
        addBottomPanel()

        showAttributes()
    }

    protected open suspend fun createAttributesActor(): AttributesActor {
        val attributesActor = MonsterAttributesActor(r, model).apply {
            y = 110f
        }
        return attributesActor
    }

    private fun addTitle() {
        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        val titleText = model.name.value(r.l)
        title = WindowTitleActor(r, titleText, closeButton, null, 2).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    protected suspend open fun fillBottomPanelActions(list: MutableList<ActionCompositeButton>) {
        list.add(ActionCompositeButton(r, Action.Stats, Mode.SingleLine(UiTexts.ButtonStats.value(r.l)))
            .apply { onClick { showAttributes() } })
        list.add(ActionCompositeButton(r, Action.Skillset, Mode.SingleLine(UiTexts.ButtonSkillset.value(r.l)))
            .apply { onClick { showSkillset() } })
        list.add(ActionCompositeButton(r, Action.Story, Mode.SingleLine(UiTexts.ButtonStory.value(r.l)))
            .apply { onClick { showStory() } })
    }

    private fun addBottomPanel() {
        KtxAsync.launch {
            val actions = mutableListOf<ActionCompositeButton>()
            fillBottomPanelActions(actions)
            bottomPanel = BottomActionPanel(r, actions, 2)
            addActor(bottomPanel)
        }
    }

    protected open fun showAttributes() {
        KtxAsync.launch {
            if (attributesActor == null) {
                attributesActor = createAttributesActor()
                root.addActor(attributesActor)
            }
            attributesActor?.apply {
                touchable = Touchable.enabled
                alpha = 0f
                addAction(Actions.alpha(1f, 0.4f))
            }
        }

        hideAbilities()
        hideStory()
    }

    protected open fun showSkillset() {
        KtxAsync.launch {
            if (abilitiesActor == null) {
                abilitiesActor = MonsterAbiltiesActor(r, model).apply {
                    y = 110f
                }
                root.addActor(abilitiesActor)
            }
            abilitiesActor?.apply {
                touchable = Touchable.enabled
                alpha = 0f
                addAction(Actions.alpha(1f, 0.4f))
            }
        }

        hideAttributes()
        hideStory()
    }

    protected open fun showStory() {
        KtxAsync.launch {
            if (loreActor == null) {
                loreActor = LoreActor(r, model.lore).apply {
                    y = 110f
                }
                root.addActor(loreActor)
            }
            loreActor?.apply {
                touchable = Touchable.enabled
                alpha = 0f
                addAction(Actions.alpha(1f, 0.4f))
            }
        }

        hideAttributes()
        hideAbilities()
    }

    protected open fun hideAttributes() {
        attributesActor?.addAction(Actions.alpha(0f, 0.4f))
        attributesActor?.touchable = Touchable.disabled
    }

    protected open fun hideStory() {
        loreActor?.addAction(Actions.alpha(0f, 0.4f))
        loreActor?.touchable = Touchable.disabled
    }

    protected open fun hideAbilities() {
        abilitiesActor?.addAction(Actions.alpha(0f, 0.4f))
        abilitiesActor?.touchable = Touchable.disabled
    }
}
