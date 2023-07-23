package com.game7th.swipe.game.characters

import com.game7th.swipe.battle.floatAttribute
import com.game7th.swipe.battle.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val CORRUPTED_DRYAD_ARBOREAL_FANGS = "CORRUPTED_DRYAD_ARBOREAL_FANGS"
private const val CORRUPTED_DRYAD_VILE_SIPHON = "CORRUPTED_DRYAD_VILE_SIPHON"
private const val CORRUPTED_DRYAD_SHADOWED_ANNIHILATION = "CORRUPTED_DRYAD_SHADOWED_ANNIHILATION"

private const val af_base = "af_base"
private const val af_scale = "af_scale"
private const val vs_dmg_base = "vs_dmg_base"
private const val vs_dmg_scale = "vs_dmg_scale"
private const val vs_heal_base = "vs_heal_base"
private const val vs_heal_scale = "vs_heal_scale"
private const val sa_base = "sa_base"
private const val sa_scale = "sa_scale"
private const val sa_tiles = "sa_tiles"

fun provideCorruptedDryadTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "corrupted_dryad.arboreal_fangs" to { context, event ->
        context.useOnComplete(event, CORRUPTED_DRYAD_ARBOREAL_FANGS) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(af_base) * (1f + 0.01f * balance.intAttribute(af_scale) * character.attributes.body)
            ).multipledBy(if (lucky) 2f else 1f)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            CORRUPTED_DRYAD_ARBOREAL_FANGS, characterId, target)))
            }
        }
    },

    "corrupted_dryad.vile_siphon" to  { context, event ->
        context.useOnComplete(event, CORRUPTED_DRYAD_VILE_SIPHON) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                dark = balance.floatAttribute(vs_dmg_base) * (1f + 0.01f * balance.intAttribute(vs_dmg_scale) * character.attributes.body)
            ).multipledBy(if (lucky) 2f else 1f)

            val healAmount = balance.floatAttribute(vs_heal_base) * (1f + 0.01f * balance.intAttribute(vs_heal_scale) * character.attributes.spirit)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            CORRUPTED_DRYAD_VILE_SIPHON, characterId, target)))
            }

            healCharacter(character.id, healAmount.toInt())
        }
    },

    "corrupted_dryad.shadowed_annihilation" to { context, event ->
        context.useOnComplete(event, CORRUPTED_DRYAD_SHADOWED_ANNIHILATION) { characterId, tileId, lucky ->
            val character= game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                dark = balance.floatAttribute(sa_base) * (1f + 0.01f * balance.intAttribute(sa_scale) * character.attributes.spirit)
            ).multipledBy(if (lucky) 2f else 1f)

            val tiles = balance.intAttribute(sa_tiles)

            allEnemies(characterId).forEach { target ->
                val positions = (0 until 25).shuffled().take(tiles)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            CORRUPTED_DRYAD_SHADOWED_ANNIHILATION, characterId, target)))

                game.character(target)?.let { targetCharacter ->
                    var missedCount = 0
                    positions.forEach { p ->
                        game.character(target)?.tileAt(p % 5, p / 5, SbTile.LAYER_TILE)?.let { tile ->
                            if (tile.skill) {
                                destroyTile(target, tile.id)
                            } else {
                                missedCount++
                            }
                        } ?: missedCount++

                        events.add(SbDisplayEvent.SbShowTileFieldEffect(target, SbTileFieldDisplayEffect.TarotOverPosition(
                            skin = CORRUPTED_DRYAD_SHADOWED_ANNIHILATION,
                            x = p % 5,
                            y = p / 5
                        )))
                    }
                    if (missedCount > 0) {
                        val totalDamage = damage.multipledBy(missedCount.toFloat())
                        dealDamage(character.id, target, totalDamage)
                    }
                }
            }
        }
    }
)
