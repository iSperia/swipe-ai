package com.game7th.swipe.game.characters

import com.game7th.swipe.battle.floatAttribute
import com.game7th.swipe.battle.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import java.lang.IllegalStateException
import javax.inject.Named

@Module
class ThornstalkerModule {

    @Provides
    @Named("MONSTER_THORNSTALKER")
    fun provideBalance(balances: Map<String, JsonObject>): JsonObject = balances["MONSTER_THORNSTALKER"] ?: throw IllegalStateException("No balance")

    @Provides
    @IntoMap
    @StringKey("thornstalker.primal_assault")
    fun providePrimalAssault(@Named("MONSTER_THORNSTALKER") balance: JsonObject): SbTrigger = { context, event ->
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
    }

    @Provides
    @IntoMap
    @StringKey("thornstalker.resilient_growth")
    fun provideResilientGrowth(@Named("MONSTER_THORNSTALKER") balance: JsonObject): SbTrigger = { context, event ->
        context.useOnComplete(event, THORNSTALKER_RESILIENT_GROWTH) { characterId, tileId, lucky ->
            game.character(characterId)?.let { character ->
                val amount = balance.floatAttribute(rg_base) * (1f + 0.01f * balance.intAttribute(rg_scale) * character.attributes.spirit)
                healCharacter(characterId, amount.toInt())
                events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                    THORNSTALKER_RESILIENT_GROWTH, characterId)))
            }
        }
    }

    @Provides
    @IntoMap
    @StringKey("thornstalker.venomous_barrage")
    fun provideVenomousBarrage(@Named("MONSTER_THORNSTALKER") balance: JsonObject): SbTrigger = { context, event ->
        context.useOnComplete(event, THORNSTALKER_VENOMOUS_BARRAGE) { characterId, tileId, lucky ->
            game.character(characterId)?.let { character ->
                val poisonAmount = balance.floatAttribute(vb_base) * (1f + 0.01f * balance.intAttribute(vb_scale) * character.attributes.spirit)
                meleeTarget(characterId).forEach { targetId ->
                    inflictPoison(targetId, poisonAmount.toInt())
                }
            }
        }
    }

}

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

