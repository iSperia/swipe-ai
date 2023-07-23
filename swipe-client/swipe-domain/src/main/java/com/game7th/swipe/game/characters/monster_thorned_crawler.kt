package com.game7th.swipe.game.characters

import com.game7th.swipe.battle.floatAttribute
import com.game7th.swipe.battle.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val THORNED_CRAWLER_VICIOUS_PINCERS = "THORNED_CRAWLER_VICIOUS_PINCERS"
private const val THORNED_CRAWLER_DEBILIATING_STRIKE = "THORNED_CRAWLER_DEBILIATING_STRIKE"
private const val THORNED_CRAWLER_LEECHING_SHADOWS = "THORNED_CRAWLER_LEECHING_SHADOWS"

private const val vp_base_phys = "vp_base_phys"
private const val vp_base_dark = "vp_base_dark"
private const val vp_scale_body = "vp_scale_body"
private const val vp_scale_spirit = "vp_scale_spirit"
private const val ds_base = "ds_base"
private const val ds_scale = "ds_scale"
private const val ds_tiles = "ds_tiles"
private const val ls_base = "ls_base"
private const val ls_scale = "ls_scale"
private const val ls_heal_base = "ls_heal_base"
private const val ls_heal_scale = "ls_heal_scale"

fun provideThornedCrawlerTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "thorned_crawler.vicious_pincers" to { context, event ->
        context.useOnComplete(event, THORNED_CRAWLER_VICIOUS_PINCERS) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(vp_base_phys) * (1f + 0.01f * balance.intAttribute(vp_scale_body) * character.attributes.body),
                dark = balance.floatAttribute(vp_base_dark) * (1f + 0.01f * balance.intAttribute(vp_scale_spirit) * character.attributes.spirit)
            ).multipledBy(if (lucky) 2f else 1f)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THORNED_CRAWLER_VICIOUS_PINCERS, characterId, target)))
            }
        }
    },

    "thorned_crawler.debiliating_stirke" to { context, event ->
        context.useOnComplete(event, THORNED_CRAWLER_DEBILIATING_STRIKE) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(ds_base) * (1f + 0.01f * balance.intAttribute(ds_scale) * character.attributes.body)
            ).multipledBy(if (lucky) 2f else 1f)
            val tilesCount = balance.intAttribute(ds_tiles)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THORNED_CRAWLER_DEBILIATING_STRIKE, characterId, target)))

                val positions = freePositions(target, SbTile.LAYER_BACKGROUND, tilesCount)
                positions.forEach { p ->
                    val tile = SbTile(
                        id = 0,
                        skin = "COMMON_WEAKNESS",
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
                    var targetCharacter = game.character(target) ?: return@forEach
                    targetCharacter = targetCharacter.withAddedTile(tile).withAddedEffect(
                        SbEffect(
                            id = 0,
                            skin = "COMMON_WEAKNESS",
                            emptyMap()
                        ))

                    game = game.withUpdatedCharacter(targetCharacter)
                    events.add(SbDisplayEvent.SbCreateTile(characterId = target, tile = targetCharacter.tiles.last().asDisplayed()))
                }
            }
        }
    },

    "thorned_crawler.leeching_shadows" to { context, event ->
        context.useOnComplete(event, THORNED_CRAWLER_LEECHING_SHADOWS) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                dark = balance.floatAttribute(ls_base) * (1f + 0.01f * balance.intAttribute(ls_scale) * character.attributes.spirit)
            ).multipledBy(if (lucky) 2f else 1f)

            val healAmount = balance.floatAttribute(ls_heal_base) * (1f + 0.01f * balance.intAttribute(ls_heal_scale) * character.attributes.spirit)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THORNED_CRAWLER_DEBILIATING_STRIKE, characterId, target)))
            }

            healCharacter(character.id, healAmount.toInt())
        }
    }
)
