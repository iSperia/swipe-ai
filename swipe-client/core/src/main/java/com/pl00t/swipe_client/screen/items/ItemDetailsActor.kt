package com.pl00t.swipe_client.screen.items

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.game7th.items.InventoryItem
import com.game7th.items.ItemAffix
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.map.InventoryCellActor
import com.pl00t.swipe_client.screen.reward.CurrencyRewardEntryActor
import com.pl00t.swipe_client.services.profile.CollectedReward
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.require
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync
import java.lang.StringBuilder

enum class ItemBrowserAction {
    CLOSE,
    EQUIP,
    UNEQUIP,
    UPGRADE,
    DUST,
}

enum class ItemDetailsMode {
    AFFIXES, UPGRADE
}

class ItemDetailsActor(
    private var item: InventoryItem,
    private val context: SwipeContext,
    private val skin: Skin,
    private val actions: List<ItemBrowserAction>,
    private val callback: suspend (ItemBrowserAction) -> Unit
): Group() {

    private val title: Label
    private val lore: Label
    private val bg: Image
    private val cell: InventoryCellActor
    private val progress: ItemExperienceActor
    private val buttonSwitch: TextButton

    private val scroll: ScrollPane
    private val table: Table

    private var mode: ItemDetailsMode = ItemDetailsMode.AFFIXES

    init {
        bg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_rarity", item.rarity).require()).apply {
            width = 480f
            height = 280f
        }
        val lineTop = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_line")).apply {
            width = 480f
            height = 4f
        }
        val lineBottom = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_line")).apply {
            width = 480f
            height = 4f
            y = bg.height - 4f
        }


        addActor(bg)
        cell = InventoryCellActor(context, skin, 120f, item).apply {
            x = 5f
            y = bg.height - 125f
        }
        addActor(cell)

        progress = ItemExperienceActor(item.id, context, skin).apply {
            x = cell.x
            y = cell.y - 25f
        }
        buttonSwitch = Buttons.createShortActionButton("", skin).apply {
            width = 120f
            x = progress.x
            y = progress.y - 40f
        }

        buttonSwitch.onClick {
            KtxAsync.launch {
                when (mode) {
                    ItemDetailsMode.AFFIXES -> showCraft()
                    ItemDetailsMode.UPGRADE -> showAffixes()
                }
            }
        }

        addActor(progress)
        addActor(buttonSwitch)
        addActor(lineTop)
        addActor(lineBottom)

        table = Table().apply {
            width = 335f
            x = 140f
            y = 90f
        }
        scroll = ScrollPane(table).apply {
            x = 140f
            y = 90f
            width = 335f
            height = bg.height - 130f
        }
        addActor(scroll)


        title = Label("", skin, "item_rarity_${item.rarity}").apply {
            x = 130f
            y = bg.height - 30f
            width = 335f
            height = 30f
            setAlignment(Align.center)
        }
        addActor(title)

        lore = Label("", skin, "lore_small").apply {
            x = 10f
            y = 50f
            width = 470f
            height = 40f
            wrap = true
            setAlignment(Align.bottomLeft)
        }

        addActor(title)
        addActor(lore)

        loadData()

        showAffixes()

        var buttonX = 380f

        actions.forEach { action ->
            val text = when (action) {
                ItemBrowserAction.CLOSE -> "Close"
                ItemBrowserAction.EQUIP -> "Equip"
                ItemBrowserAction.UNEQUIP -> "Unequip"
                ItemBrowserAction.UPGRADE -> "Upgrade"
                ItemBrowserAction.DUST -> "Dust"
            }
            val button = Buttons.createShortActionButton(text, skin).apply {
                x = buttonX + 10f
                y = 10f
            }
            button.onClick { KtxAsync.launch { callback(action) }}
            buttonX -= 100f

            addActor(button)
        }

    }

    private fun loadData() {
        KtxAsync.launch {
            item = context.profileService().getItems().first { item.id == it.id }
            context.itemService().getItemTemplate(item.skin)?.let { itemTemplate ->
                title.setText(itemTemplate.name)
                lore.setText(itemTemplate.lore)
                progress.loadData()
                cell.updateLevel(item.level)

                if (item.level >= item.maxLevel) {
                    buttonSwitch.isVisible = false
                    if (mode == ItemDetailsMode.UPGRADE) {
                        showAffixes()
                    }
                }
            }
        }
    }

    private fun showAffixes() {
        this.mode = ItemDetailsMode.AFFIXES
        KtxAsync.launch { showContent() }
    }

    private suspend fun showCraft() {
        this.mode = ItemDetailsMode.UPGRADE
        KtxAsync.launch { showContent() }
    }

    private suspend fun showContent() {
        this.buttonSwitch.setText(when (this.mode) {
            ItemDetailsMode.AFFIXES -> "Upgrade"
            ItemDetailsMode.UPGRADE -> "Details"
        })

        when (this.mode) {
            ItemDetailsMode.AFFIXES -> {
                table.clearChildren()

                item.implicit.forEach { implicitAffix ->
                    val labelText = context.itemService().getAffix(implicitAffix.affix)?.description?.replace("$", implicitAffix.value.toString()) + " (tier ${implicitAffix.level})"
                    val label =  Label(labelText, skin, "implicit_text").apply {
                        width = 335f
                        wrap = true
                        setAlignment(Align.topLeft)
                    }
                    table.add(label).width(335f).align(Align.topLeft).row()
                }
                item.affixes.forEach { affix ->
                    val labelText = context.itemService().getAffix(affix.affix)?.description?.replace("$", affix.value.toString()) + " (tier ${affix.level})"
                    val label = Label(labelText, skin, "affix_text").apply {
                        width = 335f
                        wrap = true
                        setAlignment(Align.topLeft)
                    }
                    table.add(label).width(335f).align(Align.topLeft).row()
                }

                table.row()
                table.add().grow()
            }
            ItemDetailsMode.UPGRADE -> {

                KtxAsync.launch {
                    table.clearChildren()

                    val types = listOf(SwipeCurrency.INFUSION_ORB, SwipeCurrency.INFUSION_SHARD, SwipeCurrency.INFUSION_CRYSTAL, SwipeCurrency.ASCENDANT_ESSENCE)
                    val balances = types.map { it to context.profileService().getProfile().getBalance(it) }.filter { it.second > 0 }.toMap()
                    balances.entries.forEach { (currency, amount) ->
                        val meta = context.profileService().getCurrency(currency)

                        val g = Group().apply {
                            width = scroll.width
                            height = 36f
                        }
                        val icon = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion(meta.currency.toString())).apply {
                            width = 36f
                            height = 36f
                        }
                        val labelText = when (currency) {
                            SwipeCurrency.INFUSION_ORB -> "${meta.name} (+1 EXP)"
                            SwipeCurrency.INFUSION_SHARD -> "${meta.name} (+10 EXP)"
                            SwipeCurrency.INFUSION_CRYSTAL -> "${meta.name} (+100 EXP)"
                            SwipeCurrency.ASCENDANT_ESSENCE -> "${meta.name} (+1000 EXP)"
                            else -> ""
                        }
                        val amountLabel = Label("X$amount", skin, "text_small").apply {
                            width = 40f
                            height = 36f
                            setAlignment(Align.bottomRight)
                        }
                        val useButton = Buttons.createShortActionButton(labelText, skin).apply {
                            height = 34f
                            y = 1f
                            x = 50f
                            width = 240f
                        }

                        useButton.onClick {
                            KtxAsync.launch {
                                context.profileService().spendCraftCurrency(item.id, currency)
                                loadData()
                                showContent()
                            }
                        }

                        g.addActor(icon)
                        g.addActor(amountLabel)
                        g.addActor(useButton)

                        table.add(g).width(40f).height(36f).align(Align.left)

                        table.row()

                    }

                    table.row()
                    table.add().grow()
                }
            }
        }
    }
}
