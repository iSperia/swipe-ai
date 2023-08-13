package com.pl00t.swipe_client.heroes

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.items.InventoryItem
import com.game7th.items.ItemCategory
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.Action
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.action.Mode
import com.pl00t.swipe_client.analytics.AnalyticEvents
import com.pl00t.swipe_client.home.ReloadableScreen
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.ux.ItemCellActor
import com.pl00t.swipe_client.ux.ItemRowActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class HeroEquipmentActor(
    private val r: Resources,
    private val skin: String,
    private val onChanged: () -> Unit,
    private val onItemClicked: (String) -> Unit
): Group(), ReloadableScreen {

    private val content = Table().apply {
        width = 480f
    }
    private val scrollpane = ScrollPane(content).apply {
        width = 480f
        height = r.height - 190f
        y = 110f
    }

    var selectedCategory: ItemCategory? = null
    var selectedItemId: String? = null

    init {
        addActor(scrollpane)

        loadData()
    }

    override fun reload() = loadData()

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
                                r.analytics.trackEvent(AnalyticEvents.EquipEvent.EVENT_UNEQUIP)
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
                                    r.analytics.trackEvent(AnalyticEvents.EquipEvent.EVENT_EQUIP)
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
            setSize(480f, 420f)
        }

        group.addActor(createItem(itemMap[ItemCategory.AMULET], ItemCategory.AMULET).apply {
            setPosition(0f, 280f)
        })
        group.addActor(createItem(itemMap[ItemCategory.RING], ItemCategory.RING).apply {
            setPosition(0f, 140f)
        })
        group.addActor(createItem(itemMap[ItemCategory.BELT], ItemCategory.BELT).apply {
            setPosition(0f, 0f)
        })

        group.addActor(createItem(itemMap[ItemCategory.HELMET], ItemCategory.HELMET).apply {
            setPosition(360f, 280f)
        })
        group.addActor(createItem(itemMap[ItemCategory.GLOVES], ItemCategory.GLOVES).apply {
            setPosition(360f, 140f)
        })
        group.addActor(createItem(itemMap[ItemCategory.BOOTS], ItemCategory.BOOTS).apply {
            setPosition(360f, 0f)
        })

        group.addActor(r.image(Resources.units_atlas, skin).apply {
            setPosition(120f, 0f)
            setSize(240f, 420f)
            align = Align.bottom
            setScaling(Scaling.fit)
        })

        content.add(group).row()
    }

    private suspend fun createItem(item: InventoryItem?, category: ItemCategory): Actor {
        return if (item == null) {
            val group = Group().apply {
                width = 120f
                height = 140f
            }

            val shadow = r.image(Resources.ux_atlas, "gradient_item_background").apply {
                setSize(120f, 120f)
                setPosition(0f, 10f)
                alpha = 0.5f
            }
            val plus = r.image(Resources.ux_atlas, "button_plus").apply {
                setSize(70f, 70f)
                setPosition(shadow.x + 25f, shadow.y + 25f)
                alpha = 0.15f
            }
            val label = r.regular20White(category.label.value(r.l)).apply {
                setSize(120f, 30f)
                setPosition(0f, 10f)
                setAlignment(Align.center)
                alpha = 0.6f
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
