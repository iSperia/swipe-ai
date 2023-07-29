package com.pl00t.swipe_client.screen.reward

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.items.CurrencyCellActor
import com.pl00t.swipe_client.services.profile.CollectedReward
import com.pl00t.swipe_client.services.profile.CurrencyMetadata


class CurrencyRewardEntryActor(
    private val actorWidth: Float,
    private val reward: CollectedReward.CountedCurrency,
    private val context: SwipeContext,
    private val skin: Skin,
): Group() {

    val image: CurrencyCellActor
    val title: Label
    val amountLabel: Label

    init {

//        val sbg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_gold")).apply {
//            width = actorWidth
//            height = 84f
//        }
//        addActor(sbg)

        image = CurrencyCellActor(context, skin, 72f, CurrencyMetadata(reward.currency, reward.description, reward.title, reward.rarity, reward.description)).apply {
            y = 6f
        }
        addActor(image)

        val rightGroup = Group().apply {
            x = 90f
            height = 84f
            width = actorWidth - 90f
        }

        title = Label(reward.title, skin, "wave_caption").apply {
            setAlignment(Align.topLeft)
            y = 60f
            height = 24f
            width = rightGroup.width
        }
        rightGroup.addActor(title)

        amountLabel = Label("x${reward.amount}", skin, "text_regular").apply {
            setAlignment(Align.topRight)
            width = rightGroup.width
            height = 24f
            y = 60f
        }
        rightGroup.addActor(amountLabel)

        val descriptionLabel = Label(reward.description, skin, "text_small").apply {
            setAlignment(Align.topLeft)
            wrap = true
            width = rightGroup.width
            height = 58f
        }
        rightGroup.addActor(descriptionLabel)

        addActor(rightGroup)

        width = actorWidth
        height = 84f
    }

    fun updateAmount(balance: Int) {
        amountLabel.setText("x$balance")
    }

}
