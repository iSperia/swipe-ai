package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.screen.ux.IconedButton
import com.pl00t.swipe_client.ux.Fonts
import ktx.actors.alpha
import ktx.actors.onClick

class LevelDetailsActor(
    locationId: String,
    locationBackground: String,
    locationName: String,
    locationDescription: String,
    waves: List<List<FrontMonsterEntryModel>>,
    width: Float,
    height: Float,
    coreAtlas: TextureAtlas,
    mapAtlas: TextureAtlas,
    uxAtlas: TextureAtlas,
    unitsAtlas: TextureAtlas,
    attackAction: (String) -> Unit
): Group() {

    val imLocation: Image
    val locationForeground: Image
    val title: Label
    val startButton: IconedButton

    val scrollRoot: Group
    val scroll: ScrollPane

    val locationDescriptionLabel: Label

    private val _titleHeight = height * 0.11f
    private val _bw = width * 0.6f
    private val _bh = height * 0.12f

    init {
        this.width = width
        this.height = height

        imLocation = Image(mapAtlas.findRegion(locationBackground)).apply {
            x = 0f
            y = 0f
            setScaling(Scaling.stretch)
            this.width = width
            this.height = height
        }
        locationForeground = Image(coreAtlas.findRegion("semi_black_pixel")).apply {
            x = 0f
            y = 0f
            this.width = width
            this.height = height
            this.alpha = 0.6f
        }

        startButton = IconedButton(_bw, _bh, "To Battle!", "button_attack", coreAtlas, uxAtlas).apply {
            x = (width - _bw) / 2f
            y = height * 0.05f
        }
        startButton.onClick { attackAction(locationId) }
        title = Fonts.createWhiteTitle(locationName, _titleHeight).apply {
            x = 0f
            y = height - _titleHeight
            this.width = width
            this.height = _titleHeight
            setAlignment(Align.center)
        }
        locationDescriptionLabel = Fonts.createCaptionAccent(locationDescription, _bh * 0.8f).apply {
            setAlignment(Align.topLeft)
            this.height = height * 0.7f
            this.width = width * 0.9f
            x = width * 0.05f
            wrap = true
        }

        scrollRoot = Group()
        val totalWaveActorsHeight = 0.6f * width * waves.size
        val totalRootHeight = totalWaveActorsHeight + locationDescriptionLabel.height

        val waveActors = waves.mapIndexed { index, wave ->
            LevelWaveActor(index + 1, wave, unitsAtlas, width * 0.9f).apply {
                x = width * 0.1f
                y = totalRootHeight - (index + 1) * 0.6f * width
            }
        }
        waveActors.forEach { scrollRoot.addActor(it) }
        scrollRoot.addActor(locationDescriptionLabel)
        scrollRoot.width = width
        scrollRoot.height = totalRootHeight
        scroll = ScrollPane(scrollRoot).apply {
            y = _bh + height * 0.1f
            this.width = width
            this.height = height - _bh - title.height - height * 0.1f
        }

        addActor(imLocation)
        addActor(locationForeground)
        addActor(title)
        addActor(startButton)
        addActor(scroll)
    }
}
