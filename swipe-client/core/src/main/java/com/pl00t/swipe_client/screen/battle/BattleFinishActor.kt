package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextArea
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.screen.Router
import com.pl00t.swipe_client.screen.ux.IconedButton
import com.pl00t.swipe_client.services.battle.BattleResult
import com.pl00t.swipe_client.ux.Fonts
import ktx.actors.alpha
import ktx.actors.onClick

class BattleFinishActor(
    private val result: BattleResult,
    private val coreTextureAtlas: TextureAtlas,
    private val battleTextureAtlas: TextureAtlas,
    private val router: Router
) : Group() {

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


    override fun setStage(stage: Stage) {
        super.setStage(stage)
        val _w = stage.width * 0.8f
        val _sbh = _w * 0.5f
        val _h = _w + _sbh
        subBlockGroup.apply {
            x = (stage.width - _w) / 2f
            y = (stage.height - _h) / 2f + _sbh + 1f
        }
        resultBlockGroup.apply {
            x = (stage.width - _w) / 2f
            y = (stage.height - _h) / 2f + _sbh
        }
        addActor(subBlockGroup)
        addActor(resultBlockGroup)
        subBlockbackground.apply {
            width = _w
            height = _sbh
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
            width = _w
            height = _w
        }
        borderImage.apply {
            width = _w
            height = _w
            x = resultImage.x
            y = resultImage.y
        }

        val _ch = _w / 4f
        captionGradient.apply {
            width = _w
            height = _ch * 1.5f
            scaleY = -1f
            x = borderImage.x
            y = borderImage.y + this.height
        }

        caption = Fonts.createWhiteTitle(if (result.victory) "Victory" else "Defeat", _ch)
        caption.apply {
            x = resultImage.x
            y = resultImage.y
            width = _w
            height = _ch
            setAlignment(Align.center)
        }

        val captionText = if (result.victory) "Victory shines upon the brave, as heroes forge their destiny amidst the shattered kingdoms."
        else "Defeat is but a stepping stone on the path to greatness, as the journey continues."

        flavour = Fonts.createCaptionAccent(captionText, _w * 0.12f).apply {
            width = _w * 0.8f
            height = _sbh - _w * 0.15f - _sbh * 0.3f
            wrap = true
            setAlignment(Align.topLeft)
            x = _w * 0.1f
            y = _sbh * 0.25f + _w * 0.15f
        }
        subBlockGroup.addActor(flavour)

        closeButton = IconedButton(_w * 0.8f, _w * 0.15f, "Continue Journey", "button_map", coreTextureAtlas, battleTextureAtlas)
        closeButton.apply {
            x = _w * 0.1f
            y = _sbh * 0.2f
        }
        subBlockGroup.addActor(closeButton)

        resultBlockGroup.addActor(resultImage)
        resultBlockGroup.addActor(captionGradient)
        resultBlockGroup.addActor(borderImage)
        resultBlockGroup.addActor(caption)

        closeButton.onClick {
            router.navigateMap("act1")
        }
    }
}
