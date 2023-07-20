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
class ValerianModule {

    @Provides
    @Named("CHARACTER_VALERIAN")
    fun provideBalance(balances: Map<String, JsonObject>): JsonObject = balances["CHARACTER_VALERIAN"] ?: throw IllegalStateException("No balance")

    @Provides
    @IntoMap
    @StringKey("valerian.radiant_strike")
    fun provideRadiantStrike(@Named("CHARACTER_VALERIAN") balance: JsonObject): SbTrigger = { context, event ->
        context.useOnComplete(event, VALERIAN_RADIANT_STRIKE) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(RS_BASE) * (1f + 0.01f * balance.intAttribute(RS_SCALE) * character.attributes.body)
            ).multipledBy(if (lucky) 2f else 1f)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotSimpleAttack(
                    VALERIAN_RADIANT_STRIKE, characterId, target)))
            }
        }
    }

    @Provides
    @IntoMap
    @StringKey("valerian.luminous_beam")
    fun provideLuminousBeam(@Named("CHARACTER_VALERIAN") balance: JsonObject): SbTrigger = { context, event ->
        context.useOnComplete(event, VALERIAN_LUMINOUS_BEAM) { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                light = balance.floatAttribute(LB_BASE) * (1f + 0.01f * balance.intAttribute(LB_SCALE) * character.attributes.spirit)
            ).multipledBy(if (lucky) 2f else 1f)
            allEnemies(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotDirectedAoe(
                    VALERIAN_LUMINOUS_BEAM, characterId, 0)
                ))
            }
        }
    }

    @Provides
    @IntoMap
    @StringKey("valerian.sigil_of_renewal")
    fun provideSigilOfRenewal(@Named("CHARACTER_VALERIAN") balance: JsonObject): SbTrigger = { context, event ->
        context.useOnComplete(event, VALERIAN_SIGIL_OF_RENEWAL) { characterId, tileId, lucky ->
            var character = game.character(characterId) ?: return@useOnComplete
            val tilesCount = (if (lucky) 1 else 2) * balance.intAttribute(SOR_TILES)
            val positions = freePositions(characterId, SbTile.LAYER_TILE, tilesCount)

            positions.forEach { p ->
                val tile = SbTile(
                    id = 0,
                    skin = VALERIAN_SIGIL_OF_RENEWAL_BG,
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
                character = character.withAddedTile(tile)
                game = game.withUpdatedCharacter(character)
                events.add(SbDisplayEvent.SbCreateTile(characterId = character.id, tile = character.tiles.last().asDisplayed()))
            }
            events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                VALERIAN_SIGIL_OF_RENEWAL, characterId)))
        }

        context.triggerBackgroundLayerOnComplete(event, VALERIAN_SIGIL_OF_RENEWAL_BG) { characterId, tileId ->
            val character = game.character(characterId) ?: return@triggerBackgroundLayerOnComplete
            val healAmount = balance.floatAttribute(SOR_BASE) * (1f + 0.01f * balance.intAttribute(SOR_SCALE) * character.attributes.spirit)

            healCharacter(character.id, healAmount.toInt())
        }
    }

    @Provides
    @IntoMap
    @StringKey("valerian.divine_convergence")
    fun provideDivineConvergence(@Named("CHARACTER_VALERIAN") balance: JsonObject): SbTrigger = { context, event ->
        if (event is SbEvent.UltimateUse) {
            context.game.character(event.characterId)?.let { character ->
                if (character.skin == "CHARACTER_VALERIAN") {
                    val tiles = character.tiles.filter { it.skin == VALERIAN_SIGIL_OF_RENEWAL_BG }
                    val damagePerTile = balance.floatAttribute(DC_BASE_DMG) * (1f + 0.01f * balance.intAttribute(DC_SCALE_DMG) * character.attributes.spirit)
                    val totalDamage = tiles.count() * damagePerTile

                    val healPerTile = balance.floatAttribute(DC_BASE_HEAL) * (1f + 0.01f * balance.intAttribute(
                        DC_SCALE_HEAL_BODY) * character.attributes.body + 0.01f * balance.intAttribute(
                        DC_SCALE_HEAL_SPIRIT) * character.attributes.spirit)
                    val totalHeal = tiles.count() * healPerTile

                    context.allEnemies(character.id).forEach { enemy ->
                        context.dealDamage(character.id, enemy, SbElemental(light = totalDamage))
                    }

                    context.healCharacter(character.id, totalHeal.toInt())

                    tiles.forEach { t -> context.destroyTile(character.id, t.id) }

                    context.events.add(SbDisplayEvent.SbShowTarotEffect(effect = SbBattleFieldDisplayEffect.TarotUltimate("VALERIAN_DIVINE_CONVERGENCE")))
                    context.game.character(0)?.let { c ->
                        val cc = c.withUpdatedUltimateProgress(0)
                        context.game = context.game.withUpdatedCharacter(cc)
                        context.events.add(SbDisplayEvent.SbUpdateCharacter(cc.asDisplayed()))
                    }
                }
            }
        }
    }
}

private val VALERIAN_RADIANT_STRIKE = "VALERIAN_RADIANT_STRIKE"
private val VALERIAN_LUMINOUS_BEAM = "VALERIAN_LUMINOUS_BEAM"
private val VALERIAN_SIGIL_OF_RENEWAL = "VALERIAN_SIGIL_OF_RENEWAL"
private val VALERIAN_SIGIL_OF_RENEWAL_BG = "VALERIAN_SIGIL_OF_RENEWAL_BG"
private val VALERIAN_DIVINE_CONVERGENCE = "VALERIAN_DIVINE_CONVERGENCE"

private val RS_BASE = "radiant_strike.base_physical_damage"
private val RS_SCALE = "radiant_strike.scale_physical_damage_per_body"
private val LB_BASE = "luminous_beam.base_light_damage"
private val LB_SCALE = "luminous_beam.scale_light_damage_per_spirit"
private val SOR_TILES = "sigil_of_renewal.sigil_tiles_count"
private val SOR_BASE = "sigil_of_renewal.base_heal"
private val SOR_SCALE = "sigil_of_renewal.scale_heal_per_spirit"
private val DC_BASE_DMG = "divine_convergence.base_light_damage_per_sigil"
private val DC_SCALE_DMG = "divine_convergence.scale_light_damage_per_spirit"
private val DC_BASE_HEAL = "divine_convergence.base_heal_per_sigil"
private val DC_SCALE_HEAL_BODY = "divine_convergence.scale_heal_per_spirit"
private val DC_SCALE_HEAL_SPIRIT = "divine_convergence.scale_heal_per_body"
