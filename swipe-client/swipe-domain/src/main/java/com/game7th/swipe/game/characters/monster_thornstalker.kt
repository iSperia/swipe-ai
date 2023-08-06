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
        description = SbText(en = "Melee attack.\nDeals physical damage", ru = "Рукопашная атака\nНаносит физический урон"),
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
        description = SbText(en = "Melee attack\nInflicts poison on target (3 poison symbols are generated on target's field, after each swipe character is dealt dark damage while there is poison symbol on field)\n",
            ru = "Рукопашная атака\nОтравляет цель (3 символа яда создаются на вражеском поле, после каждого хода персонаж получает урон тьмой, пока на поле есть хотя бы один символ яда)"),
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
        description = SbText(en = "Heals the monster", ru = "Исцеляет монстра"),
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
