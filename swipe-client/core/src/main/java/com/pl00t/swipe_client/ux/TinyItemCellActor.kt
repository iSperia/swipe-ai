package com.pl00t.swipe_client.ux

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import ktx.actors.alpha
import ktx.actors.onExit
import ktx.actors.onTouchDown

class TinyItemCellActor(
    private val r: Resources,
    private var model: FrontItemEntryModel
) : Group() {

    private val background = r.image(Resources.ux_atlas, "gradient_item_background").apply {
        setSize(60f, 60f)
        color = r.skin().getColor("rarity_${model.rarity}")
        setOrigin(Align.center)
    }
    private val itemImage = r.image(Resources.ux_atlas, model.skin).apply {
        setSize(50f, 50f)
        setOrigin(Align.center)
        setPosition(5f, 5f)
    }
    private val itemImageShadow = r.image(Resources.ux_atlas, model.skin).apply {
        setSize(50f, 50f)
        setOrigin(Align.center)
        setScale(1.1f, 1.1f)
        setPosition(itemImage.x, itemImage.y)
        color = Color.BLACK
        alpha = 0.75f
    }

    init {
        setSize(60f, 60f)
        addActor(background)
        addActor(itemImageShadow)
        addActor(itemImage)

        onTouchDown {
            itemImage.addAction(Actions.scaleTo(0.9f, 0.9f, 0.3f))
            background.addAction(Actions.scaleTo(0.9f, 0.9f, 0.3f))
        }
        onExit {
            itemImage.addAction(Actions.scaleTo(1f, 1f, 0.3f))
            background.addAction(Actions.scaleTo(1f, 1f, 0.3f))
        }
    }

    fun reduceCount() {
        model = (model as? FrontItemEntryModel.CurrencyItemEntryModel)?.let { model ->
            model.copy(amount = model.amount - 1)
        } ?: model
    }

}
