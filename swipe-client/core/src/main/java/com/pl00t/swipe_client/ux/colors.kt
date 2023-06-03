package com.pl00t.swipe_client.ux

import com.badlogic.gdx.graphics.Color

object Colors {
    val BG_COLOR = Color().apply { Color.rgba8888ToColor(this, Color.argb8888(1f, 178/255f, 175/255f, 172/255f)) }
    val MAIN_COLOR = Color().apply { Color.rgba8888ToColor(this, Color.argb8888(1f, 113/255f, 121/255f, 126/255f)) }
    val ACCENT_COLOR = Color().apply { Color.rgba8888ToColor(this, Color.argb8888(1f, 175/255f, 238/255f, 238/255f)) }

    val RARITY_1 = Color().apply { Color.rgba8888ToColor(this, Color.argb8888(1f, 192/255f, 192/255f, 192/255f)) }
    val RARITY_2 = Color().apply { Color.rgba8888ToColor(this, Color.argb8888(1f, 148/255f, 179/255f, 196/255f)) }
    val RARITY_3 = Color().apply { Color.rgba8888ToColor(this, Color.argb8888(1f, 0/255f, 127/255f, 255/255f)) }
    val RARITY_4 = Color().apply { Color.rgba8888ToColor(this, Color.argb8888(1f, 153/255f, 102/255f, 204/255f)) }
    val RARITY_5 = Color().apply { Color.rgba8888ToColor(this, Color.argb8888(1f, 220/255f, 164/255f, 66/255f)) }

    val RARITY_COLORS = listOf(RARITY_1, RARITY_2, RARITY_3, RARITY_4, RARITY_5)
}
