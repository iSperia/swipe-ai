package com.pl00t.swipe_client.items

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.game7th.items.ItemCategory
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.home.ReloadableScreen
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.ux.ItemCellActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class InventoryWindow(
    protected val r: R,
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

    private var currencyMode: Boolean = true
    private var categoryFilter: ItemCategory? = null

    init {
        setSize(r.width, r.height)

        background = r.image(R.ux_atlas, "texture_screen")
            .apply { setSize(r.width, r.height); alpha = 0.5f; color = r.skin().getColor("bg_color") }
        backgroundShadow = r.image(R.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
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
                        currencyMode = true
                        scrollPane.setSize(480f, r.height - 190f)
                        scrollPane.y = 110f
                        filterPanel.isVisible = false
                        loadData()
                    }
                },
                ActionCompositeButton(r, Action.Equipment, Mode.SingleLine(UiTexts.NavItems.value(r.l))).apply {
                    onClick {
                        currencyMode = false
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
            if (currencyMode) {
                showCurrency()
            } else {
                showItems()
            }
            content.row()
            content.add().growY()
        }
    }

    private suspend fun showItems() {
        r.profileService.getItems().filter { categoryFilter == null || categoryFilter == it.category }
            .sortedByDescending { it.rarity * 100000 + it.experience }.forEachIndexed { index, item ->
            val meta = r.itemService.getItemTemplate(item.skin)!!
            val actor = ItemCellActor(
                r = r,
                model = FrontItemEntryModel(
                    skin = meta.skin,
                    amount = 1,
                    level = SwipeCharacter.getLevel(item.experience),
                    rarity = item.rarity,
                    name = meta.name,
                    currency = null,
                    item = item
                )
            ).apply {
                onClick { onItemClicked(item.id) }
            }
            content.add(actor).size(120f, 160f)
            if (index % 4 == 3) {
                content.row()
            }
        }
    }

    private suspend fun showCurrency() {
        r.profileService.getProfile().balances.filter { it.amount > 0 }
            .map { it to r.profileService.getCurrency(it.currency) }
            .sortedByDescending { it.second.rarity }
            .forEachIndexed { index, p ->
                val meta = p.second
                val balance = p.first
                val actor = ItemCellActor(
                    r = r,
                    model = FrontItemEntryModel(
                        skin = balance.currency.toString(),
                        amount = balance.amount,
                        level = 0,
                        rarity = meta.rarity,
                        name = meta.name,
                        currency = meta.currency,
                        item = null
                    )
                ).apply {
                    touchable = Touchable.disabled
                }
                content.add(actor).size(120f, 160f)
                if (index % 4 == 3) {
                    content.row()
                }
            }
    }


}
