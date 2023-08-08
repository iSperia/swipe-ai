package com.pl00t.swipe_client.ux

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class ItemRowActor(
    private val r: Resources,
    private var model: FrontItemEntryModel,
    private val action: ActionCompositeButton?,
    private val onItemClick: ((String) -> Unit)?
) : Group() {

    init {
        setSize(480f, 160f)

        val cell = ItemCellActor(r, model).apply {
            setPosition(10f, 0f)
            if (onItemClick != null) {
                onClick { onItemClick.invoke(model.item!!.id) }
            } else {
                touchable = Touchable.disabled
            }
        }

        addActor(r.image(Resources.ux_atlas, "texture_row").apply {
            setSize(480f, 160f)
            color = r.skin().getColor("rarity_${model.rarity}")
            alpha = 0.5f
        })
        addActor(r.image(Resources.ux_atlas, "background_black").apply {
            setPosition(1f, 1f)
            setSize(478f, 158f)
            alpha = 0.5f
        })
        addActor(cell)

        action?.let {
            it.setPosition(400f,25f)
            it.setSize(80f, 110f)
            addActor(it)
        }

        KtxAsync.launch {
            model.item?.let { item ->
                val template = r.itemService.getAffix(item.implicit.affix)!!
                val implicit = r.regularMain(template.description.value(r.l).replace("$", item.implicit.affix.pattern.format(item.implicit.value))).apply {
                    setSize(265f, 32f)
                    setAlignment(Align.left)
                    setPosition(130f, 160f - 32f)
                    wrap = true
                }
                val shadow = r.image(Resources.ux_atlas, "background_black").apply {
                    setSize(implicit.width - 2f, implicit.height - 2f)
                    setPosition(implicit.x + 1f, implicit.y + 1f)
                    alpha = 0.7f
                }
                addActor(shadow)
                addActor(implicit)

                item.affixes.forEachIndexed { i, itemAffix ->
                    val template = r.itemService.getAffix(itemAffix.affix)!!
                    val affix = r.regularWhite(template.description.value(r.l).replace("$", itemAffix.affix.pattern.format(itemAffix.value))).apply {
                        setSize(265f, 32f)
                        setAlignment(Align.left)
                        setPosition(130f, 160f - 64f - 32f * i)
                    }
                    val shadow = r.image(Resources.ux_atlas, "background_black").apply {
                        setSize(affix.width - 2f, affix.height - 2f)
                        setPosition(affix.x + 1f, affix.y + 1f)
                        alpha = 0.7f
                    }
                    addActor(shadow)
                    addActor(affix)
                }
            }

        }
    }

}
