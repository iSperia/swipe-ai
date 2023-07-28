package com.game7th.swipe.game.characters

import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private val SKIN = "MONSTER_THORNSTALKER"

private val THORNSTALKER_PRIMAL_ASSAULT = "THORNSTALKER_PRIMAL_ASSAULT"
private val THORNSTALKER_RESILIENT_GROWTH = "THORNSTALKER_RESILIENT_GROWTH"
private val THORNSTALKER_VENOMOUS_BARRAGE = "THORNSTALKER_VENOMOUS_BARRAGE"
private val pa_base = "pa_base"
private val pa_scale = "pa_scale"
private val vb_base = "vb_base"
private val vb_scale = "vb_scale"
private val rg_base = "rg_base"
private val rg_scale = "rg_scale"

fun provideThornstalkerTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "thornstalker.primal_assault" to { context, event ->
        context.useOnComplete(event, THORNSTALKER_PRIMAL_ASSAULT) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(pa_base) * (1f + 0.01f * balance.intAttribute(pa_scale) * character.attributes.body)
            ).multipledBy(if (lucky) 2f else 1f)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THORNSTALKER_PRIMAL_ASSAULT, characterId, target)))
            }
        }
    },

    "thornstalker.resilient_growth" to { context, event ->
        context.useOnComplete(event, THORNSTALKER_RESILIENT_GROWTH) { characterId, tileId, lucky ->
            game.character(characterId)?.let { character ->
                val amount = balance.floatAttribute(rg_base) * (1f + 0.01f * balance.intAttribute(rg_scale) * character.attributes.spirit)
                healCharacter(characterId, amount.toInt())
                events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                    THORNSTALKER_RESILIENT_GROWTH, characterId)))
            }
        }
    },

    "thornstalker.venomous_barrage" to { context, event ->
        context.useOnComplete(event, THORNSTALKER_VENOMOUS_BARRAGE) { characterId, tileId, lucky ->
            game.character(characterId)?.let { character ->
                val poisonAmount = balance.floatAttribute(vb_base) * (1f + 0.01f * balance.intAttribute(vb_scale) * character.attributes.spirit)
                meleeTarget(characterId).forEach { targetId ->
                    inflictPoison(targetId, poisonAmount.toInt())
                }
            }
        }
    }
)
