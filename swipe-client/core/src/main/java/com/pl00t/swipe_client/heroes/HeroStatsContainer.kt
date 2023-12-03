package com.pl00t.swipe_client.heroes

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.game.FrontMonsterConfiguration
import com.game7th.swipe.game.SbSoundType
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.Action
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.action.BottomActionPanel
import com.pl00t.swipe_client.action.Mode
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.ItemBrowser
import com.pl00t.swipe_client.ux.LevelProgressActor
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync


class HeroStatsContainer(
    private val r: Resources,
    private var model: FrontMonsterConfiguration
) : Group() {

    private enum class BrowseMode {
        STATS, ATTRIBUTES, RESISTANCES
    }

    var cachedSelectedExpCurrency: Int? = null
    var cachedSelectedElixir: Int? = null

    private var browseMode = BrowseMode.STATS

    val contentTable = Table().apply {
        width = 480f
    }
    private val scrollPane = ScrollPane(contentTable).apply {
        setPosition(0f, 110f)
        setSize(480f, r.height - 300f)
    }

    lateinit var bottomActionPanel: BottomActionPanel

    private fun reload() {
        KtxAsync.launch {
            model = r.profileService.createCharacter(model.skin)
            loadData()
        }
    }

    init {
        val actions = listOf(
            ActionCompositeButton(
                r = r,
                action = Action.Stats,
                mode = Mode.SingleLine(UiTexts.ButtonParameters.value(r.l))
            ).apply {
                onClick {
                    browseMode = BrowseMode.STATS
                    loadData()
                }
            },
            ActionCompositeButton(
                r = r,
                action = Action.Attributes,
                mode = Mode.SingleLine(UiTexts.ButtonStats.value(r.l))
            ).apply {
                onClick {
                    browseMode = BrowseMode.ATTRIBUTES
                    loadData()
                }
            },
            ActionCompositeButton(
                r = r,
                action = Action.Resistance,
                mode = Mode.SingleLine(UiTexts.ButtonResistances.value(r.l))
            ).apply {
                onClick {
                    browseMode = BrowseMode.RESISTANCES
                    loadData()
                }
            }
        )
        bottomActionPanel = BottomActionPanel(
            r = r,
            actions = actions,
            backgroundRarity = 1
        )

        addActor(scrollPane)

        addActor(bottomActionPanel)
        addActor(scrollPane)

        loadData()
    }

    fun loadData() {
        KtxAsync.launch {
            contentTable.clearChildren()
            when (browseMode) {
                BrowseMode.ATTRIBUTES -> {
                    renderAttributes()
                }
                BrowseMode.STATS -> {
                    renderStats()
                }
                BrowseMode.RESISTANCES -> {
                    renderResists()
                }
            }
            contentTable.row()
            contentTable.add().growY()
        }
    }

    private suspend fun renderResists() {
        val statTable = Table().apply {
            width = 480f
        }

        statTable.add(r.regular24White(UiTexts.AttributeLabelPhysResist.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus("${"%.0f".format(model.resist.phys * 100f)}%").apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()
        statTable.add(r.regular24White(UiTexts.AttributeLabelDarkResist.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus("${"%.0f".format(model.resist.dark * 100f)}%").apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()
        statTable.add(r.regular24White(UiTexts.AttributeLabelLightResist.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus("${"%.0f".format(model.resist.light * 100f)}%").apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()
        statTable.add(r.regular24White(UiTexts.AttributeLabelShockResist.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus("${"%.0f".format(model.resist.shock * 100f)}%").apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()
        statTable.add(r.regular24White(UiTexts.AttributeLabelFireResist.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus("${"%.0f".format(model.resist.fire * 100f)}%").apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()
        statTable.add(r.regular24White(UiTexts.AttributeLabelColdResist.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus("${"%.0f".format(model.resist.cold * 100f)}%").apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()

        contentTable.add(statTable).row()
    }

    private suspend fun renderAttributes() {
        val statTable = Table().apply {
            width = 480f
        }

        statTable.add(r.regular24White(UiTexts.AttributeLabelBody.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus(model.attributes.body.toString()).apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()
        statTable.add(r.regular24White(UiTexts.AttributeLabelSpirit.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus(model.attributes.spirit.toString()).apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()
        statTable.add(r.regular24White(UiTexts.AttributeLabelMind.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus(model.attributes.mind.toString()).apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()

        contentTable.add(statTable).row()

        val profile = r.profileService.getProfile()
        val items: List<FrontItemEntryModel.CurrencyItemEntryModel> = listOf(SwipeCurrency.ELIXIR_AMBER, SwipeCurrency.ELIXIR_JADE, SwipeCurrency.ELIXIR_LAPIS, SwipeCurrency.ELIXIR_TURQUOISE, SwipeCurrency.ELIXIR_AGATE, SwipeCurrency.ELIXIR_CITRINE).map {
            val meta = r.profileService.getCurrency(it)
            FrontItemEntryModel.CurrencyItemEntryModel(
                skin = meta.currency.toString(),
                amount = profile.getBalance(meta.currency),
                level = 0,
                rarity = meta.rarity,
                name = meta.name,
                currency = meta.currency,
            )
        }.filter { it.amount > 0 }

        if (items.isNotEmpty()) {
            val browser = ItemBrowser(
                r = r,
                items = items,
                onItemClick = null,
                actionProvider = { model ->
                    if (model is FrontItemEntryModel.CurrencyItemEntryModel) {
                        cachedSelectedElixir = items.indexOfFirst { it.currency == model.currency }
                        ActionCompositeButton(r, Action.Complete, Mode.SingleLine(UiTexts.UseItem.value(r.l))).apply {
                            onClick {
                                useElixir(model)
                                r.playSound(SbSoundType.POISON_DRINK)
                            }
                        }
                    } else {
                        null
                    }
                }
            ).apply {
                selectedIndex = cachedSelectedElixir
                drawItems()
            }
            contentTable.add(browser).colspan(4).row()
        }

    }

    fun useElixir(model: FrontItemEntryModel.CurrencyItemEntryModel) {
        KtxAsync.launch {
            if (r.profileService.useElixir(this@HeroStatsContainer.model.skin, model.currency!!)) {
                r.profileService.spendCurrency(arrayOf(model.currency!!), arrayOf(1))
                reload()
            }
        }
    }

    private suspend fun renderStats() {
        val character = r.profileService.getCharacters().first { it.skin == model.skin }
        val levelActor = LevelProgressActor(r).apply {
            val level = SwipeCharacter.getLevel(character.experience)
            val baseExp = SwipeCharacter.experience[level - 1]
            val newExp = SwipeCharacter.experience[level]
            val exp = character.experience - baseExp
            setState(SwipeCharacter.getLevel(character.experience),0, 0, exp, exp, newExp - baseExp, 100)
        }
        contentTable.add(levelActor).colspan(4).width(480f).padBottom(20f).padTop(20f).row()

        val statTable = Table().apply {
            width = 480f
        }

        statTable.add(r.regular24White(UiTexts.AttributeLabelHealth.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus(model.health.toString()).apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()
        statTable.add(r.regular24White(UiTexts.AttributeLabelLuck.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus("${"%.1f".format(model.luck)}%").apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()
        statTable.add(r.regular24White(UiTexts.AttributeLabelUltProgress.value(r.l)).apply { setAlignment(Align.right) }).size(340f, 30f).align(Align.right).colspan(3)
        statTable.add(r.regular24Focus("${model.ult} / ${model.ultMax}").apply { setAlignment(Align.left) }).align(Align.left).size(120f, 30f).padLeft(20f).colspan(1).row()

        contentTable.add(statTable).row()

        val profile = r.profileService.getProfile()
        val items: List<FrontItemEntryModel.CurrencyItemEntryModel> = listOf(SwipeCurrency.SCROLL_OF_WISDOM, SwipeCurrency.TOME_OF_ENLIGHTMENT, SwipeCurrency.CODEX_OF_ASCENDANCY, SwipeCurrency.GRIMOIRE_OF_OMNISCENCE).map {
            val meta = r.profileService.getCurrency(it)
            FrontItemEntryModel.CurrencyItemEntryModel(
                skin = meta.currency.toString(),
                amount = profile.getBalance(meta.currency),
                level = 0,
                rarity = meta.rarity,
                name = meta.name,
                currency = meta.currency,
            )
        }.filter { it.amount > 0 }
        if (items.isNotEmpty()) {
            val browser = ItemBrowser(
                r = r,
                items = items,
                onItemClick = null,
                actionProvider = { model ->
                    if (model is FrontItemEntryModel.CurrencyItemEntryModel) {
                        cachedSelectedExpCurrency = items.indexOfFirst { it.currency == model.currency }
                        ActionCompositeButton(r, Action.Complete, Mode.SingleLine(UiTexts.UseItem.value(r.l))).apply {
                            onClick {
                                useExperienceItem(model)
                            }
                        }
                    } else null
                }
            ).apply {
                selectedIndex = cachedSelectedExpCurrency
                drawItems()
            }
            contentTable.add(browser).colspan(4).row()
        }
    }

    fun useExperienceItem(model: FrontItemEntryModel.CurrencyItemEntryModel) {
        KtxAsync.launch {
            val oldLevel = SwipeCharacter.getLevel(r.profileService.getCharacters().first { it.skin == this@HeroStatsContainer.model.skin }.experience)
            r.profileService.addCharacterExperience(this@HeroStatsContainer.model.skin, model.currency.expBonus)
            r.profileService.spendCurrency(arrayOf(model.currency), arrayOf(1))
            val newLevel = SwipeCharacter.getLevel(r.profileService.getCharacters().first { it.skin == this@HeroStatsContainer.model.skin }.experience)

            r.playSound(SbSoundType.USE_TOME)
            if (oldLevel != newLevel) {
                r.playSound(SbSoundType.LEVELUP)
            }
            reload()
        }
    }
}
