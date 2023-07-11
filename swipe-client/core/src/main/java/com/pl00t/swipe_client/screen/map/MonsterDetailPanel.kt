package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.battle.MonsterConfiguration
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.hideToBehindAndRemove
import ktx.actors.onClick

class MonsterDetailPanel(
    private val monsterConfiguration: MonsterConfiguration,
    private val context: SwipeContext,
    private val skin: Skin,
) : Group() {

    init {

        val panel = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_bg")).apply {
            width = 480f
            height = 60f
            setScaling(Scaling.stretch)
        }
        val buttonClose = Buttons.createActionButton("Close", skin).apply {
            x = 300f
            y = 12f
        }
        buttonClose.onClick {
            this@MonsterDetailPanel.hideToBehindAndRemove(context.height())
        }

        val monsterDetail = MonsterDetailActor(
            context.height() - 60f,
            monsterConfiguration,
            character = null,
            context,
            skin
        ).apply {
            y = 60f
        }

        addActor(monsterDetail)
        addActor(panel)
        addActor(buttonClose)
    }
}
