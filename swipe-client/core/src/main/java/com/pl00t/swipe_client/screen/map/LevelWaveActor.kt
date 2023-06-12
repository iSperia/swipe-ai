package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.ux.Fonts

class LevelWaveActor(
    private val index: Int,
    private val units: List<FrontMonsterEntryModel>,
    private val unitsAtlas: TextureAtlas,
    private val w: Float
) : Group() {

    private val caption = Fonts.createWhiteTitle("Wave $index", w * 0.1f)
    private val unitGroup = Group()

    init {
        width = w
        height = w * 0.6f

        caption.apply {
            setAlignment(Align.center)
            y = w * 0.5f
            width = w
            height = w * 0.1f
        }
        unitGroup.apply {
            units.forEachIndexed { index, unit ->
                val entry = MonsterEntryActor(unit, w / 3f, w * 0.5f, unitsAtlas).apply {
                    x = index * w / 3f
                }
                addActor(entry)
            }
        }
        addActor(caption)
        addActor(unitGroup)
    }
}
