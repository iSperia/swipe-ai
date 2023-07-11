package com.pl00t.swipe_client.ux

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion

fun AtlasRegion?.require() = if (this == null) throw IllegalStateException("No region") else this
