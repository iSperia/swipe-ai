package com.pl00t.swipe_client.ux

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext

object LabelFactory {

    fun createScreenTitle(
        context: SwipeContext,
        skin: Skin,
        text: String
    ): Group {
        return Group().apply {
            this.width = 360f
            this.height = 60f

            val background = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("title_background")).apply {
                name = "background"
                this.width = 350f
                this.height = 50f
                setScaling(Scaling.stretch)
                x = 5f
                y = 5f
            }

            val label = Label(text, skin.get("window_title", LabelStyle::class.java)).apply {
                name = "label"
                this.width = 350f
                this.height = 50f
                setAlignment(Align.center)
                x = 5f
                y = 5f
                this.wrap = true
                setFontScale(0.66f)
            }

            addActor(background)
            addActor(label)
        }
    }
}
