package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
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

fun provideThornstalkerAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Primal Assault", ru = "Первобытная Атака"),
        skin = THORNSTALKER_PRIMAL_ASSAULT,
        description = SbText(en = "The Thornstalker unleashes a raw and unbridled physical assault, employing its primal strength to deal straightforward melee damage to its target.",
            ru = "Тернистый Охотник развязывает грубую и необузданную физическую атаку, используя свою первобытную силу, чтобы нанести своей цели прямой урон в ближнем бою."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(pa_base) * (1f + 0.01f * balance.intAttribute(pa_scale) * attributes.body)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Venomous Barrage", ru = "Ядовитый залп"),
        skin = THORNSTALKER_VENOMOUS_BARRAGE,
        description = SbText(en = "The Thornstalker releases a barrage of venom-infused projectiles, infecting the target with poisonous affliction. The venom spawns three toxic tiles, each inflicting dark damage upon the victim over time.",
            ru = "Тернистый охотник выпускает шквал наполненных ядом снарядов, заражая цель ядовитой болезнью. Яд порождает три токсичных плитки, каждая из которых со временем наносит темный урон жертве."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Poison amount", ru = "Количество яда"),
                value = (balance.floatAttribute(vb_base) * (1f + 0.01f * balance.intAttribute(vb_base) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Resilient Growth", ru = "Устойчивый Рост"),
        skin = THORNSTALKER_RESILIENT_GROWTH,
        description = SbText(en = "The Thornstalker taps into its affinity with the earth, invoking a profound rejuvenation that restores a substantial portion of its health, bolstering its ability to withstand combat.",
            ru = "Тернистый Охотник использует свою близость к земле, вызывая глубокое омоложение, которое восстанавливает значительную часть его здоровья, укрепляя его способность противостоять бою."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Heal amount", ru = "Лечение"),
                value = (balance.floatAttribute(rg_base) * (1f + 0.01f * balance.intAttribute(rg_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    )
)

fun provideThornstalkerTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "thornstalker.primal_assault" to { context, event ->
        context.useOnComplete(event, THORNSTALKER_PRIMAL_ASSAULT) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(pa_base) * (1f + 0.01f * balance.intAttribute(pa_scale) * character.attributes.body)
            ).multipledBy(koef)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THORNSTALKER_PRIMAL_ASSAULT, characterId, target), SbSoundType.WOOSH_ATTACK))
            }
        }
    },

    "thornstalker.resilient_growth" to { context, event ->
        context.useOnComplete(event, THORNSTALKER_RESILIENT_GROWTH) { characterId, tileId, koef ->
            game.character(characterId)?.let { character ->
                val amount = balance.floatAttribute(rg_base) * (1f + 0.01f * balance.intAttribute(rg_scale) * character.attributes.spirit) * koef
                healCharacter(characterId, amount.toInt())
                events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                    THORNSTALKER_RESILIENT_GROWTH, characterId), SbSoundType.HEAL_MONSTER))
            }
        }
    },

    "thornstalker.venomous_barrage" to { context, event ->
        context.useOnComplete(event, THORNSTALKER_VENOMOUS_BARRAGE) { characterId, tileId, koef ->
            game.character(characterId)?.let { character ->
                val poisonAmount = balance.floatAttribute(vb_base) * (1f + 0.01f * balance.intAttribute(vb_scale) * character.attributes.spirit) * koef
                meleeTarget(characterId).forEach { targetId ->
                    inflictPoison(targetId, poisonAmount.toInt())
                    events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                        THORNSTALKER_VENOMOUS_BARRAGE, characterId), SbSoundType.WOOSH_TREE_ATTACK))
                }
            }
        }
    }
)
