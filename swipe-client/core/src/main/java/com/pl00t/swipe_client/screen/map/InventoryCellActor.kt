package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.game7th.items.InventoryItem
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import ktx.actors.alpha

class InventoryCellActor(
    private val context: SwipeContext,
    private val skin: Skin,
    private val size: Float,
    private val item: InventoryItem,
) : Group() {

    init {
        val itemImage = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion(item.skin)).apply {
            width = size
            height = size
        }
        addActor(itemImage)

        val starSize = size / 5f
        val statPadding = (size - starSize * item.rarity) / 2f
        (0 until item.rarity).forEach { i ->
            val star = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("icon_star")).apply {
                width = starSize
                height = starSize
                x = statPadding + i * starSize
            }
            addActor(star)
            star.setScale(2f, 2f)
            star.alpha = 0f
            star.addAction(Actions.delay(0.1f * i, Actions.parallel(
                Actions.scaleTo(1f, 1f, 0.2f),
                Actions.alpha(1f, 0.2f)
            )))
        }

        val levelLabel = Label("Lvl. ${item.level}", skin, "text_small").apply {
            x = 2f
            y = size - 22f
            width = size - 4f
            height = 20f
            setAlignment(Align.topLeft)
        }
        addActor(levelLabel)

    }
}
