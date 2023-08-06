package com.pl00t.swipe_client.monster

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.game7th.swipe.SbText
import com.pl00t.swipe_client.R

class LoreActor(
    private val r: R,
    private val lore: SbText
): Group() {

    val loreText = r.labelLore(lore.value(r.l)).apply {
        wrap = true
    }
    val table = Table().apply {
        width = 470f
    }
    val scrollPane = ScrollPane(table).apply {
        height = r.height - 190f
        width = r.width
    }

    init {
        table.add(loreText).width(470f).pad(5f).row()
        table.add().growY()

        addActor(scrollPane)
    }
}
