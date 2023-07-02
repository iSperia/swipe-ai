package com.pl00t.swipe_client.screen.reward

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.profile.CollectedReward


class CurrencyRewardEntryActor(
    private val reward: CollectedReward.CollectedCurrencyReward,
    private val context: SwipeContext,
    private val skin: Skin,
): Group() {

    val image: Image
    val title: Label
    val amountLabel: Label

    init {
        width = 360f
        height = 72f

        image = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion(reward.currency.toString())).apply {
            width = 72f
            height = 72f
        }
        val table = Table().apply {
            x = 80f
            y = 6f
            height = 60f
            width = 270f
        }

        title = Label(reward.title, skin, "wave_caption").apply {
            setAlignment(Align.left)
        }
        table.add(title).width(270f).left()
        table.row()
        amountLabel = Label("x${reward.amount}", skin, "text_regular").apply {
            setAlignment(Align.left)
        }
        table.add(amountLabel).width(270f).left()
        table.row()

        addActor(image)
        addActor(table)
    }
}
