package com.game7th.swipe.game

import kotlin.math.min
import kotlin.random.Random

private fun inb(x: Int, y: Int) = x > -1 && x < 5 && y > -1 && y < 5

fun SbContext.freePositions(characterId: Int, z: Int, count: Int): List<Int> {
    return game.character(characterId)?.let { character ->
        return (0 until 25).shuffled().filter { p -> character.tileAt(p % 5, p / 5, z) == null}.take(count)
    } ?: emptyList()
}

fun SbContext.generateTiles(characterId: Int, tiles: Int) {
    var character = game.character(characterId) ?: return
    val config = character.collect<SbTileTemplate>(CommonKeys.Generator.GENERATOR)
    val total = config.sumOf { it.weight }

    val positions = freePositions(characterId, SbTile.LAYER_TILE, tiles)
    positions.forEach { p ->
        val random = Random.nextInt(total)
        var w = 0
        val tileTemplate = config.first {
            w += it.weight
            w > random
        }
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
            skill = true,
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

fun SbContext.evaluateSimple(): Int {
    val heroLifeScore = game.characters.filter { it.team == 0 }.sumOf { it.health }
    if (heroLifeScore <= 0) return 0
    val heroUltimateScore = game.character(0)?.ultimateProgress ?: 0
    val enemyMonsterLifeScore = game.characters.filter { it.team == 1 }.sumOf { it.health }
    return heroLifeScore * (1 + game.wave) + heroUltimateScore - enemyMonsterLifeScore
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
    val tilesComplete = mutableListOf<Int>()
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
                        if (t.skin == tile.skin && (tile.mergeStrategy == SbTileMergeStrategy.SIMPLE || tile.mergeStrategy == SbTileMergeStrategy.KEEP_MAX) && tile.progress < tile.maxProgress && t.progress < t.maxProgress) {
                            merged = true
                            mergedCount++
                            val sumStack = t.progress + tile.progress
                            val newTile = t.copy(progress = min(sumStack, t.maxProgress))
                            if (newTile.progress == t.maxProgress && tile.mergeStrategy == SbTileMergeStrategy.SIMPLE) {
                                tilesComplete.add(newTile.id)
                            }
                            val progressLeft = sumStack - t.maxProgress
                            val remainderTile = if (progressLeft <= 0) null else tile.copy(x = tx, y = ty, progress = progressLeft)
                            events.add(SbDisplayEvent.SbMoveTile(
                                characterId = character.id,
                                tileId = tile.id,
                                tox = t.x,
                                toy = t.y,
                                remainder = remainderTile?.asDisplayed(),
                                targetTile = newTile.asDisplayed(),
                                z = tile.z,
                            ))
                            character = character.withUpdatedTile(newTile)
                            character = if (remainderTile == null) character.withRemovedTile(tile.id) else character.withUpdatedTile(remainderTile)
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
                    remainder = newTile.asDisplayed(),
                    z = tile.z,
                    targetTile = null
                ))
                character = character.withUpdatedTile(newTile)
            }
        }
    }
    game = game.withUpdatedCharacter(character)

    tilesComplete.forEach { tileId ->
        handleEvent(SbEvent.TileReachedMaxProgress0(characterId, tileId))
        handleEvent(SbEvent.TileReachedMaxProgress1(characterId, tileId))
    }
    game.character(characterId)?.let { character = it }

    if (mergedCount > 0) {
        val comboAmount = (Random.nextFloat() * 10f * (1f + 0.05f * character.attributes.mind)).toInt()
        val newUltimateProgress = min(character.maxUltimateProgress, character.ultimateProgress + comboAmount)
        character = character.withUpdatedUltimateProgress(newUltimateProgress)
        events.add(SbDisplayEvent.SbUpdateCharacter(character.asDisplayed()))
        game = game.withUpdatedCharacter(character)

        if (newUltimateProgress >= character.maxUltimateProgress && !character.human) {
            generateTiles(character.id, 10)
            character = game.character(characterId)?.withUpdatedUltimateProgress(0) ?: return
            game = game.withUpdatedCharacter(character)
        }
    }
    val timeEffectsUpdated = character.effects.mapNotNull { effect ->
        if (effect.data.containsKey(CommonKeys.DURATION)) {
            val newDuration = (effect.data[CommonKeys.DURATION]!! as Int) - 1
            if (newDuration <= 0) null else {
                effect.copy(data = effect.data.toMutableMap().apply { this[CommonKeys.DURATION] = newDuration })
            }
        } else {
            effect
        }
    }
    character = character.copy(effects = timeEffectsUpdated)

    game = game.withUpdatedCharacter(character)

    if (character.id == 0 && game.tutorialMetadata.isFirstTutorial && game.tutorialMetadata.firstTutorialTick == 0) {
        game = game.copy(tutorialMetadata = game.tutorialMetadata.copy(firstTutorialTick = 1, isFirstTutorial = false))
        game = game.withUpdatedCharacter(game.character(0)!!.withAddedTile(SbTile(0, "VALERIAN_RADIANT_STRIKE", 2, 4, SbTile.LAYER_TILE, true, 5, SbTileMergeStrategy.SIMPLE, 1, 3, 0, emptyList())))
        events.add(SbDisplayEvent.SbCreateTile(0, game.character(0)!!.tiles.last().asDisplayed()))
    } else {
        generateTiles(character.id, 1)
    }

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
