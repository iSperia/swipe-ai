package com.game7th.swipe.battle

data class TileField(
    val tiles: List<Tile>,
    val maxTileId: Int
) {
    fun tileAt(x: Int, y: Int, layer: Int) = tiles.firstOrNull { it.x == x && it.y == y && it.layer == layer}

    fun tileBy(id: Int) = tiles.firstOrNull { it.id == id }

    override fun toString(): String {
        return tiles.joinToString(",")
    }
}
