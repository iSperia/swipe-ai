package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.items.ItemCategory
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync

class HeroAttributesEquipmentActor(
    private val context: SwipeContext,
    private val skin: Skin,
    private var character: SwipeCharacter,
    private val router : MapScreenRouter,
    private val onItemClicked: (ItemCategory, String?) -> Unit
) : Group() {

    init {
        KtxAsync.launch {
            loadData()
        }
        width = 480f
        height = 240f
    }

    private suspend fun loadData() {

        val g1 = createEquipmentGroup(listOf(ItemCategory.BELT, ItemCategory.AMULET, ItemCategory.RING)).apply {
            x = 0f
        }
        val monsterImage = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion(character.skin)).apply {
            height = 240f
            width = 160f
            x = 60f
            setScaling(Scaling.stretch)
        }
        val g2 = createEquipmentGroup(listOf(ItemCategory.BOOTS, ItemCategory.GLOVES, ItemCategory.HELMET)).apply {
            x = 200f
        }

        addActor(monsterImage)
        addActor(g1)
        addActor(g2)

        val table = Table().apply {
            this.x = g2.x + 90f
            this.height = 240f
            this.width = 120f
        }

        inflateAttribute(table, "Body", character.attributes.body.toString(), "ic_body")
        table.row()

        inflateAttribute(table, "Health", "1350", "ic_health")
        table.row()

        inflateAttribute(table, "Spirit", character.attributes.spirit.toString(), "ic_spirit")
        table.row()

        inflateAttribute(table, "Luck Chance", "5%", "ic_luck")
        table.row()

        inflateAttribute(table, "Mind", character.attributes.mind.toString(), "ic_mind")
        table.row()

        inflateAttribute(table, "Ult. progress", "+7.5%", "ic_wisdom")
        table.row()

        addActor(table)
    }

    private fun inflateAttribute(table: Table, label: String, value: String, icon: String) {
        table.add(Label(label, skin, "damage_popup").apply {
            width = 120f
            height = 20f
            setAlignment(Align.left)
        }).colspan(2).height(20f).width(120f).align(Align.left).row()
        table.add(Image(context.commonAtlas(Atlases.COMMON_UX).findRegion(icon)).apply {
            width = 20f
            height = 20f
        }).size(20f)
        table.add(Label(value, skin, "lore_medium").apply {
            width = 90f
            height = 20f
            setAlignment(Align.left)
        }).width(100f).height(20f).padLeft(5f).align(Align.left)
    }

    private suspend fun createEquipmentGroup(categories: List<ItemCategory>): Group {
        val group = Group().apply {
            width = 80f
            height = categories.size * 80f
        }

        categories.forEachIndexed { index, itemCategory ->
            val item = context.profileService().getItems().firstOrNull { it.category == itemCategory && it.equippedBy == character.skin }
            val bg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_rarity", item?.rarity ?: 0)).apply {
                width = 76f
                height = 76f
                x = 2f
                y = index * 80f + 2f
            }
            group.addActor(bg)

            bg.onClick { this@HeroAttributesEquipmentActor.onItemClicked(itemCategory, item?.id) }

            item?.let { item ->

                val actor = InventoryCellActor(context, skin, 76f, item)
                actor.x = 2f
                actor.y = index * 80f + 2f
                actor.touchable = Touchable.disabled
                group.addActor(actor)
            }
        }


        return group
    }
}
