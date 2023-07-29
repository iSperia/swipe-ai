package com.pl00t.swipe_client.screen.items

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.game7th.items.InventoryItem
import com.game7th.items.ItemCategory
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync

class ItemBrowserActor(
    var categoryFilter: ItemCategory?,
    var selectedId: String?,
    private val browserWidth: Float,
    private val browserHeight: Float,
    private val context: SwipeContext,
    private val skin: Skin,
    private val actionsProvider: suspend (InventoryItem) -> List<ItemBrowserAction>,
    private val actionsHandler: suspend (ItemBrowserAction, InventoryItem) -> Unit
) : Group() {

    private val table = Table()
    private val scroll = ScrollPane(table).apply {
        width = browserWidth
        height = browserHeight
        y = 280f
    }

    private val detailsContainer = Group()
    private var detailsActor: ItemDetailsActor? = null
    private var pityCloseButton: TextButton? = null

    init {
        val bg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_dark_blue")).apply {
            width = browserWidth
            height = browserHeight + 280f
        }
        addActor(bg)
        addActor(scroll)
        val l1 = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_line")).apply {
            width = 480f
            height = 4f
            y = bg.height - 4f
        }
        val l2 = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_line")).apply {
            width = 480f
            height = 4f
            y = scroll.y - 4f
        }
        addActor(detailsContainer)
        addActor(l1)
        addActor(l2)

        reloadData()
    }

    fun reloadData() {
        table.clearChildren()
        KtxAsync.launch {
            val items = context.profileService().getItems().let {
                if (categoryFilter != null) {
                    it.filter { it.category == categoryFilter }
                } else {
                    it
                }
            }
            if (selectedId == null) {
                selectedId = items.firstOrNull()?.id
            }
            if (items.isNotEmpty()) {
                items.forEachIndexed { index, item ->
                    val actor = InventoryCellActor(context, skin, 96f, item)
                    actor.name = item.id
                    actor.onClick {
                        println("Clicked ${item.id}")
                        selectItem(this.name)
                    }
                    table.add(actor).width(96f).height(96f)
                    if (index % 5 == 4) {
                        table.row()
                    }
                }
                table.row()
                table.add().growY()
                selectItem(selectedId ?: "")
            } else {

            }
        }
    }

    fun selectItem(id: String?) {
        table.cells.forEach { cell ->
            if (cell.actor != null && cell.actor is InventoryCellActor) {
                val actor = cell.actor as InventoryCellActor
                actor.setFocused(actor.name == id)

                KtxAsync.launch {
                    context.profileService().getItems().firstOrNull { it.id == id }?.let { item ->
                        detailsActor?.remove()
                        detailsActor = ItemDetailsActor(item, context, skin, actionsProvider(item)) { action ->
                            actionsHandler(action, item)
                        }
                        detailsContainer.addActor(detailsActor)
                    }
                }
            }
        }
    }
}
