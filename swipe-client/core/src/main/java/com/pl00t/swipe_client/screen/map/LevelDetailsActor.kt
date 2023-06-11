package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.ux.Fonts
import ktx.actors.alpha
import ktx.actors.onClick

class LevelDetailsActor(
    locationId: String,
    locationName: String,
    width: Float,
    height: Float,
    taCore: TextureAtlas,
    taMap: TextureAtlas,
    attackAction: (String) -> Unit
): Group() {

    val imLocation: Image
    val locationForeground: Image
    val title: Label
    val startButton: Button
    val startLabel: Label
    val startIcon: Image

    private val _titleHeight = height * 0.12f
    private val _bw = width * 0.3f
    private val _bh = height * 0.12f

    init {
        this.width = width
        this.height = height

        imLocation = Image(taMap.findRegion(locationId)).apply {
            x = 0f
            y = 0f
            setScaling(Scaling.stretch)
            this.width = width
            this.height = height
        }
        locationForeground = Image(taCore.findRegion("semi_black_pixel")).apply {
            x = 0f
            y = 0f
            this.width = width
            this.height = height
            this.alpha = 0.6f
        }
        startButton = Button(TextureRegionDrawable(taCore.findRegion("button_bg")), null).apply {
            setWidth(_bw)
            setHeight(_bh)
            x = width - _bw - height * 0.03f
            y = height * 0.03f
        }
        startButton.onClick { attackAction(locationId) }
        startLabel = Fonts.createWhiteCaption("Attack", startButton.height).apply {
            x = startButton.x + _bh
            y = startButton.y
            this.width = _bw - _bh
            this.height = startButton.height
            setAlignment(Align.left)
            touchable = Touchable.disabled
        }
        startIcon = Image(taMap.findRegion("button_attack")).apply {
            this.width = _bh
            this.height = _bh
            x = startButton.x
            y = startButton.y
        }
        title = Fonts.createWhiteTitle(locationName, _titleHeight).apply {
            x = 0f
            y = height - _titleHeight
            this.width = width
            this.height = _titleHeight
            setAlignment(Align.center)
        }
        addActor(imLocation)
        addActor(locationForeground)
        addActor(title)
        addActor(startButton)
        addActor(startLabel)
        addActor(startIcon)
    }
}
