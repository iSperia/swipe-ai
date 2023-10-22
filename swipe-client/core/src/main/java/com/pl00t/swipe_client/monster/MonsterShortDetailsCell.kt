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
import kotlin.random.Random

class MonsterShortDetailsCell(
    private val r: Resources,
    private val model: FrontMonsterEntryModel
) : Group() {

    private val background = r.image(Resources.ux_atlas, "background_transparent50").apply {
        setSize(150f, 310f)
        alpha = 0f
    }
    lateinit var unitImage: Image

    init {
        addActor(background)
        setSize(150f, 310f)
        val rarity = model.rarity
        addActor(r.image(Resources.units_atlas, model.skin).apply {
            setSize(150f, 250f)
            setPosition(0f, 60f)
            color = Color.BLACK
            setOrigin(Align.center)
            setScale(1.05f)
            alpha = 0.75f
        })
        addActor(r.image(Resources.units_atlas, model.skin).apply {
            setSize(150f, 250f)
            setPosition(0f, 60f)
            setOrigin(Align.bottom)
        }.also { unitImage = it })
        addActor(r.image(Resources.ux_atlas, "texture_row").apply {
            setSize(148f, 68f)
            setPosition(1f, 1f)
            color = r.skin().getColor("rarity_${rarity}")
            alpha = 0.5f
        })
        addActor(r.regular20Focus(model.name.value(r.l)).apply {
            setSize(150f, 35f)
            setAlignment(Align.center)
            wrap = true
            setPosition(0f, 30f)
        })
        addActor(r.regular20White("${UiTexts.LvlPrefix.value(r.l)}${model.level}").apply {
            setSize(150f, 30f)
            setAlignment(Align.center)
            wrap = true
        })
        addActor(MonsterRankActor(r, rarity).apply {
            setSize(50f, 60f)
            setPosition(0f, 60f)
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
