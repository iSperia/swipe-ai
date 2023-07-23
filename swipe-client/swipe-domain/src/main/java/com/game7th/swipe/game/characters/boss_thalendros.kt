package com.game7th.swipe.game.characters

import com.game7th.swipe.battle.floatAttribute
import com.game7th.swipe.battle.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val THALENDROS_THORN_WHIP = "THALENDROS_THORN_WHIP"
private const val THALENDROS_EARTHQUAKE_SLAM = "THALENDROS_EARTHQUAKE_SLAM"
private const val THALENDROS_DARK_AURA = "THALENDROS_DARK_AURA"
private const val THALENDROS_CORRUPTED_ROOTS = "THALENDROS_CORRUPTED_ROOTS"
private const val THALENDROS_DARK_TILE = "THALENDROS_DARK_TILE"

private const val tw_base = "tw_base"
private const val tw_scale = "tw_scale"
private const val es_tiles = "es_tiles"
private const val es_damage = "es_damage"
private const val es_scale = "es_scale"
private const val da_tiles = "da_tiles"
private const val da_base = "da_base"
private const val da_scale = "da_scale"

fun provideThalendrosTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(

    "thalendros.thorn_whip" to { context, event ->
        context.useOnComplete(event, THALENDROS_THORN_WHIP) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(tw_base) * (1f + 0.01f * balance.intAttribute(tw_scale) * character.attributes.body)
            ).multipledBy(if (lucky) 2f else 1f)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THALENDROS_THORN_WHIP, characterId, target)))
            }
        }
    },

    "thalendros.earthquake_slam" to { context, event ->
        context.useOnComplete(event, THALENDROS_EARTHQUAKE_SLAM) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            allEnemies(characterId).randomOrNull()?.let { targetId ->
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THALENDROS_EARTHQUAKE_SLAM, characterId, targetId
                        )
                    )
                )

                val rootsCount = game.character(targetId)!!.tiles.count { it.skin == THALENDROS_CORRUPTED_ROOTS }
                if (rootsCount <= 0) {
                    val tiles = balance.intAttribute(es_tiles)
                    val positions = (0 until 25).shuffled().take(tiles)
                    positions.forEach { p ->
                        val x = p % 5
                        val y = p / 5
                        game.character(targetId)?.tileAt(x, y, SbTile.LAYER_TILE)?.let { tileToDelete ->
                            destroyTile(targetId, tileToDelete.id)
                        }
                        val tile = SbTile(
                            id = 0,
                            skin = THALENDROS_CORRUPTED_ROOTS,
                            x = x,
                            y = y,
                            z = SbTile.LAYER_TILE,
                            skill = false,
                            mobility = 5,
                            mergeStrategy = SbTileMergeStrategy.SIMPLE,
                            progress = 1,
                            maxProgress = tiles,
                            maxEffectId = 0,
                            effects = emptyList()
                        )
                        game.character(targetId)?.let {
                            val character = it.withAddedTile(tile)
                            game = game.withUpdatedCharacter(character)
                            events.add(SbDisplayEvent.SbCreateTile(targetId, character.tiles.last().asDisplayed()))
                        }
                    }
                } else {
                    val damage = SbElemental(
                        dark = rootsCount * balance.floatAttribute(es_damage) * (1f + 0.01f * balance.intAttribute(
                            es_scale
                        ) * character.attributes.spirit)
                    ).multipledBy(if (lucky) 2f else 1f)
                    dealDamage(characterId, targetId, damage)
                }
            }
        }
    },

    "thalendros.dark_aura" to { context, event ->

        context.useOnComplete(event, THALENDROS_DARK_AURA) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            allEnemies(characterId).randomOrNull()?.let { targetId ->
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THALENDROS_DARK_AURA, characterId, targetId)))

                val auraTilesCount = balance.intAttribute(da_tiles) * (if (lucky) 2 else 1)
                val positions = freePositions(targetId, SbTile.LAYER_BACKGROUND, auraTilesCount)
                positions.forEach { p ->
                    val tile = SbTile(
                        id = 0,
                        skin = THALENDROS_DARK_TILE,
                        x = p % 5,
                        y = p / 5,
                        z = SbTile.LAYER_BACKGROUND,
                        mobility = 0,
                        mergeStrategy = SbTileMergeStrategy.NONE,
                        progress = 0,
                        maxProgress = 0,
                        maxEffectId = 0,
                        effects = emptyList()
                    )
                    var targetCharacter = game.character(targetId) ?: return@forEach
                    targetCharacter = targetCharacter.withAddedTile(tile)

                    game = game.withUpdatedCharacter(targetCharacter)
                    events.add(SbDisplayEvent.SbCreateTile(characterId = targetId, tile = targetCharacter.tiles.last().asDisplayed()))
                }
            }
        }

        context.triggerBackgroundLayerOnComplete(event, THALENDROS_DARK_TILE) { characterId, tileId ->
            val thalendrosSpirit = game.characters.firstOrNull { it.skin == "MONSTER_THALENDROS" }?.attributes?.spirit ?: 0
            val damage = balance.floatAttribute(da_base) * (1f + 0.01f * balance.intAttribute(da_scale) * thalendrosSpirit)
            dealDamage(null, characterId, SbElemental(dark = damage))
            events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(THALENDROS_DARK_AURA, characterId)))
        }
    }

)
