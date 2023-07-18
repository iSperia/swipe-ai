package com.game7th.swipe.game

import com.game7th.swipe.battle.Battle
import com.game7th.swipe.battle.BattleEvent
import com.game7th.swipe.battle.ProcessResult
import kotlin.math.min
import kotlin.random.Random

private fun inb(x: Int, y: Int) = x > -1 && x < 5 && y > -1 && y < 5

fun SbContext.generateTiles(characterId: Int, tiles: Int) {
    var character = game.character(characterId) ?: return
    val config = character.collect<Pair<SbTileTemplate, Int>>(CommonKeys.Generator.GENERATOR)
    val total = config.sumOf { it.second }

    val positions = (0 until 25).filter { p -> character.tileAt(p % 5, p / 5, SbTile.LAYER_TILE) == null}.take(tiles)
    positions.forEach { p ->
        val random = Random.nextInt(total)
        var w = 0
        val tileTemplate = config.first {
            w += it.second
            w > random
        }.first
        val tile = SbTile(
            id = 0,
            skin = tileTemplate.skin,
            x = p % 5,
            y = p / 5,
            z = tileTemplate.z,
            mobility = tileTemplate.mobility,
            mergeStrategy = tileTemplate.mergeStrategy,
            progress = 1,
            maxProgress = tileTemplate.maxProgress,
            maxEffectId = 0,
            effects = emptyList()
        )
        character = character.withAddedTile(tile)
        game = game.withUpdatedCharacter(character)

        events.add(SbDisplayEvent.SbCreateTile(
            characterId = characterId,
            tile = character.tiles.last().asDisplayed()
        ))
    }
}

fun SbContext.swipe(characterId: Int, dx: Int, dy: Int) {
    var character = game.character(characterId) ?: return

    val tilesToProcess = when {
        dx == -1 -> character.tiles.sortedBy { it.x }
        dx == 1 -> character.tiles.sortedByDescending { it.x }
        dy == -1 -> character.tiles.sortedBy { it.y }
        dy == 1 -> character.tiles.sortedByDescending { it.y }
        else -> character.tiles.shuffled()
    }
    var mergedCount = 0
    tilesToProcess.forEach { ttp ->
        character.tile(ttp.id)?.let { tile ->
            val maxDistance = min(5, tile.mobility)
            var tx = tile.x
            var ty = tile.y
            var merged = false
            var dist = 0
            while (dist < maxDistance) {
                dist++
                val nx = tile.x + dist * dx
                val ny = tile.y + dist * dy
                //out of bounds, no move
                if (inb(nx, ny)) {
                    val t = character.tileAt(nx, ny, tile.z)
                    if (t != null) {
                        //Simple merge us into another stuff
                        if (t.skin == tile.skin && tile.mergeStrategy == SbTileMergeStrategy.SIMPLE && tile.progress < tile.maxProgress && t.progress < t.maxProgress) {
                            merged = true
                            mergedCount++
                            val sumStack = t.progress + tile.progress
                            //TODO: add remainder
                            events.add(SbDisplayEvent.SbMoveTile(
                                characterId = character.id,
                                tileId = tile.id,
                                tox = t.x,
                                toy = t.y,
                                remainder = null,
                            ))
                            val newTile = t.copy(progress = min(sumStack, t.maxProgress))
                            character = character.withUpdatedTile(newTile).withRemovedTile(tile.id)
                        }
                        break
                    } else {
                        tx = nx
                        ty = ny
                    }
                } else {
                    break
                }
            }
            if (!merged && tx != tile.x || ty != tile.y) {
                val newTile = tile.withUpdatedPosition(tx, ty)
                events.add(SbDisplayEvent.SbMoveTile(
                    characterId = character.id,
                    tileId = tile.id,
                    tox = tx,
                    toy = ty,
                    remainder = newTile.asDisplayed()
                ))
                character = character.withUpdatedTile(newTile)
            }
        }
    }

    if (mergedCount > 0) {
        val comboAmount = (10f * (1f + 0.05f * character.sumInt(CommonKeys.Attributes.MIND))).toInt()
        val newUltimateProgress = min(character.maxUltimateProgress, character.ultimateProgress + comboAmount)
        character = character.withUpdatedUltimateProgress(newUltimateProgress)
        events.add(SbDisplayEvent.SbUpdateCharacter(character.asDisplayed()))
        game = game.withUpdatedCharacter(character)

        if (newUltimateProgress >= character.maxUltimateProgress && !character.human) {
            generateTiles(character.id, 10)
            character = game.character(characterId)?.withUpdatedUltimateProgress(0) ?: return
        }
    }
    game = game.withUpdatedCharacter(character)

    generateTiles(character.id, 1)

    handleEvent(SbEvent.EndOfSwipe(character.id))
    handleEvent(SbEvent.EndOfTick(character.id))

    var swipes = game.ticksUntilNpc
    if (character.human) {
        swipes = game.ticksUntilNpc - 1
        if (swipes <= 0) {
            swipes = game.characters.count { it.human }
            val bots = game.characters.filter { !it.human }.toList()
            bots.forEach { botUnit ->
                game.character(botUnit.id)?.let { bot ->
                    val directions = when (Random.nextInt(4)) {
                        0 -> -1 to 0
                        1 -> 1 to 0
                        2 -> 0 to 1
                        else -> 0 to -1
                    }
                    swipe(bot.id, directions.first, directions.second)
                }
            }
        }
    }
}

fun SbContext.processUltimate(characterId: Int) {
    game.character(characterId)?.let { character ->
        if (character.ultimateProgress >= character.maxUltimateProgress) {
            handleEvent(SbEvent.UltimateUse(characterId))
        }
    }
}
