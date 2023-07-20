package com.game7th.swipe.game

import kotlin.random.Random

fun SbContext.destroyTile(characterId: Int, tileId: Int) {
    game.character(characterId)?.let { character ->
        character.tile(tileId)?.let { tile ->
            game = game.withUpdatedCharacter(character.withRemovedTile(tileId))
            events.add(SbDisplayEvent.SbDestroyTile(characterId, tile.z, tile.id))
        }
    }

}

fun SbContext.useUltimate(characterId: Int) {
    handleEvent(SbEvent.UltimateUse(characterId))
}

fun SbContext.useOnComplete(event: SbEvent, tileSkin: String, action: SbContext.(characterId: Int, tileId: Int, lucky: Boolean) -> Unit) {
    if (event is SbEvent.TileReachedMaxProgress1) {
        val character = game.character(event.characterId)
        val tile = character?.tile(event.tileId)
        if (character != null && tile != null && tile.skin == tileSkin) {

            val luckChance = 0.05f * (1f + character.attributes.spirit * 0.05f)
            val isLucky = Random.nextFloat() < luckChance

            if (isLucky && tile.skill) {
                events.add(SbDisplayEvent.SbShowPopup(
                    characterId = character.id,
                    text = "Lucky!",
                    icons = emptyList()
                ))
            }

            action(character.id, tile.id, isLucky)

            destroyTile(character.id, tile.id)
        }
    }
}

fun SbContext.onEndOfTurn(event: SbEvent, action: SbContext.(characterId: Int) -> Unit) {
    if (event is SbEvent.EndOfTick) {
        action(event.characterId)
    }
}

fun SbContext.triggerBackgroundLayerOnComplete(event: SbEvent, tileSkin: String, action: SbContext.(characterId: Int, tileId: Int) -> Unit) {
    if (event is SbEvent.TileReachedMaxProgress0) {
        val character = game.character(event.characterId)
        val tile = character?.tile(event.tileId)
        if (character != null && tile != null) {
            val bgTile = character.tileAt(tile.x, tile.y, SbTile.LAYER_BACKGROUND)
            if (bgTile?.skin == tileSkin) {
                action(character.id, bgTile.id)
                destroyTile(character.id, bgTile.id)
            }
        }
    }
}
