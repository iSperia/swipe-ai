package com.pl00t.swipe_client

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.pl00t.swipe_client.services.profile.SwipeAct

object Atlases {
    val COMMON_MAP = "atlases/map.atlas"
    val COMMON_UX = "styles/ui.atlas"
    val COMMON_CORE = "atlases/core.atlas"
    val COMMON_BATTLE = "atlases/battle.atlas"
    val COMMON_UNITS = "atlases/units.atlas"
    val COMMON_TAROT = "atlases/tarot.atlas"

    fun ACT(act: SwipeAct) = "atlases/$act.atlas"
}

interface SwipeContext {

    fun width(): Float

    fun height(): Float

    fun commonAtlas(atlas: String): TextureAtlas
}
