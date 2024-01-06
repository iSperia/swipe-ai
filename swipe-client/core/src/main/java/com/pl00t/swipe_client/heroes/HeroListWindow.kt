package com.pl00t.swipe_client.heroes

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.Action
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.action.Mode
import com.pl00t.swipe_client.action.WindowTitleActor
import com.pl00t.swipe_client.home.ReloadableScreen
import com.pl00t.swipe_client.monster.MonsterShortDetailsCell
import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.ux.HoverAction
import com.pl00t.swipe_client.ux.TutorialHover
import com.pl00t.swipe_client.ux.bounds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class HeroListWindow(
    private val r: Resources,
    private val onClose: () -> Unit,
    private val onHeroSelected: (String) -> Unit
) : Group(), ReloadableScreen {

    lateinit var title: WindowTitleActor

    private val content = Table().apply {
        width = 480f
    }
    private val scroll = ScrollPane(content).apply {
        setSize(480f, r.height - 80f)
    }

    override fun reload() {
        loadHeroes()
    }

    init {
        setSize(r.width, r.height)

        val texture = r.image(Resources.ux_atlas, "texture_screen").apply { setSize(r.width, r.height); setScaling(Scaling.fillY); setColor(r.skin().getColor("rarity_3")) }
        val backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }

        addActor(texture)
        addActor(backgroundShadow)

        addActor(scroll)
        loadHeroes()

        addTitle()
    }

    private fun addTitle() {
        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        title = WindowTitleActor(r, UiTexts.NavParty.value(r.l), closeButton, null, 3).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun loadHeroes() {
        KtxAsync.launch {
            content.clearChildren()
            val activeSkin = r.profileService.getActiveCharacter()
            r.profileService.getCharacters().forEachIndexed { index, character ->
                val monster = r.monsterService.getMonster(character.skin)!!
                val actor = MonsterShortDetailsCell(
                    r = r,
                    model = FrontMonsterEntryModel(monster.skin, monster.name, SwipeCharacter.getLevel(character.experience), 4),
                    unlocked = character.unlocked,
                )
                if (character.unlocked) {
                    actor.onClick { onHeroSelected(character.skin) }
                }
                val cell = Group().apply {
                    setSize(actor.width, actor.height)
                }
                val bg = r.image(Resources.ux_atlas, "harmony_portal").apply {
                    align = Align.top
                    setScaling(Scaling.fit)
                    setSize(actor.width, actor.height)
                    alpha = 0.12f
                }
                cell.addActor(bg)
                val shadow = r.image(Resources.units_atlas, character.skin).apply {
                    setSize(150f, 250f)
                    color = Color.BLACK
                    x = -4f
                    y = 62f
                    alpha = 0.6f
                }
                cell.addActor(shadow)
                if (character.skin == activeSkin) {
                    bg.alpha = 0.36f
                }
                cell.addActor(actor)

                content.add(cell)
                if (index % 3 == 2) content.row()
            }
            content.row()
            content.add().growY()

            checkTutorials()
        }
    }

    private suspend fun checkTutorials() {
        delay(200)
        if (!r.profileService.getTutorial().a1HeroOpened) {
            addActor(TutorialHover(r, content.getChild(0).bounds(), UiTexts.Tutorials.Act1Hero2, HoverAction.HoverClick(true) {
                r.profileService.saveTutorial(r.profileService.getTutorial().copy(a1HeroOpened = true))
                onHeroSelected("CHARACTER_VALERIAN")
            }))
        }
    }
}
