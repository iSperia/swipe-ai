package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
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
import com.pl00t.swipe_client.services.battle.MonsterConfiguration
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.ScreenTitle
import com.pl00t.swipe_client.ux.hideToBehindAndRemove
import ktx.actors.alpha
import ktx.actors.onClick

private enum class DisplayMode {
    DESCRIPTION,
    ABILITIES,
}

class MonsterDetailActor(
    private val monsterInfo: MonsterConfiguration,
    private val context: SwipeContext,
    private val skin: Skin
): Group() {

    private var displayMode = DisplayMode.DESCRIPTION

    private val buttonSwitch: TextButton
    private val scroll: ScrollPane

    init {
        val blackBackground = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("opaque_black")).apply {
            width = context.width()
            height = context.height()
        }
        val monsterImage = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion(monsterInfo.skin.toString())).apply {
            name = "monster_image"
            width = context.width()
            height = context.height()
            setScaling(Scaling.fit)
        }
        val panel = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_bg")).apply {
            width = 480f
            height = 60f
            setScaling(Scaling.stretch)
        }
        val buttonClose = Buttons.createActionButton("Close", skin).apply {
            x = 305f
            y = 14f
        }
        buttonClose.onClick {
            this@MonsterDetailActor.hideToBehindAndRemove(context.height())
        }
        buttonSwitch = Buttons.createActionButton("Show abilities", skin).apply {
            x = 5f
            y = 14f
        }
        buttonSwitch.onClick {
            updateDisplayMode(if (displayMode == DisplayMode.DESCRIPTION) DisplayMode.ABILITIES else DisplayMode.DESCRIPTION)
        }
        val title = ScreenTitle.createScreenTitle(context, skin, monsterInfo.name).apply {
            y = context.height() - 60f
            x = 60f
        }
        val loreBackground = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("opaque_black")).apply {
            name = "lore_background"
            y = 60f
            width = 480f
            height = 80f
        }
        val loreLine = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_line")).apply {
            name = "lore_line"
            y = 138f
            width = 480f
            height = 4f
        }
        val monsterLore = Label(monsterInfo.lore, skin, "lore_small").apply {
            name = "monster_lore"
            height = 75f
            width = 470f
            wrap = true
            y = 60f
            x = 5f
            setAlignment(Align.topLeft)
        }

        val skillsTable = Table()
        monsterInfo.abilities?.forEach { ability ->
            val tarot = Image(context.commonAtlas(Atlases.COMMON_TAROT).findRegion(ability.skin.toString())).apply {
                width = 120f
                height = 200f
                setScaling(Scaling.stretch)
            }
            skillsTable.add(tarot).pad(5f).width(130f).height(210f)

            val sideTable = Table()
            sideTable.add(Label(ability.title, skin, "wave_caption").apply {
                setAlignment(Align.left)
            }).width(330f).pad(5f).colspan(2)
            sideTable.row()
            sideTable.add(Label(ability.description, skin, "text_regular").apply {
                setAlignment(Align.left)
                wrap = true
            }).width(330f).pad(5f).colspan(2)
            sideTable.row()

            ability.descriptionTable.forEach { row ->
                val titleLabel = Label(row.title, skin, "text_small").apply {
                    wrap = true
                    width = 250f
                    setAlignment(Align.right)
                }
                sideTable.add(titleLabel).width(260f).pad(5f)
                val valueLabel = Label(row.formatDescription(ability.attributes), skin, "text_small_accent").apply {
                    wrap = true
                    width = 50f
                    setAlignment(Align.left)
                }
                sideTable.add(valueLabel).width(60f).pad(5f)
                sideTable.row()
            }

            sideTable.add(Label(ability.lore, skin, "lore_small").apply {
                wrap = true
                setAlignment(Align.left)
            }).width(330f).pad(5f).colspan(2)

            skillsTable.add(sideTable).width(340f)
            skillsTable.row()
        }

        scroll = ScrollPane(skillsTable).apply {
            y = 60f
            x = 0f
            width = 480f
            height = context.height() - 120f
            isVisible = false
            alpha = 0f
        }

        addActor(blackBackground)
        addActor(monsterImage)
        addActor(panel)
        addActor(buttonClose)
        addActor(buttonSwitch)
        addActor(title)
        addActor(loreBackground)
        addActor(monsterLore)
        addActor(loreLine)
        addActor(scroll)
    }

    init {
//        backgroundImage = Image(coreAtlas.findRegion("semi_black_pixel")).apply {
//            width = w
//            height = h
//        }
//        monsterImage = Image(monsterAtlas.findRegion(monsterInfo.skin.toString())).apply {
//            width = w * 0.9f
//            height = h * 0.9f
//            x = w * 0.05f
//            y = h * 0.05f
//            setScaling(Scaling.fit)
//        }
////        monsterName = Fonts.createWhiteTitle(monsterInfo.name, _nameHeight).apply {
////            y = h - _nameHeight - _buttonHeight
////            x = w * 0.05f
////            setAlignment(Align.center)
////            width = w * 0.9f
////            height = _nameHeight
////        }
//        buttonClose = IconedButton(_buttonWidth, _buttonHeight, "Close", "icon_close", coreAtlas, coreAtlas).apply {
//            x = this@MonsterDetailActor.w * 0.55f
////            y = monsterName.y + _nameHeight - this@MonsterDetailActor.w * 0.025f
//        }
//        buttonAbilities = IconedButton(_buttonWidth, _buttonHeight, "Abilities", "icon_question", coreAtlas, coreAtlas, Align.left).apply {
//            x = this@MonsterDetailActor.w * 0.05f
//            y = buttonClose.y
//        }
//        buttonClose.onClick { this@MonsterDetailActor.hideToBehindAndRemove(this@MonsterDetailActor.h) }
//        buttonAbilities.onClick {
//            this@MonsterDetailActor.updateDisplayMode(when (displayMode) {
//                DisplayMode.ABILITIES -> DisplayMode.DESCRIPTION
//                DisplayMode.DESCRIPTION -> DisplayMode.ABILITIES
//            })
//        }
//        monsterNameBackground = Image(coreAtlas.findRegion("top_gradient")).apply {
//            y = h - _nameBgHeight
//            width = w
//            height = _nameBgHeight
//        }
////        monsterLore = Fonts.createCaptionAccent(monsterInfo.lore, _loreHeight * 0.3f).apply {
////            width = w * 0.9f
////            x = w * 0.05f
////            height = _loreHeight
////            setAlignment(Align.topLeft)
////            wrap = true
////        }
//        monsterLoreBackground = Image(coreAtlas.findRegion("top_gradient")).apply {
//            scaleY = -1f
//            y = _loreBgHeight
//            width = w
//            height = _loreBgHeight
//        }
////        monsterProperties = Fonts.createWhiteCaption("", _loreHeight / 2f).apply {
////            width = w * 0.9f
////            x = w * 0.05f
////            setAlignment(Align.left)
////            height = _loreHeight
////            y = _loreHeight
////            wrap = true
////        }
//
//        abilitiesGroup = Group()
//        fillAbilities()
//        abilitiesScroll = ScrollPane(abilitiesGroup).apply {
//            x = 0f
//            y = 0f
//            width = w
////            height = monsterName.y
//        }
//        abilitiesScroll.isVisible = false
//
//        addActor(backgroundImage)
//        addActor(monsterImage)
//        addActor(abilitiesScroll)
//        addActor(monsterNameBackground)
////        addActor(monsterName)
//        addActor(buttonClose)
//        addActor(buttonAbilities)
//        addActor(monsterLoreBackground)
////        addActor(monsterLore)
////        addActor(monsterProperties)
    }

    private fun fillAbilities() {
//        val abilityActors = monsterInfo.abilities?.map { config ->
//            MonsterAbilityDetailActor(config, w, coreAtlas, tarotAtlas)
//        } ?: emptyList()
//        var cursor = max(monsterName.y.toInt(), abilityActors.sumOf { it.height.toInt() }) + monsterName.y - monsterNameBackground.y
//        abilitiesGroup.height = cursor
//        cursor -= (monsterName.y - monsterNameBackground.y).toInt()
//        abilitiesGroup.width = w
//        abilityActors.forEach { actor ->
//            cursor -= actor.height.toInt()
//            actor.y = cursor.toFloat()
//            abilitiesGroup.addActor(actor)
//        }
    }

    private fun updateDisplayMode(mode: DisplayMode) {
        this.displayMode = mode
        when (this.displayMode) {
            DisplayMode.ABILITIES -> {
                listOf("monster_lore", "lore_line", "lore_background", "monster_image").map { findActor<Actor>(it) }.forEach { actor ->
                    actor.addAction(Actions.sequence(
                        Actions.alpha(0f, 0.5f),
                        Actions.visible(false)
                    ))
                }
                scroll.addAction(Actions.sequence(
                    Actions.visible(true),
                    Actions.alpha(1f, 0.5f)
                ))
                buttonSwitch.setText("Show information")
            }
            DisplayMode.DESCRIPTION -> {
                listOf("monster_lore", "lore_line", "lore_background", "monster_image").map { findActor<Actor>(it) }.forEach { actor ->
                    actor.addAction(Actions.sequence(
                        Actions.visible(true),
                        Actions.alpha(1f, 0.5f),
                    ))
                    scroll.addAction(Actions.sequence(
                        Actions.alpha(0f, 0.5f),
                        Actions.visible(false)
                    ))
                }

                buttonSwitch.setText("Show abilities")
            }
        }
    }
}
