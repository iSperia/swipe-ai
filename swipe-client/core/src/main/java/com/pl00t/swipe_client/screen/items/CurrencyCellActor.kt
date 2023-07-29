package com.pl00t.swipe_client.screen.items

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.profile.CurrencyMetadata
import ktx.actors.alpha
import ktx.actors.repeatForever

class CurrencyCellActor(
    private val context: SwipeContext,
    private val skin: Skin,
    private val size: Float,
    private val currency: CurrencyMetadata,
) : Group() {

    private val itemImage: Image

    init {
        val rarityImage = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_rarity", currency.rarity)).apply {
            width = size - 2f
            height = size - 2f
            x = 1f
            y = 1f
        }
        itemImage = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion(currency.currency.toString())).apply {
            width = size
            height = size
            setOrigin(Align.center)
        }
        addActor(rarityImage)
        addActor(itemImage)

        val starSize = size / 5f
        val statPadding = (size - starSize * currency.rarity) / 2f
        (0 until currency.rarity).forEach { i ->
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
    }

    fun setFocused(focused: Boolean) {
        if (focused) {
            itemImage.setScale(0.95f)
            itemImage.addAction(Actions.sequence(
                Actions.scaleTo(1.05f, 1.05f, 1f),
                Actions.scaleTo(0.95f, 0.95f, 1f)
            ).repeatForever())
        } else {
            itemImage.clearActions()
            itemImage.setScale(1f)
        }
    }

}
