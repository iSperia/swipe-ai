package com.pl00t.swipe_client.heroes

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.game7th.items.ItemCategory
import com.game7th.swipe.SbText
import com.game7th.swipe.game.FrontMonsterConfiguration
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.Action
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.action.Mode
import com.pl00t.swipe_client.home.ReloadableScreen
import com.pl00t.swipe_client.monster.AttributesActor
import com.pl00t.swipe_client.monster.MonsterDetailWindow
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.HoverAction
import com.pl00t.swipe_client.ux.TutorialHover
import com.pl00t.swipe_client.ux.bounds
import com.pl00t.swipe_client.ux.path
import kotlinx.coroutines.delay
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

    init {
        KtxAsync.launch {
            delay(200)
//            checkTutorial()
        }
    }

    override suspend fun fillBottomPanelActions(list: MutableList<ActionCompositeButton>) {
        super.fillBottomPanelActions(list)
        list.add(ActionCompositeButton(r, Action.Equipment, Mode.SingleLine(UiTexts.NavItems.value(r.l))).apply {
            onClick {
                showEquipment()
            }
        })
        list.add(ActionCompositeButton(r, Action.Tarot, Mode.SingleLine(UiTexts.Tarot.value(r.l))))
        list.add(ActionCompositeButton(r, Action.Complete, Mode.SingleLine(UiTexts.MakeActive.value(r.l))).apply {
            onClick {
                KtxAsync.launch {
                    r.profileService.setActiveCharacter(model.skin)
                    reload()
                    this@HeroDetailWindow.onClose()
                }
            }
        })

    }

    override suspend fun createAttributesActor(): Actor {
        val character = r.profileService.getCharacters().first { it.skin == model.skin }
        return HeroStatsContainer(r, model).apply {
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

    private suspend fun checkTutorial() {
        if (!r.profileService.getTutorial().a1HeroOpened) {
            addActor(TutorialHover(r, bottomPanel.getChild(3).bounds(), UiTexts.Tutorials.CharacterScreen.S1, HoverAction.HoverClick(true) {
            val attrs = attributesActor!! as HeroStatsContainer
            addActor(TutorialHover(r, attrs.bottomActionPanel.getChild(3).bounds(), UiTexts.Tutorials.CharacterScreen.S2, HoverAction.HoverClick(false) {
            var attrsContent = (attrs.getChild(0) as ScrollPane).getChild(0) as Table
            addActor(TutorialHover(r, attrsContent.getChild(0).bounds(), UiTexts.Tutorials.CharacterScreen.S3, HoverAction.HoverClick(false) {
            val attrsValsTable = attrsContent.getChild(1) as Table
            addActor(TutorialHover(r, attrsValsTable.getChild(0).bounds().apply { width = 480f }, UiTexts.Tutorials.CharacterScreen.S4, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, attrsValsTable.getChild(2).bounds().apply { width = 480f }, UiTexts.Tutorials.CharacterScreen.S5, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, attrsValsTable.getChild(4).bounds().apply { width = 480f }, UiTexts.Tutorials.CharacterScreen.S6, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, attrsContent.getChild(2).bounds(), UiTexts.Tutorials.CharacterScreen.S7, HoverAction.HoverClick(true) {
            attrs.cachedSelectedExpCurrency = 0; attrs.loadData()
            delay(100)
            attrsContent = (attrs.getChild(0) as ScrollPane).getChild(0) as Table
            addActor(TutorialHover(r, attrsContent.path(2, 1, 3).bounds(), UiTexts.Tutorials.CharacterScreen.S8, HoverAction.HoverClick(true) {
                attrs.useExperienceItem(FrontItemEntryModel.CurrencyItemEntryModel(SwipeCurrency.SCROLL_OF_WISDOM.toString(), 2, 0, 0, SbText("", ""), SwipeCurrency.SCROLL_OF_WISDOM))
                addActor(TutorialHover(r, attrsContent.getChild(0).bounds(), UiTexts.Tutorials.CharacterScreen.S9, HoverAction.HoverClick(false) {
                    addActor(TutorialHover(r, attrsContent.path(2, 1, 3).bounds(), UiTexts.Tutorials.CharacterScreen.S10, HoverAction.HoverClick(true) {
                        attrs.useExperienceItem(FrontItemEntryModel.CurrencyItemEntryModel(SwipeCurrency.SCROLL_OF_WISDOM.toString(), 2, 0, 0, SbText("", ""), SwipeCurrency.SCROLL_OF_WISDOM))
                        addActor(TutorialHover(r, attrsContent.getChild(0).bounds(), UiTexts.Tutorials.CharacterScreen.S11, HoverAction.HoverClick(true) {
                            showSkillsTutorial()
                        }))
                    }))
                }))
            }))
            }))
            }))
            }))
            }))
            }))
            }))
            }))
        }
    }

    private suspend fun showSkillsTutorial() {
        addActor(TutorialHover(r, bottomPanel.path(4).bounds(), UiTexts.Tutorials.CharacterScreen.S12, HoverAction.HoverClick(true) {
            showSkillset()
            delay(100)
            addActor(TutorialHover(r, abilitiesActor!!.path(0, 3).bounds(), UiTexts.Tutorials.CharacterScreen.S13, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, abilitiesActor!!.path(0, 4).bounds(), UiTexts.Tutorials.CharacterScreen.S14, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, abilitiesActor!!.path(0, 5).bounds(), UiTexts.Tutorials.CharacterScreen.S15, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, abilitiesActor!!.path(0, 6).bounds(), UiTexts.Tutorials.CharacterScreen.S16, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, abilitiesActor!!.path(1, 0, 1).bounds(), UiTexts.Tutorials.CharacterScreen.S17, HoverAction.HoverClick(false) {
                showEquipmentTutorial()
            }))
            }))
            }))
            }))
            }))
        }))
    }

    private suspend fun showEquipmentTutorial() {
        addActor(TutorialHover(r, bottomPanel.path(6).bounds(), UiTexts.Tutorials.CharacterScreen.S18, HoverAction.HoverClick(true) {
            showEquipment()
            delay(100)
            addActor(TutorialHover(r, equipmentActor!!.path(0, 0, 0).bounds(), UiTexts.Tutorials.CharacterScreen.S19, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, equipmentActor!!.path(0, 0, 0).bounds(), UiTexts.Tutorials.CharacterScreen.S20, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, equipmentActor!!.path(0, 0, 0).bounds(), UiTexts.Tutorials.CharacterScreen.S21, HoverAction.HoverClick(false) {
            addActor(TutorialHover(r, equipmentActor!!.path(0, 0, 0, 1).bounds(), UiTexts.Tutorials.CharacterScreen.S22, HoverAction.HoverClick(true) {
                equipmentActor!!.selectedCategory = ItemCategory.RING
                equipmentActor!!.reload()
                delay(100)
                addActor(TutorialHover(r, equipmentActor!!.path(0, 0, 1).bounds(), UiTexts.Tutorials.CharacterScreen.S23, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, equipmentActor!!.path(0, 0, 1, 7).bounds(), UiTexts.Tutorials.CharacterScreen.S24, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, equipmentActor!!.path(0, 0, 1, 2).bounds(), UiTexts.Tutorials.CharacterScreen.S25, HoverAction.HoverClick(false) {
                addActor(TutorialHover(r, equipmentActor!!.path(0, 0, 1, 3).bounds(), UiTexts.Tutorials.CharacterScreen.S26, HoverAction.HoverClick(true) {
                    r.profileService.equipItem("CHARACTER_VALERIAN", r.profileService.getItems().first().also { equipmentActor!!.selectedItemId = it.id })
                    equipmentActor!!.reload()
                    delay(100)
                    addActor(TutorialHover(r, equipmentActor!!.path(0, 0, 0, 1).bounds(), UiTexts.Tutorials.CharacterScreen.S27, HoverAction.HoverClick(false) {
                    addActor(TutorialHover(r, title.path(4).bounds(), UiTexts.Tutorials.CharacterScreen.S28, HoverAction.HoverClick(true) {
                        onClose()
                        r.profileService.saveTutorial(r.profileService.getTutorial().copy(a1HeroOpened = true))
                    }))
                    }))
                }))
                }))
                }))
                }))
            }))
            }))
            }))
            }))
        }))
    }
}
