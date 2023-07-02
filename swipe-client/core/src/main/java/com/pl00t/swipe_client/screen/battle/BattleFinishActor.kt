package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.screen.Router
import com.pl00t.swipe_client.screen.reward.RewardDialog
import com.pl00t.swipe_client.ux.IconedButton
import com.pl00t.swipe_client.services.battle.BattleResult
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class BattleFinishActor(
    private val actId: SwipeAct,
    private val locationId: String,
    private val profileService: ProfileService,
    private val result: BattleResult,
    private val coreTextureAtlas: TextureAtlas,
    private val battleTextureAtlas: TextureAtlas,
    private val uiTextureAtlas: TextureAtlas,
    private val router: Router
) : Group() {

    var topBlockSize = 0f
    var dialogHeight = 0f
    var iconedButtonWidth = 0f
    var iconedButtonHeight = 0f

    val resultBlockGroup = Group()
    val textureName = if (result.victory) "bg_victory" else "bg_defeat"
    val resultImage = Image(battleTextureAtlas.findRegion(textureName))
    val borderImage = Image(battleTextureAtlas.createPatch("panel_border"))
    val subBlockGroup = Group()
    val subBlockbackground = Image(battleTextureAtlas.createPatch("panel_border_no_top_filled"))
    val captionGradient = Image(coreTextureAtlas.findRegion("top_gradient"))
    lateinit var caption: Label
    lateinit var closeButton: IconedButton
    lateinit var flavour: Label
    lateinit var freeRewardButton: IconedButton

    override fun setStage(stage: Stage) {
        super.setStage(stage)
        topBlockSize = stage.width * 0.8f
        val subBlockHeight = topBlockSize * 0.8f
        dialogHeight = topBlockSize + subBlockHeight
        iconedButtonWidth = topBlockSize * 0.8f
        iconedButtonHeight = topBlockSize * 0.15f
        subBlockGroup.apply {
            x = (stage.width - topBlockSize) / 2f
            y = (stage.height - dialogHeight) / 2f + subBlockHeight + 1f
        }
        resultBlockGroup.apply {
            x = (stage.width - topBlockSize) / 2f
            y = (stage.height - dialogHeight) / 2f + subBlockHeight
        }
        addActor(subBlockGroup)
        addActor(resultBlockGroup)
        subBlockbackground.apply {
            width = topBlockSize
            height = subBlockHeight
            setScaling(Scaling.stretch)
        }
        subBlockGroup.addActor(subBlockbackground)
        subBlockGroup.addAction(
            Actions.sequence(
                Actions.delay(0.4f),
                Actions.moveBy(0f, -subBlockbackground.height, 0.3f))
            )
        resultBlockGroup.setOrigin(Align.center)
        resultBlockGroup.setScale(2f)
        resultBlockGroup.alpha = 0f
        resultBlockGroup.addAction(Actions.parallel(
            Actions.alpha(1f, 0.4f, SwingOut(1.5f)),
            Actions.scaleTo(1f, 1f, 0.4f, SwingOut(1.5f))
        ))
        resultImage.apply {
            width = topBlockSize
            height = topBlockSize
        }
        borderImage.apply {
            width = topBlockSize
            height = topBlockSize
            x = resultImage.x
            y = resultImage.y
        }

        val _ch = topBlockSize / 4f
        captionGradient.apply {
            width = topBlockSize
            height = _ch
            scaleY = -1f
            x = borderImage.x
            y = borderImage.y + this.height
        }

//        caption = Fonts.createWhiteTitle(if (result.victory) "Victory" else "Defeat", _ch)
//        caption.apply {
//            x = resultImage.x
//            y = resultImage.y
//            width = topBlockSize
//            height = _ch
//            setAlignment(Align.center)
//        }

        val captionText = if (result.victory) "Victory shines upon the brave, as heroes forge their destiny amidst the shattered kingdoms."
        else "Defeat is but a stepping stone on the path to greatness, as the journey continues."

//        flavour = Fonts.createCaptionAccent(captionText, topBlockSize * 0.11f).apply {
//            width = topBlockSize * 0.8f
//            height = subBlockHeight - topBlockSize * 0.15f - subBlockHeight * 0.3f
//            wrap = true
//            setAlignment(Align.topLeft)
//            x = topBlockSize * 0.1f
//            y = subBlockHeight * 0.25f + topBlockSize * 0.15f
//        }
//        subBlockGroup.addActor(flavour)

        closeButton = IconedButton(iconedButtonWidth, iconedButtonHeight, "Continue Journey", "button_map", coreTextureAtlas, battleTextureAtlas)
        closeButton.apply {
            x = topBlockSize * 0.1f
            y = subBlockHeight * 0.15f
        }
        subBlockGroup.addActor(closeButton)

        resultBlockGroup.addActor(resultImage)
        resultBlockGroup.addActor(captionGradient)
        resultBlockGroup.addActor(borderImage)
        resultBlockGroup.addActor(caption)

        KtxAsync.launch {
            checkFreeRewardAvailable()
        }

        closeButton.onClick {
            KtxAsync.launch {
                router.navigateMap(SwipeAct.ACT_1)
            }
        }
    }

    private suspend fun checkFreeRewardAvailable() {
        if (profileService.isFreeRewardAvailable(actId, locationId)) {
            freeRewardButton = IconedButton(iconedButtonWidth, iconedButtonHeight, "Collect Rewards", "icon_free_reward", coreTextureAtlas, battleTextureAtlas, Align.right)
            freeRewardButton.apply {
                x = closeButton.x
                y = closeButton.y + iconedButtonHeight * 1.05f
            }
            freeRewardButton.onClick {
                KtxAsync.launch {
                    val rewards = profileService.collectFreeReward(actId, locationId)
                    //we need to show somehow
                    val rewardsDialog = RewardDialog(
                        w = topBlockSize,
                        h = dialogHeight,
                        rewards = rewards,
                        coreAtlas = this@BattleFinishActor.coreTextureAtlas,
                        uxAtlas = uiTextureAtlas,
                    ).apply {
                        x = subBlockGroup.x
                        y = subBlockGroup.y
                    }
                    stage.addActor(rewardsDialog)
                }
            }
            subBlockGroup.addActor(freeRewardButton)
        }
    }
}
