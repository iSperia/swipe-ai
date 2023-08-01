package com.pl00t.swipe_client

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling

class SplashScreen(
    r: R
) : SbBaseScreen(r) {

    override fun loadScreenContent() {
        r.loadAtlas(R.ux_atlas)
        r.loadAtlas(R.battle_atlas)
        r.loadAtlas(R.map_atlas)
        r.onLoad {
            hideSplash()
            root.addActor(r.image(R.ux_atlas, "solid_window_background").apply {
                width = r.width
                height = r.height
                setScaling(Scaling.fill)
                setOrigin(Align.center)
            })
        }
    }
}
