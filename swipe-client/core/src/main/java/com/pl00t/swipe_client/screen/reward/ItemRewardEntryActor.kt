package com.pl00t.swipe_client.screen.reward

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.game7th.items.InventoryItem
import com.game7th.items.ItemCategory
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.items.InventoryCellActor
import com.pl00t.swipe_client.services.profile.CollectedReward


class ItemRewardEntryActor(
    private val actorWidth: Float,
    private val reward: CollectedReward.CollectedItem,
    private val context: SwipeContext,
    private val skin: Skin,
): Group() {

    val image: InventoryCellActor
    val title: Label

    init {
//        val sbg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_blue")).apply {
//            width = actorWidth
//            height = 84f
//        }
//        addActor(sbg)

        val table = Table().apply {
            width = actorWidth
            height = 84f
        }

        image = InventoryCellActor(context, skin, 72f, InventoryItem(
            id = "",
            skin = reward.skin,
            implicit = emptyList(),
            affixes = emptyList(),
            level = reward.level,
            maxLevel = 0,
            rarity = reward.rarity,
            category = ItemCategory.GLOVES,
            equippedBy = null,
            experience = 0
        ))
        table.add(image).width(80f).height(84f).align(Align.top)


        val rightGroup = Table().apply {
            width = actorWidth - 85f
        }

        title = Label(reward.title, skin, "wave_caption").apply {
            setAlignment(Align.topLeft)
            wrap = true
        }
        rightGroup.add(title).width(actorWidth - 85f).padLeft(5f).row()

        val descriptionLabel = Label(reward.lore, skin, "text_small").apply {
            setAlignment(Align.topLeft)
            wrap = true
            width = actorWidth - 85f
        }
        rightGroup.add(descriptionLabel).width(actorWidth - 80f).padLeft(5f).row()

        table.add(rightGroup).width(actorWidth - 80f).height(84f)
        addActor(table)

        width = actorWidth
        height = 84f
    }

}
