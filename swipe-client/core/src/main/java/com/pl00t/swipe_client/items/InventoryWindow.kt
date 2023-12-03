package com.pl00t.swipe_client.items

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.game7th.items.ItemCategory
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.home.ReloadableScreen
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.ux.ItemBrowser
import com.pl00t.swipe_client.ux.ItemCellActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

enum class FilterMode {
    CURRENCY, GEMS, ITEMS
}

class InventoryWindow(
    protected val r: Resources,
    private val onClose: () -> Unit,
    private val onItemClicked: (String) -> Unit,
) : Group(), ReloadableScreen {

    lateinit var title: WindowTitleActor
    lateinit var bottomPanel: BottomActionPanel
    lateinit protected var background: Image
    lateinit protected var backgroundShadow: Image
    lateinit var filterPanel: BottomActionPanel

    private val content = Table().apply {
        width = 480f
    }
    private val scrollPane = ScrollPane(content).apply {
        setSize(480f, r.height - 190f)
        setPosition(0f, 110f)
    }

    private var filterMode: FilterMode = FilterMode.CURRENCY
    private var categoryFilter: ItemCategory? = null

    init {
        setSize(r.width, r.height)

        background = r.image(Resources.ux_atlas, "texture_screen")
            .apply { setSize(r.width, r.height); alpha = 0.5f; color = r.skin().getColor("bg_color") }
        backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(background)
        addActor(backgroundShadow)

        addActor(scrollPane)

        loadData()

        addTitle()
        addBottomPanel()
    }

    private fun addTitle() {
        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        val titleText = UiTexts.NavItems.value(r.l)
        title = WindowTitleActor(r, titleText, closeButton, null, 2).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun addBottomPanel() {
        KtxAsync.launch {
            val actions = listOf(
                ActionCompositeButton(
                    r,
                    Action.FilterCurrency,
                    Mode.SingleLine(UiTexts.FilterCurrency.value(r.l))
                ).apply {
                    onClick {
                        filterMode = FilterMode.CURRENCY
                        scrollPane.setSize(480f, r.height - 190f)
                        scrollPane.y = 110f
                        filterPanel.isVisible = false
                        loadData()
                    }
                },
                ActionCompositeButton(
                    r,
                    Action.FilterGems,
                    Mode.SingleLine(UiTexts.FilterGems.value(r.l))
                ).apply {

                },
                ActionCompositeButton(r, Action.Equipment, Mode.SingleLine(UiTexts.NavItems.value(r.l))).apply {
                    onClick {
                        filterMode = FilterMode.ITEMS
                        categoryFilter = null
                        scrollPane.setSize(480f, r.height - 300f)
                        scrollPane.y = 220f
                        filterPanel.isVisible = true
                        loadData()
                    }
                }
            )
            bottomPanel = BottomActionPanel(r, actions, 2)
            addActor(bottomPanel)

            filterPanel = BottomActionPanel(
                r = r,
                actions = listOf(
                    ActionCompositeButton(
                        r,
                        Action.FilterHelmet,
                        Mode.SingleLine(UiTexts.FilterHelm.value(r.l))
                    ).apply {
                        onClick { categoryFilter = ItemCategory.HELMET; loadData() }
                    },
                    ActionCompositeButton(
                        r,
                        Action.FilterGloves,
                        Mode.SingleLine(UiTexts.FilterGloves.value(r.l))
                    ).apply {
                        onClick { categoryFilter = ItemCategory.GLOVES; loadData() }
                    },
                    ActionCompositeButton(
                        r,
                        Action.FilterBoots,
                        Mode.SingleLine(UiTexts.FilterBoots.value(r.l))
                    ).apply {
                        onClick { categoryFilter = ItemCategory.BOOTS; loadData() }
                    },
                    ActionCompositeButton(
                        r,
                        Action.FilterAmulet,
                        Mode.SingleLine(UiTexts.FilterAmulet.value(r.l))
                    ).apply {
                        onClick { categoryFilter = ItemCategory.AMULET; loadData() }
                    },
                    ActionCompositeButton(r, Action.FilterRing, Mode.SingleLine(UiTexts.FilterRing.value(r.l))).apply {
                        onClick { categoryFilter = ItemCategory.RING; loadData() }
                    },
                    ActionCompositeButton(r, Action.FilterBelt, Mode.SingleLine(UiTexts.FilterBelt.value(r.l))).apply {
                        onClick { categoryFilter = ItemCategory.BELT; loadData() }
                    },
                ),
                backgroundRarity = 1
            ).apply {
                isVisible = false
                y = 110f
            }
            addActor(filterPanel)
        }
    }

    override fun reload() = loadData()

    fun loadData() {
        KtxAsync.launch {
            content.clearChildren()
            when (filterMode) {
                FilterMode.CURRENCY -> showCurrency()
                FilterMode.ITEMS -> showItems()
                else -> Unit
            }
            content.row()
            content.add().growY()
        }
    }

    private suspend fun showItems() {
        val items = r.profileService.getItems().filter { categoryFilter == null || categoryFilter == it.category }
            .sortedByDescending { it.rarity * 100000 + it.experience }.map { item ->
                val meta = r.itemService.getItemTemplate(item.skin)!!
                FrontItemEntryModel.InventoryItemEntryModel(
                    skin = meta.skin,
                    amount = 1,
                    level = SwipeCharacter.getLevel(item.experience),
                    rarity = item.rarity,
                    name = meta.name,
                    item = item
                )
            }
        content.add(ItemBrowser(r, items, onItemClicked) { item ->
            ActionCompositeButton(r, Action.Resistance, Mode.SingleLine(UiTexts.Details.value(r.l))).apply {
                onClick {
                    if (item is FrontItemEntryModel.InventoryItemEntryModel) {
                        onItemClicked(item.item.id)
                    }
                }
            }
        }).colspan(4).row()
    }

    private suspend fun showCurrency() {
        val items = r.profileService.getProfile().balances.filter { it.amount > 0 }
            .map { it to r.profileService.getCurrency(it.currency) }
            .sortedByDescending { it.second.rarity }
            .map {
                val meta = it.second
                val balance = it.first
                FrontItemEntryModel.CurrencyItemEntryModel(
                    skin = balance.currency.toString(),
                    amount = balance.amount,
                    level = 0,
                    rarity = meta.rarity,
                    name = meta.name,
                    currency = meta.currency,
                )
            }
            val actor = ItemBrowser(r, items, null, null)
            content.add(actor).colspan(4).row()
    }
}
