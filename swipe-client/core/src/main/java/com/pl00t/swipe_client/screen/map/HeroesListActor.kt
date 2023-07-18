package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.game7th.swipe.monsters.MonsterService
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.hideToBehindAndRemove
import com.pl00t.swipe_client.ux.raiseFromBehind
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync

class HeroesListActor(
    private val profileService: ProfileService,
    private val monsterService: MonsterService,
    private val context: SwipeContext,
    private val skin: Skin
) : Group() {

    private val heroContainer = Group().apply {
        y = 180f
    }
    lateinit var heroes: List<SwipeCharacter>
    private val heroBackgrounds = mutableListOf<Image>()

    init {
        val background = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("opaque_black")).apply {
            width = context.height()
            height = context.height()
        }
        val panelBg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_bg")).apply {
            height = 180f
            width = 480f
        }

        val closeButton = Buttons.createActionButton("Close", skin).apply {
            x = 300f
            y = 12f
        }
        closeButton.onClick {
            this@HeroesListActor.hideToBehindAndRemove(context.height())
        }

        addActor(background)
        addActor(heroContainer)
        addActor(panelBg)
        addActor(closeButton)

        loadHeroes()
    }

    private fun loadHeroes() {
        KtxAsync.launch {
            val table = Table()

            heroes = profileService.getCharacters()
            heroes.forEachIndexed { index, hero ->
                val heroGroup = Group().apply {
                    width = 80f
                    height = 120f
                }
                val background = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("black_pixel")).apply {
                    width = 80f
                    height = 120f
                    isVisible = false
                }
                val heroImage = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion(hero.skin.toString())).apply {
                    width = 80f
                    height = 120f
                    touchable = Touchable.disabled
                }
                val heroName = Label("${hero.name}\nLvl. ${hero.level.level}", skin, "text_small").apply {
                    width = 80f
                    wrap = true
                    height = 40f
                    setAlignment(Align.bottomLeft)
                    touchable = Touchable.disabled
                }
                heroGroup.addActor(background)
                heroGroup.addActor(heroImage)
                heroGroup.addActor(heroName)

                heroBackgrounds.add(background)
                background.onClick { KtxAsync.launch { selectCharacter(index) } }

                table.add(heroGroup).align(Align.left).width(80f).maxWidth(80f)
            }

            val scroll = ScrollPane(table).apply {
                width = 460f
                height = 120f
                x = 10f
                y = 60f

            }

            addActor(scroll)
            selectCharacter(0)
        }
    }

    private suspend fun selectCharacter(index: Int) {
        heroBackgrounds.forEachIndexed { i, bg ->
            bg.isVisible = i == index

            val hero = heroes[index]
            val monster = monsterService.getMonster(hero.skin.toString()) ?: return
            val actor = MonsterDetailActor(context.height() - 180f, monster, hero, context, skin)
            if (heroContainer.hasChildren()) heroContainer.getChild(0)?.hideToBehindAndRemove(context.height() - 180f)
            heroContainer.addActor(actor)
            actor.raiseFromBehind(context.height() - 180f)
        }
    }
}
