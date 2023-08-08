package com.pl00t.swipe_client.heroes

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.game7th.swipe.game.FrontMonsterConfiguration
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.Action
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.action.Mode
import com.pl00t.swipe_client.home.ReloadableScreen
import com.pl00t.swipe_client.monster.AttributesActor
import com.pl00t.swipe_client.monster.MonsterDetailWindow
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class HeroDetailWindow(
    r: Resources,
    model: FrontMonsterConfiguration,
    onClose: () -> Unit,
    private val onItemClicked: (String) -> Unit
): MonsterDetailWindow(r, model, onClose), ReloadableScreen {

    private var equipmentActor: HeroEquipmentActor? = null

    override suspend fun fillBottomPanelActions(list: MutableList<ActionCompositeButton>) {
        super.fillBottomPanelActions(list)
        list.add(ActionCompositeButton(r, Action.Equipment, Mode.SingleLine(UiTexts.NavItems.value(r.l))).apply {
            onClick {
                showEquipment()
            }
        })
        list.add(ActionCompositeButton(r, Action.Tarot, Mode.SingleLine(UiTexts.Tarot.value(r.l))))

    }

    override suspend fun createAttributesActor(): AttributesActor {
        val character = r.profileService.getCharacters().first { it.skin == model.skin }
        return HeroAttributesActor(r, character, model).apply {
            y = 110f
        }
    }

    override fun reload() {
        equipmentActor?.reload()
    }

    private fun showEquipment() {

        KtxAsync.launch {
            if (equipmentActor == null) {
                equipmentActor = HeroEquipmentActor(r, model.skin, onChanged = {
                    KtxAsync.launch {
                        model = r.profileService.createCharacter(model.skin)
                    }
                    attributesActor?.remove()
                    attributesActor = null
                }, onItemClicked = { id ->
                    this@HeroDetailWindow.onItemClicked(id)
                })
                root.addActor(equipmentActor)
            }
            equipmentActor?.apply {
                touchable = Touchable.enabled
                alpha = 0f
                addAction(Actions.alpha(1f, 0.4f))
            }

            hideAbilities()
            hideStory()
            hideAttributes()
        }
    }

    private fun hideEquipment() {
        equipmentActor?.addAction(Actions.alpha(0f, 0.4f))
        equipmentActor?.touchable = Touchable.disabled
    }

    override fun showAttributes() {
        super.showAttributes()
        hideEquipment()
    }

    override fun showSkillset() {
        super.showSkillset()
        hideEquipment()
    }

    override fun showStory() {
        super.showStory()
        hideEquipment()
    }
}
