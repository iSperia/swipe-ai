package com.pl00t.swipe_client.battle

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources

class PopupActor(
    private val r: Resources,
    private val text: String,
    private val icons: List<String>,
) : Table() {

    private val label = r.regular24Outline(text).apply {
        setAlignment(Align.left)
        setFontScale(1.5f)
    }

    init {
        icons.forEachIndexed { index, icon ->
            val image = r.image(Resources.battle_atlas, icon).apply {
                setSize(24f, 24f)
            }
            add(image).size(24f, 24f)
        }
        add(label)

        width = 160f
        height = 36f
    }
}
