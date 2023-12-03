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
        setSize(480f, 184f)

        val cell = ItemCellActor(r, model).apply {
            setPosition(10f, 20f)
            if (onItemClick != null) {
                (model as? FrontItemEntryModel.InventoryItemEntryModel)?.let { model ->
                    onClick { onItemClick.invoke(model.item.id) }
                }
            } else {
                touchable = Touchable.disabled
            }
        }

        addActor(r.image(Resources.ux_atlas, "texture_row").apply {
            setSize(480f, 184f)
            color = r.skin().getColor("rarity_${model.rarity}")
            alpha = 0.5f
        })
        addActor(r.image(Resources.ux_atlas, "background_black").apply {
            setPosition(1f, 1f)
            setSize(478f, 182f)
            alpha = 0.5f
        })
        addActor(cell)

        action?.let {
            it.setPosition(400f,25f)
            it.setSize(80f, 110f)
            addActor(it)
        }

        KtxAsync.launch {
            (model as? FrontItemEntryModel.InventoryItemEntryModel)?.let { model ->
                model.item?.let { item ->
                    val template = r.itemService.getAffix(item.implicit.affix)!!
                    val implicit = r.regularMain(template.description.value(r.l).replace("$", item.implicit.affix.pattern.format(item.implicit.value))).apply {
                        setSize(265f, 32f)
                        setAlignment(Align.left)
                        setPosition(130f, 160f - 32f)
                        wrap = true
                    }
                    val shadow = r.image(Resources.ux_atlas, "background_black").apply {
                        setSize(implicit.width, 32f * 5)
                        setPosition(130f, 1f)
                        alpha = 0.7f
                    }

                    addActor(shadow)
                    addActor(implicit)

                    val name = r.regular24White(model.name.value(r.l)).apply {
                        setPosition(0f, 160f)
                        setSize(r.width, 24f)
                        setAlignment(Align.center)
                    }
                    addActor(name)

                    item.affixes.forEachIndexed { i, itemAffix ->
                        val template = r.itemService.getAffix(itemAffix.affix)!!
                        val affix = r.regularWhite(template.description.value(r.l).replace("$", itemAffix.affix.pattern.format(itemAffix.value))).apply {
                            setSize(260f, 32f)
                            setAlignment(Align.left)
                            setPosition(132f, 160f - 64f - 32f * i)
                        }
                        val line = r.image(Resources.ux_atlas, if (i == 0) "background_main" else "background_white").apply {
                            width = affix.width - 10f
                            height = 1f
                            setPosition(affix.x + 3f, affix.y + affix.height)
                            alpha = 0.1f
                        }
                        addActor(affix)
                        addActor(line)
                    }
                }
            }
            (model as? FrontItemEntryModel.CurrencyItemEntryModel)?.let { model ->
                model.currency?.let { c ->
                    val meta = r.profileService.getCurrency(c)
                    val shadow = r.image(Resources.ux_atlas, "background_black").apply {
                        setSize(265f, 32f * 5)
                        setPosition(130f, 1f)
                        alpha = 0.7f
                    }
                    addActor(shadow)
                    val name = r.regular24White(meta.name.value(r.l)).apply {
                        setPosition(0f, 160f)
                        setSize(r.width, 24f)
                        setAlignment(Align.center)
                    }

                    val description = r.regular20White(meta.description.value(r.l)).apply {
                        setPosition(132f, 2f)
                        setSize(260f, 31f * 5)
                        setAlignment(Align.topLeft)
                        wrap = true
                    }
                    addActor(name)
                    addActor(description)
                }
            }
        }
    }

}
