package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext

class PopupActor(
    private val text: String,
    private val icons: List<String>,
    private val context: SwipeContext,
    private val skin: Skin,
) : Table() {

    private val label = Label(text, skin, "damage_popup").apply {
        setAlignment(Align.left)
    }

    init {
        icons.forEachIndexed { index, icon ->
            context.commonAtlas(Atlases.COMMON_BATTLE).findRegion(icon).let { iconTexture ->
                val image = Image(iconTexture)
                image.width = 24f
                image.height = 24f
                add(image).width(24f).height(24f)
            }
        }
        add(label)
        width = 160f
    }
}
