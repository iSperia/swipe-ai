package com.pl00t.swipe_client.home

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.SbBaseScreen
import com.pl00t.swipe_client.services.profile.SwipeAct
import ktx.actors.alpha

class HomeScreen(
    r: R
) : SbBaseScreen(r) {

    lateinit var actId: SwipeAct

    private var mapActor: MapActor? = null

    override fun loadScreenContent() {

        actId = SwipeAct.ACT_1

        r.loadAtlas(R.actAtlas(actId))
        r.loadAtlas(R.ux_atlas)
        r.loadAtlas(R.units_atlas)
        r.loadSkin(R.SKIN)

        r.onLoad {
            r.skin().getFont("caption").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("regular").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            hideSplash()
            showMap()
        }
    }

    private fun showMap() {
        mapActor = MapActor(r, actId).apply {
            alpha = 0f
            addAction(Actions.alpha(1f, 0.3f))
        }
        root.addActor(mapActor)
    }

    private fun hide(actor: Actor?) = actor?.addAction(Actions.sequence(
        Actions.alpha(0f, 0.3f),
        Actions.removeActor()
    ))
}
