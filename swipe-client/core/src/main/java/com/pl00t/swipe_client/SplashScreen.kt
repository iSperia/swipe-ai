package com.pl00t.swipe_client

import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling

class SplashScreen(
    r: Resources
) : SbBaseScreen(r) {

    override fun loadScreenContent() {
        r.loadAtlas(Resources.ux_atlas)
        r.loadAtlas(Resources.battle_atlas)
        r.loadAtlas(Resources.map_atlas)
        r.onLoad {
            hideSplash()
            root.addActor(r.image(Resources.ux_atlas, "solid_window_background").apply {
                width = r.width
                height = r.height
                setScaling(Scaling.fill)
                setOrigin(Align.center)
            })
        }
    }
}
