package com.pl00t.swipe_client.monster

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.pl00t.swipe_client.Resources

class MonsterRankActor(
    private val r: Resources,
    private val rarity: Int,
): Group() {

    private val actor: Actor

    init {
        actor = r.image(Resources.ux_atlas, "rank", rarity)
        addActor(actor)
    }

    override fun sizeChanged() {
        actor.setSize(this.width, this.height)
        actor.setPosition(0f, 0f)
    }
}
