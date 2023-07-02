package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.Router
import com.pl00t.swipe_client.screen.reward.RewardDialog
import com.pl00t.swipe_client.ux.IconedButton
import com.pl00t.swipe_client.services.battle.BattleResult
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.ScreenTitle
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class BattleFinishActor(
    private val actId: SwipeAct,
    private val locationId: String,
    private val profileService: ProfileService,
    private val result: BattleResult,
    private val context: SwipeContext,
    private val skin: Skin,
    private val router: Router
) : Group() {

    var topBlockSize = 0f
    var dialogHeight = 0f
    var iconedButtonWidth = 0f
    var iconedButtonHeight = 0f

    val resultBlockGroup = Group()
    val textureName = if (result.victory) "bg_victory" else "bg_defeat"
    val resultImage = Image(context.commonAtlas(Atlases.COMMON_BATTLE).findRegion(textureName))
    val borderImage = Image(context.commonAtlas(Atlases.COMMON_BATTLE).createPatch("panel_border"))
    val subBlockGroup = Group()
    val subBlockbackground = Image(context.commonAtlas(Atlases.COMMON_BATTLE).createPatch("panel_border_no_top_filled"))
    lateinit var caption: Actor
    lateinit var closeButton: TextButton
    lateinit var flavour: Label
    lateinit var freeRewardButton: TextButton

    init {
        topBlockSize = 400f
        val subBlockHeight = 320f
        dialogHeight = topBlockSize + subBlockHeight

        resultBlockGroup.apply {
            y = subBlockHeight
        }
        addActor(subBlockGroup)
        subBlockGroup.y = subBlockHeight
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
                Actions.moveBy(0f, -subBlockHeight, 0.3f))
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

        caption = ScreenTitle.createScreenTitle(context, skin, if (result.victory) "Victory" else "Defeat").apply {
            y = subBlockHeight - 15f
            x = 20f
        }

        val captionText = if (result.victory) "Victory shines upon the brave, as heroes forge their destiny amidst the shattered kingdoms."
        else "Defeat is but a stepping stone on the path to greatness, as the journey continues."

        flavour = Label(captionText, skin, "lore_medium").apply {
            width = 360f
            height = 220f
            wrap = true
            setAlignment(Align.topLeft)
            x = 20f
            y = 80f
        }
        subBlockGroup.addActor(flavour)

        closeButton = Buttons.createActionButton("Continue Journey", skin).apply {
            x = 100f
            y = 24f
            width = 200f
        }

        subBlockGroup.addActor(closeButton)

        resultBlockGroup.addActor(resultImage)
        resultBlockGroup.addActor(borderImage)
        addActor(caption)

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
            freeRewardButton = Buttons.createActionButton("Collect Rewards", skin)
            freeRewardButton.apply {
                x = closeButton.x
                y = closeButton.y + 40f
                width = 200f
            }
            freeRewardButton.onClick {
                KtxAsync.launch {
                    val rewards = profileService.collectFreeReward(actId, locationId)
                    //we need to show somehow
                    val rewardsDialog = RewardDialog(
                        rewards = rewards,
                        context = context,
                        skin = skin
                    ).apply {
                        x = this@BattleFinishActor.x
                        y = this@BattleFinishActor.y
                    }
                    stage.addActor(rewardsDialog)
                }
            }
            subBlockGroup.addActor(freeRewardButton)
        }
    }
}
