package com.pl00t.swipe_client.monster

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import ktx.actors.alpha
import ktx.actors.onExit
import ktx.actors.onTouchDown

class MonsterTinyDetailsCell(
    private val r: Resources,
    private val model: FrontMonsterEntryModel
) : Group() {

    private val background = r.image(Resources.ux_atlas, "background_transparent50").apply {
        setSize(80f, 132f)
        alpha = 0f
    }
    lateinit var unitImage: Image

    init {
        addActor(background)
        setSize(80f, 132f)
        addActor(r.image(Resources.units_atlas, model.skin).apply {
            setSize(80f, 132f)
            color = Color.BLACK
            setOrigin(Align.bottom)
            setScale(1.05f)
            alpha = 0.75f
        })
        addActor(r.image(Resources.units_atlas, model.skin).apply {
            setSize(80f, 132f)
            setOrigin(Align.bottom)
        }.also { unitImage = it })
        addActor(r.regular20Focus("${UiTexts.LvlShortPrefix.value(r.l)}${model.level}").apply {
            setSize(80f, 20f)
            setAlignment(Align.center)
        })

        onTouchDown {
            background.addAction(Actions.alpha(1f, 0.2f))
            unitImage.addAction(Actions.scaleTo(0.9f, 0.9f, 0.2f))
        }
        onExit {
            background.addAction(Actions.alpha(0f, 0.2f))
            unitImage.addAction(Actions.scaleTo(1f, 1f, 0.2f))
        }
    }

}
