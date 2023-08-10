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

class ItemCellActor(
    private val r: Resources,
    private var model: FrontItemEntryModel
) : Group() {

    private val background = r.image(Resources.ux_atlas, "gradient_item_background").apply {
        setSize(110f, 130f)
        color = r.skin().getColor("rarity_${model.rarity}")
        setOrigin(Align.center)
        setPosition(5f, 5f)
    }
    private val itemImage = r.image(Resources.ux_atlas, model.skin).apply {
        setSize(100f, 100f)
        setOrigin(Align.center)
        setPosition(10f, 30f)
    }
    private val itemImageShadow = r.image(Resources.ux_atlas, model.skin).apply {
        setSize(100f, 100f)
        setOrigin(Align.center)
        setScale(1.1f, 1.1f)
        setPosition(itemImage.x, itemImage.y)
        color = Color.BLACK
        alpha = 0.75f
    }
    private val starGroup = Group().apply {
        setPosition(24f, 118f)
        setSize(12f * model.rarity, 12f)
        setOrigin(Align.center)
    }
    private val amountBackground = r.image(Resources.ux_atlas, "background_black").apply {
        setSize(110f, 20f)
        setPosition(5f, 5f)
        alpha = 0.5f
    }
    private val amountLabel = r.regularWhite(model.getText(r)).apply {
        setAlignment(Align.center)
        setSize(110f, 20f)
        setPosition(amountBackground.x, amountBackground.y)
    }

    init {
        setSize(120f, 140f)
        addActor(background)
        addActor(amountBackground)
        addActor(itemImageShadow)
        addActor(itemImage)
        addActor(starGroup)

        addActor(amountLabel)


        val padding = (72f - (model.rarity + 1) * 12f)/2f
        (0..model.rarity).forEach { index ->
            val star = r.image(Resources.ux_atlas, "star").apply {
                setPosition(padding + index * 12f, 0f)
                setSize(12f, 12f)
                color = r.skin().getColor("focus_color")
            }
            starGroup.addActor(star)
        }

        onTouchDown {
            starGroup.addAction(Actions.moveTo(24f, 110f, 0.3f))
            amountLabel.addAction(Actions.moveTo(5f, 13f, 0.3f))
            itemImage.addAction(Actions.scaleTo(0.9f, 0.9f, 0.3f))
            background.addAction(Actions.scaleTo(0.9f, 0.9f, 0.3f))
            amountBackground.addAction(Actions.moveTo(5f, 13f, 0.3f))
        }
        onExit {
            starGroup.addAction(Actions.moveTo(24f, 118f, 0.3f))
            amountLabel.addAction(Actions.moveTo(5f, 5f, 0.3f))
            itemImage.addAction(Actions.scaleTo(1f, 1f, 0.3f))
            background.addAction(Actions.scaleTo(1f, 1f, 0.3f))
            amountBackground.addAction(Actions.moveTo(5f, 5f, 0.3f))
        }
    }

    fun reduceCount() {
        model = model.copy(amount = model.amount - 1)
        amountLabel.setText(model.getText(r))
    }
}
