package com.pl00t.swipe_client.heroes

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.items.InventoryItem
import com.game7th.items.ItemCategory
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.Action
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.action.Mode
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.ux.ItemCellActor
import com.pl00t.swipe_client.ux.ItemRowActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class HeroEquipmentActor(
    private val r: R,
    private val skin: String,
    private val onChanged: () -> Unit,
    private val onItemClicked: (String) -> Unit
): Group() {

    private val content = Table().apply {
        width = 480f
    }
    private val scrollpane = ScrollPane(content).apply {
        width = 480f
        height = r.height - 190f
        y = 110f
    }

    private var selectedCategory: ItemCategory? = null
    private var selectedItemId: String? = null

    init {
        addActor(scrollpane)

        loadData()
    }

    private fun loadData() {
        KtxAsync.launch {
            val itemMap = mutableMapOf<ItemCategory, InventoryItem>()
            val items = r.profileService.getItems().filter { it.equippedBy == skin }
            items.forEach { itemMap[it.category] = it }

            content.clearChildren()
            createRagdoll(itemMap)
            if (selectedItemId != null) {
                val item = r.profileService.getItems().first { it.id == selectedItemId }
                val template = r.itemService.getItemTemplate(item.skin)!!
                val row = ItemRowActor(r, FrontItemEntryModel(
                    skin = item.skin,
                    amount = 0,
                    level = SwipeCharacter.getLevel(item.experience),
                    rarity = item.rarity,
                    name = template.name,
                    currency = null,
                    item = item
                ),
                    action = ActionCompositeButton(
                        r = r,
                        action = Action.Close,
                        mode = Mode.SingleLine(UiTexts.PutOff.value(r.l))
                    ).apply {
                        onClick {
                            KtxAsync.launch {
                                selectedItemId = null
                                r.profileService.unequipItem(item.id)
                                loadData()
                                onChanged()
                            }
                        }
                    },
                    onItemClick = onItemClicked
                )
                content.add(row).row()
            }

            if (selectedCategory != null) {
                val items = r.profileService.getItems().filter { it.category == selectedCategory && it.id != selectedItemId }.sortedByDescending { it.rarity * 100000 +  it.experience }
                items.forEachIndexed { index, inventoryItem ->
                    val template = r.itemService.getItemTemplate(inventoryItem.skin)!!
                    val row = ItemRowActor(r, FrontItemEntryModel(
                        skin = inventoryItem.skin,
                        amount = 0,
                        level = SwipeCharacter.getLevel(inventoryItem.experience),
                        rarity = inventoryItem.rarity,
                        name = template.name,
                        currency = null,
                        item = inventoryItem
                    ),
                        action = ActionCompositeButton(
                            r = r,
                            action = Action.Complete,
                            mode = Mode.SingleLine(UiTexts.PutOn.value(r.l))
                        ).apply {
                            onClick {
                                KtxAsync.launch {
                                    selectedItemId = inventoryItem.id
                                    r.profileService.equipItem(skin, inventoryItem)
                                    loadData()
                                    onChanged()
                                }
                            }
                        },
                        onItemClick = onItemClicked
                    )
                    content.add(row).row()
                }
            }

            content.row()
            content.add().growY()
        }
    }

    private suspend fun createRagdoll(itemMap: Map<ItemCategory, InventoryItem>) {
        val group = Group().apply {
            setSize(480f, 480f)
        }

        group.addActor(createItem(itemMap[ItemCategory.AMULET], ItemCategory.AMULET).apply {
            setPosition(0f, 320f)
        })
        group.addActor(createItem(itemMap[ItemCategory.RING], ItemCategory.RING).apply {
            setPosition(0f, 160f)
        })
        group.addActor(createItem(itemMap[ItemCategory.BELT], ItemCategory.BELT).apply {
            setPosition(0f, 0f)
        })

        group.addActor(createItem(itemMap[ItemCategory.HELMET], ItemCategory.HELMET).apply {
            setPosition(360f, 320f)
        })
        group.addActor(createItem(itemMap[ItemCategory.GLOVES], ItemCategory.GLOVES).apply {
            setPosition(360f, 160f)
        })
        group.addActor(createItem(itemMap[ItemCategory.BOOTS], ItemCategory.BOOTS).apply {
            setPosition(360f, 0f)
        })

        group.addActor(r.image(R.units_atlas, skin).apply {
            setPosition(120f, 0f)
            setSize(240f, 480f)
            align = Align.bottom
            setScaling(Scaling.fit)
        })

        content.add(group).row()
    }

    private suspend fun createItem(item: InventoryItem?, category: ItemCategory): Actor {
        return if (item == null) {
            val group = Group().apply {
                width = 120f
                height = 160f
            }

            val shadow = r.image(R.ux_atlas, "gradient_item_background").apply {
                setSize(120f, 120f)
                setPosition(0f, 30f)
                alpha = 0.5f
            }
            val plus = r.image(R.ux_atlas, "button_plus").apply {
                setSize(100f, 100f)
                setPosition(shadow.x + 10f, shadow.y + 10f)
                setColor(r.skin().getColor("focus_color"))
                alpha = 0.25f
            }
            val label = r.regular20White(category.label.value(r.l)).apply {
                setSize(120f, 30f)
                setAlignment(Align.center)
            }
            group.addActor(shadow)
            group.addActor(plus)
            group.addActor(label)

            group.onClick {
                selectedItemId = null
                selectedCategory = category
                loadData()
            }
            group
        } else {
            val template = r.itemService.getItemTemplate(item.skin)!!
            ItemCellActor(r, FrontItemEntryModel(
                skin = item.skin,
                amount = 0,
                level = SwipeCharacter.getLevel(item.experience),
                rarity = item.rarity,
                name = template.name,
                currency = null,
                item = item,
            )).apply {
                onClick {
                    selectedItemId = item.id
                    selectedCategory = category
                    loadData()
                }
            }
        }
    }
}
