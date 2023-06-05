package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.pl00t.swipe_client.services.battle.logic.Effect

class UnitActor(
    val id: Int,
    var health: Int,
    var maxHealth: Int,
    var effects: List<Effect>,
    val atlas: TextureAtlas,
    val texture: String,
    val team: Int,
    val w: Float,//character width
    val s: Float,//character scale
    val position: Int,
) : Group() {

    val characterImage: Image

    init {
        val region = atlas.findRegion(texture)
        characterImage = Image(region).apply {
            this.scaleX = if (team == 0) 1f else -1f
            this.width = s * w
            this.height = s * w * region.originalHeight.toFloat() / region.originalWidth.toFloat()
        }
        addActor(characterImage)
    }
}
