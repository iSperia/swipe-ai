package com.pl00t.swipe_client.heroes

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.game.FrontMonsterConfiguration
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.monster.AttributesActor
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.ItemCellActor
import com.pl00t.swipe_client.ux.LevelProgressActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.actors.onExit
import ktx.actors.onTouchDown
import ktx.async.KtxAsync

class HeroAttributesActor(
    r: R,
    character: SwipeCharacter,
    model: FrontMonsterConfiguration,
) : AttributesActor(r, model) {

    val buttonApprove = r.image(R.ux_atlas, "fg_complete").apply {
        setSize(36f, 36f)
        setPosition(480f - 42f, 202f)
        touchable = Touchable.disabled
        alpha = 0.5f
        setOrigin(Align.center)
        onTouchDown {
            addAction(Actions.scaleTo(0.9f, 0.9f, 0.2f))
        }
        onExit {
            addAction(Actions.scaleTo(1f, 1f, 0.2f))
        }
        onClick {
            r.profileService.spendCurrency(currencies, useCount)
            r.profileService.addCharacterExperience(model.skin, boostExp)


            KtxAsync.launch {
                this@HeroAttributesActor.model = r.profileService.createCharacter(model.skin)

                val character = r.profileService.getCharacters().first { it.skin == model.skin }
                baseLevel = model.level
                baseExp = character.experience

                currencies.forEachIndexed { i, c -> balances[i] = r.profileService.getProfile().getBalance(c) }
                useCount.indices.forEach { i -> useCount[i] = 0 }

                refreshData()
                recalculate()
            }
        }
    }

    init {
        currencies[0] = SwipeCurrency.SCROLL_OF_WISDOM
        currencies[1] = SwipeCurrency.TOME_OF_ENLIGHTMENT
        currencies[2] = SwipeCurrency.CODEX_OF_ASCENDANCY
        currencies[3] = SwipeCurrency.GRIMOIRE_OF_OMNISCENCE

        KtxAsync.launch {
            baseLevel = model.level
            baseExp = character.experience

            handleDataLoaded()
            addActor(buttonApprove)
        }
    }

    override fun getTableHeight(): Float {
        return super.getTableHeight() + 240f
    }

    override fun handleDataLoaded() {
        progressActor = LevelProgressActor(r).apply {
            y = 200f
        }
        addActor(progressActor)
        addCurrencyItems()
        super.handleDataLoaded()
    }

    override fun addCurrencyItems() {
        KtxAsync.launch {
            val profile = r.profileService.getProfile()
            val entries = currencies.mapIndexed { i, c ->
                balances[i] = profile.getBalance(c)
                val meta = r.profileService.getCurrency(c)
                FrontItemEntryModel(
                    c.toString(),
                    balances[i],
                    level = 0,
                    rarity = meta.rarity,
                    name = meta.name,
                    currency = c,
                    item = null
                )
            }
            entries.forEachIndexed { index, model ->
                val button = ItemCellActor(
                    r = r,
                    model = model
                ).apply {
                    x = index * 120f
                    y = 20f
                    if (balances[index] <= 0) {
                        touchable = Touchable.disabled
                        alpha = 0.4f
                    }
                }
                button.onClick {
                    if (useCount[index] < balances[index]) {
                        useCount[index]++
                        cells[index]?.reduceCount()
                    }
                    recalculate()
                }
                cells[index] = button
                addActor(button)
            }
        }
    }

    override fun recalculate() {
        var expBoost = 0
        currencies.forEachIndexed { i, c ->
            expBoost += c.expBonus * useCount[i]
        }
        boostExp = expBoost
        if (expBoost > 0) {
            buttonApprove.alpha = 1f
            buttonApprove.touchable = Touchable.enabled
        } else {
            buttonApprove.alpha = 0.5f
            buttonApprove.touchable = Touchable.disabled
        }

        val newExp = expBoost + baseExp
        val newLevel = SwipeCharacter.getLevel(baseExp + expBoost)
        val levelDelta = newLevel - baseLevel
        val newLevelMax = SwipeCharacter.experience[newLevel]
        val newLevelMin = SwipeCharacter.experience[newLevel - 1]

        progressActor.setState(model.level, levelDelta, expBoost, baseExp - newLevelMin, newExp - newLevelMin, newLevelMax - newLevelMin)
    }
}
