package com.pl00t.swipe_client.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.game.SbCharacterDisplayIntent
import com.pl00t.swipe_client.Resources

class UnitIntentActor(
    private val r: Resources,
    private val w: Float,
    private var config: SbCharacterDisplayIntent
) : Group() {

    private val iconSize = w / 2f
    private val labelSize = w - iconSize

    private val labelTicks: Label = r.labelAction(config.ticks.toString()).apply {
        x = iconSize
        y = 0f
        width = labelSize
        height = iconSize
        setAlignment(Align.left)
    }

    private var icon: Image = r.image(Resources.skills_atlas, config.skin).apply {
        setSize(iconSize, iconSize)
    }

    init {
        addActor(icon)
        addActor(labelTicks)
    }

    fun updateConfig(newConfig: SbCharacterDisplayIntent) {
        if (config.skin != newConfig.skin) {
            removeActor(icon)
            icon = r.image(Resources.skills_atlas, newConfig.skin).apply {
                setSize(iconSize, iconSize)
            }
            addActor(icon)
        }
        labelTicks.setText(newConfig.ticks.toString())
        config = newConfig
    }

}
