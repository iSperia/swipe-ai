package com.pl00t.swipe_client.screen.reward

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.screen.ux.IconedButton
import com.pl00t.swipe_client.services.profile.CollectedReward
import com.pl00t.swipe_client.ux.Fonts
import ktx.actors.alpha
import ktx.actors.onClick

class RewardDialog(
    private val w: Float,
    private val h: Float,
    private val rewards: List<CollectedReward>,
    private val coreAtlas: TextureAtlas,
    private val uxAtlas: TextureAtlas,
): Group() {

    val bg: Image
    val title: Label
    val closeButton: IconedButton

    val contentWidth = w * 0.8f
    val contentPadding = (w - contentWidth) / 2f
    val titleHeight = h * 0.1f
    val entryHeight = h * 0.1f

    init {
        bg = Image(uxAtlas.createPatch("panelBg")).apply {
            width = w
            height = h
        }

        title = Fonts.createWhiteTitle("Rewards", titleHeight).apply {
            setAlignment(Align.center)
            width = w
            height = titleHeight
            y = h - titleHeight
        }

        closeButton = IconedButton(contentWidth, titleHeight, "Close", "icon_close", coreAtlas, coreAtlas, Align.right).apply {
            x = contentPadding
            y = contentPadding
        }

        addActor(bg)
        addActor(title)
        addActor(closeButton)

        closeButton.onClick {
            this@RewardDialog.addAction(Actions.sequence(
                Actions.alpha(0f, 0.4f),
                Actions.removeActor()
            ))
        }

        var nowY = h - titleHeight - entryHeight
        rewards.forEach { reward ->
            when (reward) {
                is CollectedReward.CollectedCurrencyReward -> {
                    val entryActor = CurrencyRewardEntryActor(contentWidth, entryHeight, reward, uxAtlas).apply {
                        x = contentPadding
                        y = nowY
                    }
                    addActor(entryActor)
                }
            }
            nowY -= entryHeight
        }

        alpha = 0f
        addAction(Actions.alpha(1f, 0.4f))
    }
}
