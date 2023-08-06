package com.pl00t.swipe_client.ux

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import ktx.actors.alpha
import ktx.actors.onExit
import ktx.actors.onTouchDown

class ItemCellActor(
    private val r: R,
    private var model: FrontItemEntryModel
) : Group() {

    private val background = r.image(R.ux_atlas, "gradient_item_background").apply {
        setSize(110f, 110f)
        color = r.skin().getColor("rarity_${model.rarity}")
        setOrigin(Align.center)
        y = 40f
        x = 5f
    }
    private val name = r.regularWhite(model.name.value(r.l)).apply {
        setSize(110f, 40f)
        setAlignment(Align.center)
        wrap = true
        x = 5f
    }
    private val itemImage = r.image(R.ux_atlas, model.skin).apply {
        setSize(100f, 100f)
        setOrigin(Align.center)
        y = 45f
        x = 10f
    }
    private val itemImageShadow = r.image(R.ux_atlas, model.skin).apply {
        setSize(100f, 100f)
        setOrigin(Align.center)
        setScale(1.1f, 1.1f)
        setPosition(itemImage.x, itemImage.y)
        color = Color.BLACK
        alpha = 0.75f
    }
    private val starGroup = Group().apply {
        x = 10f
        y = 140f
        setSize(20f * model.rarity, 20f)
        setOrigin(Align.center)
    }
    private val amountLabel = r.regularWhite(model.getText(r)).apply {
        setAlignment(Align.left)
        setSize(140f, 16f)
        x = itemImage.x
        y = itemImage.y
    }
    private val amountLabelShadow = r.regularWhite(model.getText(r)).apply {
        setAlignment(Align.left)
        setSize(140f, 16f)
        setPosition(amountLabel.x - 1f, amountLabel.y - 1f)
        isVisible = model.amount > 0
        color = Color.BLACK
    }

    init {
        setSize(120f, 160f)
        addActor(background)
        addActor(itemImageShadow)
        addActor(itemImage)
        addActor(starGroup)
        addActor(name)

        addActor(amountLabelShadow)
        addActor(amountLabel)


        val padding = 50f - (model.rarity + 1) * 10f
        (0..model.rarity).forEach { index ->
            val star = r.image(R.ux_atlas, "star").apply {
                setPosition(padding + index * 20f, 0f)
                setSize(20f, 20f)
                color = r.skin().getColor("focus_color")
                alpha = 0.8f
            }
            starGroup.addActor(star)
        }

        onTouchDown {
            starGroup.addAction(Actions.moveTo(10f, 130f, 0.3f))
            name.addAction(Actions.moveTo(5f, 10f, 0.3f))
            amountLabel.addAction(Actions.moveTo(14f, 55f, 0.3f))
            amountLabelShadow.addAction(Actions.moveTo(13f, 54f, 0.3f))
            itemImage.addAction(Actions.scaleTo(0.9f, 0.9f, 0.3f))
            background.addAction(Actions.scaleTo(0.9f, 0.9f, 0.3f))
        }
        onExit {
            starGroup.addAction(Actions.moveTo(10f, 140f, 0.3f))
            name.addAction(Actions.moveTo(5f, 0f, 0.3f))
            amountLabel.addAction(Actions.moveTo(10f, 45f, 0.3f))
            amountLabelShadow.addAction(Actions.moveTo(9f, 44f, 0.3f))
            itemImage.addAction(Actions.scaleTo(1f, 1f, 0.3f))
            background.addAction(Actions.scaleTo(1f, 1f, 0.3f))
        }
    }

    fun reduceCount() {
        model = model.copy(amount = model.amount - 1)
        amountLabel.setText(model.getText(r))
        amountLabelShadow.setText(model.getText(r))
    }
}
