package com.pl00t.swipe_client.screen.items

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.game7th.items.InventoryItem
import com.game7th.items.ItemAffix
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.map.InventoryCellActor
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
}

class ItemDetailsActor(
    private val item: InventoryItem,
    private val context: SwipeContext,
    private val skin: Skin,
    private val actions: List<ItemBrowserAction>,
    private val callback: suspend (ItemBrowserAction) -> Unit
): Group() {

    init {
        val bg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_rarity", item.rarity).require()).apply {
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
        val cell = InventoryCellActor(context, skin, 120f, item).apply {
            x = 5f
            y = bg.height - 125f
        }
        addActor(cell)
        addActor(lineTop)
        addActor(lineBottom)

        KtxAsync.launch {
            context.itemService().getItemTemplate(item.skin)?.let { itemTemplate ->
                val title = Label(itemTemplate.name, skin, "item_rarity_${item.rarity}").apply {
                    x = 130f
                    y = bg.height - 30f
                    width = 335f
                    height = 30f
                    setAlignment(Align.center)
                }
                addActor(title)

                val loreLabel = Label(itemTemplate.lore, skin, "lore_small").apply {
                    x = 10f
                    y = 50f
                    width = 470f
                    height = 40f
                    wrap = true
                    setAlignment(Align.bottomLeft)
                }
                addActor(loreLabel)
            }

            val table = Table().apply {
                width = 335f
                x = 140f
                y = 50f
                width = 335f
                height = bg.height - 80f
            }
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

            table.add().growY()
            addActor(table)
        }

        var buttonX = 380f

        actions.forEach { action ->
            val text = when (action) {
                ItemBrowserAction.CLOSE -> "Close"
                ItemBrowserAction.EQUIP -> "Equip"
                ItemBrowserAction.UNEQUIP -> "Unequip"
                ItemBrowserAction.UPGRADE -> "Upgrade"
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
}
