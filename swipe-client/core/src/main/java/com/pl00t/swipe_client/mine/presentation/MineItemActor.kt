package com.pl00t.swipe_client.mine.presentation

import com.badlogic.gdx.scenes.scene2d.Group
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.mine.data.MineItem
import ktx.actors.alpha

class MineItemActor(
    private val r: Resources,
    private val item: MineItem,
) : Group() {

    val gemImage = r.image(ATLAS, item.skin)
    val shadow = r.image(Resources.ux_atlas, "background_black").apply {
        alpha = 0.75f
    }
    val tierText = r.regular24Focus(UiTexts.Mine.CaptionGemTier.value(r.l) + (item.tier + 1).toString())

    init {
        addActor(gemImage)
        addActor(shadow)
        addActor(tierText)
    }

    override fun setSize(width: Float, height: Float) {
        super.setSize(width, height)

        gemImage.setSize(width, height)

        shadow.setSize(width - 6f, 30f)
        shadow.setPosition(3f, 3f)
        tierText.setSize(width - 6f, 20f)
        tierText.setPosition(3f, 8f)
    }

}

private const val ATLAS = "atlases/mine.atlas"

