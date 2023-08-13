package com.pl00t.swipe_client.ux

import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class ItemBrowser(
    private val r: Resources,
    private var items: List<FrontItemEntryModel>,
    private var onItemClick: ((String) -> Unit)?,
    private var actionProvider: (suspend (FrontItemEntryModel) -> ActionCompositeButton?)?,
) : Table() {

    var selectedIndex: Int? = null

    init {
        width = 480f
        align(Align.topLeft)
        drawItems()
    }

    fun drawItems() {
        KtxAsync.launch {
            clearChildren()
            val lastIndex = items.size - 1
            var selectionRow: ItemRowActor? = null
            var row: Table? = null
            items.forEachIndexed { index, entry ->
                if (row == null) {
                    row = Table().apply { width = 480f; align(Align.left) }
                }
                val cell = ItemCellActor(r, entry)
                cell.onClick {
                    if (index != selectedIndex) {
                        selectedIndex = index
                    } else {
                        selectedIndex = null
                    }
                    drawItems()
                }
                row!!.add(cell).size(120f, 140f).align(Align.left)


                if (index == selectedIndex) {
                    selectionRow = ItemRowActor(r, entry, actionProvider?.invoke(entry), onItemClick)
                    cell.addAction(Actions.moveBy(0f, -5f, 0.3f))
                }

                if (index % 4 == 3 || index == lastIndex) {
                    row!!.add().growX().row()
                    add(row).width(480f).align(Align.left)
                    row()
                    row = null
                    if (selectionRow != null) {
                        add(selectionRow).row()
                        selectionRow = null
                    }
                }
            }

            row()
            add().growY()
        }
    }
}
