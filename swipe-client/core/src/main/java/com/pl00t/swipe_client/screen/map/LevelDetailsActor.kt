package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.services.battle.UnitSkin
import com.pl00t.swipe_client.ux.Fonts
import com.pl00t.swipe_client.ux.IconedButton
import ktx.actors.alpha
import ktx.actors.onClick

interface LevelDetailsCallback {
    fun processMonsterClicked(skin: UnitSkin)
}

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
): Group(), LevelWaveCallback {

    val imLocation: Image
    val locationForeground: Image
    val title: Label
    val startButton: IconedButton

    val scrollRoot: Group
    val scroll: ScrollPane

    val locationDescriptionLabel: Label

    private val _titleHeight = width * 0.1f
    private val _bw = width * 0.5f
    private val _bh = width * 0.12f

    var callback: LevelDetailsCallback? = null

    init {
        this.width = width
        this.height = height

        imLocation = Image(mapAtlas.findRegion(locationBackground)).apply {
            x = 0f
            y = 0f
            setScaling(Scaling.fill)
            this.width = width
            this.height = height
        }
        locationForeground = Image(coreAtlas.findRegion("semi_black_pixel")).apply {
            x = 0f
            y = 0f
            this.width = width
            this.height = height
            this.alpha = 0.7f
        }

        startButton = IconedButton(_bw, _bh, "To Battle!", "button_attack", coreAtlas, uxAtlas).apply {
            x = width * 0.45f
            y = width * 0.05f
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
            this.height = width * 0.7f
            this.width = width * 0.9f
            x = width * 0.05f
            wrap = true
        }

        scrollRoot = Group()
        val totalWaveActorsHeight = 0.6f * width * waves.size
        val totalRootHeight = totalWaveActorsHeight + locationDescriptionLabel.height

        val waveActors = waves.mapIndexed { index, wave ->
            LevelWaveActor(index + 1, wave, unitsAtlas, width * 0.9f).apply {
                x = width * 0.05f
                y = totalRootHeight - (index + 1) * 0.6f * width
            }.apply {
                callback = this@LevelDetailsActor
            }
        }
        waveActors.forEach { scrollRoot.addActor(it) }
        scrollRoot.addActor(locationDescriptionLabel)
        scrollRoot.width = width
        scrollRoot.height = totalRootHeight
        scroll = ScrollPane(scrollRoot).apply {
            y = _bh + width * 0.1f
            this.width = width
            this.height = height - _bh - title.height - height * 0.1f
        }

        addActor(imLocation)
        addActor(locationForeground)
        addActor(title)
        addActor(startButton)
        addActor(scroll)
    }

    override fun processMonsterClicked(skin: UnitSkin) {
        callback?.processMonsterClicked(skin)
    }
}
