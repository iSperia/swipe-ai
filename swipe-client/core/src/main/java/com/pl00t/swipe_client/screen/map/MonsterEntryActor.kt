package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.services.battle.UnitSkin
import com.pl00t.swipe_client.ux.Fonts

data class FrontMonsterEntryModel(
    val skin: UnitSkin,
    val name: String,
    val level: Int,
)

class MonsterEntryActor(
    val entry: FrontMonsterEntryModel,
    private val w: Float,
    private val h: Float,
    private val unitsAtlas: TextureAtlas,
) : Group() {

    val skinImage = Image(unitsAtlas.findRegion(entry.skin.name))
    val _th = w * 0.25f
    val nameLabel = Fonts.createWhiteCaption("${entry.name} \nLevel: ${entry.level}", _th)

    init {
        skinImage.apply {
            height = h
            width = h * 0.66f
            setScaling(Scaling.stretch)
            x = (w - this.width) / 2f
            y = 0f
        }
        nameLabel.apply {
            width = w
            height = _th
            wrap = true
            setAlignment(Align.bottom)
        }
        addActor(skinImage)
        addActor(nameLabel)
    }
}
