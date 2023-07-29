package com.pl00t.swipe_client.screen.reward

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.profile.CollectedReward
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.ScreenTitle
import ktx.actors.alpha
import ktx.actors.onClick

class RewardDialog(
    private val rewards: List<CollectedReward>,
    private val context: SwipeContext,
    private val skin: Skin
): Group() {

    val bg: Image
    val title: Actor
    val closeButton: TextButton

    val scroll: ScrollPane

    init {
        bg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_dark_blue")).apply {
            width = 400f
            height = 720f
            setScaling(Scaling.stretch)
        }

        title = ScreenTitle.createScreenTitle(context, skin, "Rewards").apply {
            x = 20f
            y = 690f
        }

        closeButton = Buttons.createActionButton("Close", skin).apply {
            x = 100f
            width = 200f
            y = 10f
        }



        closeButton.onClick {
            this@RewardDialog.addAction(Actions.sequence(
                Actions.alpha(0f, 0.4f),
                Actions.removeActor()
            ))
        }

        val rewardTable = Table()
        scroll = ScrollPane(rewardTable).apply {
            x = 20f
            y = 50f
            width = 360f
            height = 640f
        }

        rewards.forEach {  reward ->
            when (reward) {
                is CollectedReward.CountedCurrency -> {
                    val entryActor = CurrencyRewardEntryActor(scroll.width - 10f, reward, context, skin)
                    rewardTable.add(entryActor).padBottom(5f).top()
                    rewardTable.row()
                }

                is CollectedReward.CollectedItem -> {
                    val entryActor = ItemRewardEntryActor(reward, context, skin)
                    rewardTable.add(entryActor).padBottom(5f).top()
                    rewardTable.row()
                }
            }
        }



        addActor(bg)
        addActor(closeButton)
        addActor(scroll)
        addActor(title)

        alpha = 0f
        addAction(Actions.alpha(1f, 0.4f))
    }
}
