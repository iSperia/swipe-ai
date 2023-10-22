package com.pl00t.swipe_client.monster

import com.badlogic.gdx.scenes.scene2d.Group
import com.pl00t.swipe_client.Resources

class MonsterRankActor(
    private val r: Resources,
    private val rarity: Int,
): Group() {

    init {
        (0 until rarity).forEach { i ->
            val actor = r.image(Resources.ux_atlas, "rank", rarity)
            addActor(actor)
        }
    }

    override fun sizeChanged() {
        val singleWidth = this.width
        val singleHeight = this.width * 0.83f
        val delta = if (rarity > 1) (this.height - singleWidth) / (rarity - 1) else 0f
        var yy = this.height - singleHeight
        children.forEach { actor ->
            actor.setSize(singleWidth, singleHeight)
            actor.setPosition(0f, yy)
            yy -= delta
        }
    }
}
