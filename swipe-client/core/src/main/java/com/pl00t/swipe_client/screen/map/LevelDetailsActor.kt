package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.ux.Fonts
import ktx.actors.alpha

class LevelDetailsActor(
    locationId: String,
    locationName: String,
    width: Float,
    height: Float,
    taCore: TextureAtlas,
    taMap: TextureAtlas,
): Group() {

    val imLocation: Image
    val imFg: Image
    val title: Label

    private val _titleHeight = height * 0.12f

    init {
        this.width = width
        this.height = height

        imLocation = Image(taMap.findRegion(locationId)).apply {
            x = 0f
            y = 0f
            setScaling(Scaling.fit)
            this.width = width
            this.height = height
        }
        imFg = Image(taCore.findRegion("semi_black_pixel")).apply {
            x = 0f
            y = 0f
            this.width = width
            this.height = height
            alpha = 0.85f
        }
        title = Fonts.createCaption(locationName, _titleHeight).apply {
            x = 0f
            y = height - _titleHeight
            this.width = width
            this.height = _titleHeight
            setAlignment(Align.center)
        }
        addActor(imLocation)
        addActor(imFg)
        addActor(title)
    }
}
