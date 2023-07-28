package com.game7th.swipe.game

data class SbGame(
    val wave: Int,
    val ticksUntilNpc: Int,
    val maxCharacterId: Int = 0,
    val characters: List<SbCharacter> = emptyList(),
) {
    fun character(id: Int) = characters.firstOrNull { it.id == id }
    fun team(team: Int) = characters.filter { it.team == team }
    fun teamAlive(team: Int) = characters.any { it.team == team }
    fun withUpdatedCharacter(character: SbCharacter) = copy(characters = characters.map { c -> if (c.id == character.id) character else c })
    fun withAddedCharacter(character: SbCharacter) = copy(maxCharacterId = maxCharacterId + 1, characters = characters + character.copy(id = maxCharacterId))
    fun withRemovedCharacter(characterId: Int) = copy(characters = characters.filter { it.id != characterId })
}

data class SbCharacter(
    val id: Int,
    val skin: String,
    val human: Boolean,
    val health: Int,
    val maxHealth: Int,
    val scale: Float,
    val attributes: CharacterAttributes,
    val ultimateProgress: Int,
    val maxUltimateProgress: Int,
    val team: Int,
    val maxTileId: Int = 0,
    val tiles: List<SbTile> = emptyList(),
    val maxEffectId: Int = 0,
    val effects: List<SbEffect> = emptyList(),
) {

    fun tile(id: Int) = tiles.firstOrNull { it.id == id }
    fun withUpdatedTile(tile: SbTile) = copy(tiles = tiles.map { t -> if (t.id == tile.id) tile else t })
    fun withAddedTile(tile: SbTile) = copy(maxTileId = maxTileId + 1, tiles = tiles + tile.copy(id = maxTileId))
    fun tileAt(x: Int, y: Int, z: Int) = tiles.firstOrNull { it.x == x && it.y == y && it.z == z }
    fun withRemovedTile(tileId: Int) = copy(tiles = tiles.filter { it.id != tileId })
    fun withAddedEffect(effect: SbEffect) = copy(maxEffectId = maxEffectId + 1, effects = effects + effect.copy(id = maxEffectId))
    fun withRemovedEffect(effectId: Int) = copy(effects = effects.filter { it.id != effectId })
    fun withUpdatedEffect(effect: SbEffect) = copy(effects = effects.map { e -> if (e.id == effect.id) effect else e })
    fun removeEffects(skin: String) = copy(effects = effects.filter { it.skin != skin })
    fun withUpdatedHealth(health: Int) = copy(health = health)
    fun <T> collect(key: String): List<T> = effects.mapNotNull { it.data[key] as? T }

    fun sumFloat(key: String) = effects.sumOf { e ->
        (e.data[key] as? Float ?: 0f).toDouble()
    }.toFloat()

    fun sumInt(key: String) = effects.sumOf { e ->
        e.data[key] as? Int ?: 0
    }

    fun withUpdatedUltimateProgress(ultimateProgress: Int) = copy(ultimateProgress = ultimateProgress)
    fun asDisplayed() = SbDisplayCharacter(
        id = id,
        skin = skin,
        health = health,
        maxHealth = maxHealth,
        ultimateProgress = ultimateProgress,
        maxUltimateProgress = maxUltimateProgress,
        effects = emptyList(),
        team = team,
        scale = scale,
    )
}

enum class SbTileMergeStrategy {
    SIMPLE, NONE
}

data class SbTileTemplate(
    val weight: Int,
    val skin: String,
    val z: Int,
    val mergeStrategy: SbTileMergeStrategy,
    val maxProgress: Int,
    val mobility: Int,
)

data class SbTile(
    val id: Int,
    val skin: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val skill: Boolean = false,
    val mobility: Int,
    val mergeStrategy: SbTileMergeStrategy,
    val progress: Int,
    val maxProgress: Int,
    val maxEffectId: Int = 0,
    val effects: List<SbEffect> = emptyList(),
) {
    fun withUpdatedPosition(x: Int, y: Int) = copy(x = x, y = y)
    fun withUpdatedPosition(p: Int) = copy(x = p % 5, y = p / 5)
    fun withAddedEffect(effect: SbEffect) = copy(maxEffectId = maxEffectId + 1, effects = effects + effect.copy(id = maxEffectId))
    fun withRemovedEffect(effectId: Int) = copy(effects = effects.filter { it.id != effectId })
    fun withUpdatedEffect(effect: SbEffect) = copy(effects = effects.map { e -> if (e.id == effect.id) effect else e })
    fun asDisplayed(): SbDisplayTile = SbDisplayTile(
        id = id,
        skin = skin,
        type = if (z == LAYER_BACKGROUND) SbDisplayTileType.BACKGROUND else SbDisplayTileType.TAROT,
        x = x,
        y = y,
        z = z,
        progress = progress,
        maxProgress = maxProgress,
    )

    companion object {
        const val LAYER_BACKGROUND = 0
        const val LAYER_TILE = 2
    }
}

data class SbEffect(
    val id: Int,
    val skin: String,
    val data: Map<String, Any>
) {
    fun withProperty(key: String, value: Any) = copy(data = data + (key to value))
}

data class SbDisplayCharacter(
    val id: Int,
    val team: Int,
    val scale: Float,
    val skin: String,
    val health: Int,
    val maxHealth: Int,
    val ultimateProgress: Int,
    val maxUltimateProgress: Int,
    val effects: List<SbCharacterDisplayEffect>
) {

}

data class SbCharacterDisplayEffect(
    val skin: String
)

data class SbElemental(
    val phys: Float = 0f,
    val dark: Float = 0f,
    val light: Float = 0f,
    val shock: Float = 0f,
    val fire: Float = 0f,
    val cold: Float = 0f
) {
    fun reducedByResist(resist: SbElemental) = copy(
        phys = phys * (1f - resist.phys),
        dark = dark * (1f - resist.dark),
        light = light * (1f - resist.light),
        shock = shock * (1f - resist.shock),
        fire = fire * (1f - resist.fire),
        cold = cold * (1f - resist.cold)
    )

    fun addedTo(other: SbElemental) = copy(
        phys = phys + other.phys,
        dark = dark + other.dark,
        light = light + other.light,
        shock = shock + other.shock,
        fire = fire + other.fire,
        cold = cold + other.cold
    )

    fun scaledBy(scale: SbElemental): SbElemental = copy(
        phys = phys * (1f + scale.phys),
        dark = dark * (1f + scale.dark),
        light = light * (1f + scale.light),
        shock = shock * (1f + scale.shock),
        fire = fire * (1f + scale.fire),
        cold = cold * (1f + scale.cold),
    )

    fun multipledBy(scale: Float): SbElemental = copy(
        phys = phys * (1f + scale),
        dark = dark * (1f + scale),
        light = light * (1f + scale),
        shock = shock * (1f + scale),
        fire = fire * (1f + scale),
        cold = cold * (1f + scale),
    )

    fun total() = phys + dark + light + shock + fire + cold
}

enum class SbDisplayTileType {
    TAROT, BACKGROUND,
}

data class SbDisplayTile(
    val id: Int,
    val skin: String,
    val type: SbDisplayTileType,
    val progress: Int,
    val maxProgress: Int,
    val x: Int,
    val y: Int,
    val z: Int,
)

sealed interface SbBattleFieldDisplayEffect {
    data class TarotSimpleAttack(val skin: String, val from: Int, val to: Int): SbBattleFieldDisplayEffect
    data class TarotDirectedAoe(val skin: String, val from: Int, val team: Int): SbBattleFieldDisplayEffect
    data class TarotStatic(val skin: String, val at: Int): SbBattleFieldDisplayEffect
    data class TarotUltimate(val skin: String): SbBattleFieldDisplayEffect
}

sealed interface SbTileFieldDisplayEffect {
    data class TarotOverPosition(val skin: String, val x: Int, val y: Int): SbTileFieldDisplayEffect
}

sealed interface SbDisplayEvent {
    interface SbCharacterSpecificEvent : SbDisplayEvent {
        val characterId: Int
    }

    data class SbCreateCharacter(val personage: SbDisplayCharacter): SbDisplayEvent
    data class SbDestroyCharacter(val id: Int): SbDisplayEvent
    data class SbUpdateCharacter(val personage: SbDisplayCharacter): SbDisplayEvent
    data class SbShowPopup(val characterId: Int, val text: String, val icons: List<String>): SbDisplayEvent
    data class SbShowTarotEffect(val effect: SbBattleFieldDisplayEffect): SbDisplayEvent

    data class SbWave(val wave: Int): SbDisplayEvent

    data class SbShowTileFieldEffect(override val characterId: Int, val effect: SbTileFieldDisplayEffect): SbCharacterSpecificEvent
    data class SbCreateTile(override val characterId: Int, val tile: SbDisplayTile) : SbCharacterSpecificEvent
    data class SbDestroyTile(override val characterId: Int, val z: Int, val tileId: Int): SbCharacterSpecificEvent
    data class SbMoveTile(override val characterId: Int, val z: Int, val tileId: Int, val tox: Int, val toy: Int, val remainder: SbDisplayTile?, val targetTile: SbDisplayTile?) : SbCharacterSpecificEvent
}
