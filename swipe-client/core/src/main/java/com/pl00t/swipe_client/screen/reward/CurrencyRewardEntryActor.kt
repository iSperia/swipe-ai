package com.pl00t.swipe_client.screen.reward

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.services.profile.CollectedReward
import com.pl00t.swipe_client.ux.Fonts


class CurrencyRewardEntryActor(
    private val w: Float,
    private val h: Float,
    private val reward: CollectedReward.CollectedCurrencyReward,
    private val uxAtlas: TextureAtlas,
): Group() {

    val image: Image
    val title: Label
    val amountLabel: Label

    val titleHeight = h * 0.6f
    val amountHeight = h - titleHeight
    val padding = h * 0.05f

    init {
        image = Image(uxAtlas.findRegion(reward.currency.toString())).apply {
            width = h
            height = h
        }
        title = Fonts.createWhiteTitle(reward.title, titleHeight).apply {
            x = h + padding
            y = amountHeight
            setAlignment(Align.left)
        }
        amountLabel = Fonts.createCaptionAccent("x${reward.amount}", amountHeight).apply {
            x = h + padding
            setAlignment(Align.left)
        }

        addActor(image)
        addActor(title)
        addActor(amountLabel)
    }
}
