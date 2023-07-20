package com.game7th.swipe.battle

data class Character(
    val id: Int,
    val field: TileField,
    val health: Int,
    val maxHealth: Int,
    val resists: ElementalConfig,
    val effects: List<Effect>,
    val skin: UnitSkin,
    val level: Int,
    val human: Boolean,
    val team: Int,
    val attributes: CharacterAttributes,
    val ultimateProgress: Int,
    val maxUltimateProgress: Int,
    val combo: Int,
    val ultimateBehavior: TileSkin,
    val scale: Float,
) {

    override fun toString(): String {
        return "$skin $health/$maxHealth \n$field"
    }

    fun updateField(field: TileField) = copy(field = field)

    fun addTile(tile: Tile) = copy(field = field.copy(tiles = field.tiles + tile, maxTileId = field.maxTileId + 1))

    fun removeTile(id: Int) = copy(field = field.copy(tiles = field.tiles.filterNot { it.id == id }))

    fun updateUltimateProgress(combo: Int, progress: Int) = copy(combo = combo, ultimateProgress = progress)
}


