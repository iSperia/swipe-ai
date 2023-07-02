package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.ux.IconedButton
import com.pl00t.swipe_client.services.battle.MonsterConfiguration
import com.pl00t.swipe_client.ux.Fonts
import com.pl00t.swipe_client.ux.hideToBehindAndRemove
import ktx.actors.onClick
import kotlin.math.max

private enum class DisplayMode {
    DESCRIPTION,
    ABILITIES,
}

class MonsterDetailActor(
    private val w: Float,
    private val h: Float,
    private val monsterInfo: MonsterConfiguration,
    private val monsterAtlas: TextureAtlas,
    private val coreAtlas: TextureAtlas,
    private val tarotAtlas: TextureAtlas,
): Group() {

    val backgroundImage: Image
    val monsterImage: Image
    val abilitiesGroup: Group
    val abilitiesScroll: ScrollPane
    val monsterName: Label
    val monsterNameBackground: Image
    val monsterLore: Label
    val monsterLoreBackground: Image
    val monsterProperties: Label
    val buttonClose: IconedButton
    val buttonAbilities: IconedButton

    private var displayMode = DisplayMode.DESCRIPTION

    private val _nameHeight = w / 7f
    private val _loreHeight = w * 0.3f
    private val _loreBgHeight = _loreHeight * 1.2f * 2f
    private val _buttonHeight = w * 0.1f
    private val _buttonWidth = w * 0.4f
    private val _nameBgHeight = (_buttonHeight + _nameHeight) * 1.5f

    init {
        backgroundImage = Image(coreAtlas.findRegion("semi_black_pixel")).apply {
            width = w
            height = h
        }
        monsterImage = Image(monsterAtlas.findRegion(monsterInfo.skin.toString())).apply {
            width = w * 0.9f
            height = h * 0.9f
            x = w * 0.05f
            y = h * 0.05f
            setScaling(Scaling.fit)
        }
        monsterName = Fonts.createWhiteTitle(monsterInfo.name, _nameHeight).apply {
            y = h - _nameHeight - _buttonHeight
            x = w * 0.05f
            setAlignment(Align.center)
            width = w * 0.9f
            height = _nameHeight
        }
        buttonClose = IconedButton(_buttonWidth, _buttonHeight, "Close", "icon_close", coreAtlas, coreAtlas).apply {
            x = this@MonsterDetailActor.w * 0.55f
            y = monsterName.y + _nameHeight - this@MonsterDetailActor.w * 0.025f
        }
        buttonAbilities = IconedButton(_buttonWidth, _buttonHeight, "Abilities", "icon_question", coreAtlas, coreAtlas, Align.left).apply {
            x = this@MonsterDetailActor.w * 0.05f
            y = buttonClose.y
        }
        buttonClose.onClick { this@MonsterDetailActor.hideToBehindAndRemove(this@MonsterDetailActor.h) }
        buttonAbilities.onClick {
            this@MonsterDetailActor.updateDisplayMode(when (displayMode) {
                DisplayMode.ABILITIES -> DisplayMode.DESCRIPTION
                DisplayMode.DESCRIPTION -> DisplayMode.ABILITIES
            })
        }
        monsterNameBackground = Image(coreAtlas.findRegion("top_gradient")).apply {
            y = h - _nameBgHeight
            width = w
            height = _nameBgHeight
        }
        monsterLore = Fonts.createCaptionAccent(monsterInfo.lore, _loreHeight * 0.3f).apply {
            width = w * 0.9f
            x = w * 0.05f
            height = _loreHeight
            setAlignment(Align.topLeft)
            wrap = true
        }
        monsterLoreBackground = Image(coreAtlas.findRegion("top_gradient")).apply {
            scaleY = -1f
            y = _loreBgHeight
            width = w
            height = _loreBgHeight
        }
        monsterProperties = Fonts.createWhiteCaption("", _loreHeight / 2f).apply {
            width = w * 0.9f
            x = w * 0.05f
            setAlignment(Align.left)
            height = _loreHeight
            y = _loreHeight
            wrap = true
        }

        abilitiesGroup = Group()
        fillAbilities()
        abilitiesScroll = ScrollPane(abilitiesGroup).apply {
            x = 0f
            y = 0f
            width = w
            height = monsterName.y
        }
        abilitiesScroll.isVisible = false

        addActor(backgroundImage)
        addActor(monsterImage)
        addActor(abilitiesScroll)
        addActor(monsterNameBackground)
        addActor(monsterName)
        addActor(buttonClose)
        addActor(buttonAbilities)
        addActor(monsterLoreBackground)
        addActor(monsterLore)
        addActor(monsterProperties)
    }

    private fun fillAbilities() {
        val abilityActors = monsterInfo.abilities?.map { config ->
            MonsterAbilityDetailActor(config, w, coreAtlas, tarotAtlas)
        } ?: emptyList()
        var cursor = max(monsterName.y.toInt(), abilityActors.sumOf { it.height.toInt() }) + monsterName.y - monsterNameBackground.y
        abilitiesGroup.height = cursor
        cursor -= (monsterName.y - monsterNameBackground.y).toInt()
        abilitiesGroup.width = w
        abilityActors.forEach { actor ->
            cursor -= actor.height.toInt()
            actor.y = cursor.toFloat()
            abilitiesGroup.addActor(actor)
        }
    }

    private fun updateDisplayMode(mode: DisplayMode) {
        this.displayMode = mode
        when (this.displayMode) {
            DisplayMode.ABILITIES -> {
                listOf(monsterImage, monsterLoreBackground, monsterLore, monsterProperties).forEach { actor ->
                    actor.addAction(Actions.sequence(
                        Actions.alpha(0f, 0.5f),
                        Actions.visible(false)
                    ))
                }
                abilitiesScroll.addAction(Actions.sequence(
                    Actions.visible(true),
                    Actions.alpha(1f, 0.5f)
                ))
                buttonAbilities.updateText("Abilities")
            }
            DisplayMode.DESCRIPTION -> {
                listOf(monsterImage, monsterLoreBackground, monsterLore, monsterProperties).forEach { actor ->
                    actor.addAction(Actions.sequence(
                        Actions.visible(true),
                        Actions.alpha(1f, 0.5f),
                    ))
                }
                abilitiesScroll.addAction(Actions.sequence(
                    Actions.alpha(0f, 0.5f),
                    Actions.visible(false)
                ))
                buttonAbilities.updateText("Information")
            }
        }
    }
}
