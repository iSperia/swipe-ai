package com.pl00t.swipe_client.screen.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.ux.require

class PopupActor(
    private val text: String,
    private val icons: List<String>,
    private val context: SwipeContext,
    private val skin: Skin,
) : Table() {

    private val label = Label(text, skin, "window_title").apply {
        setAlignment(Align.left)
        setFontScale(0.66f)
    }

    init {
        icons.forEachIndexed { index, icon ->
            context.commonAtlas(Atlases.COMMON_BATTLE).findRegion(icon).require().let { iconTexture ->
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
