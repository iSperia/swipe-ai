package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val SHADOW_OF_CHAOS_ABYSSAL_REND = "SHADOW_OF_CHAOS_ABYSSAL_REND"
private const val SHADOW_OF_CHAOS_EVASIVE_SHADOWS = "SHADOW_OF_CHAOS_EVASIVE_SHADOWS"
private const val THALENDROS_DARK_AURA = "THALENDROS_DARK_AURA"
private const val THALENDROS_DARK_TILE = "THALENDROS_DARK_TILE"

private const val ar_base = "ar_base"
private const val ar_scale = "ar_scale"
private const val es_res_base = "es_res_base"
private const val es_res_scale = "es_res_scale"
private const val es_duration = "es_duration"
private const val da_tiles = "da_tiles"
private const val da_base = "da_base"
private const val da_scale = "da_scale"

fun provideShadowOfChaosAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Abyssal Rend", ru = "Разрыв Бездны"),
        skin = SHADOW_OF_CHAOS_ABYSSAL_REND,
        description = SbText(en = "Unleashing the depths of dark energy, the Shadow of Chaos inflicts a devastating blow of pure darkness, dealing substantial damage that echoes the chaos from which it originates.",
            ru = "Высвобождая глубины темной энергии, Тень Хаоса наносит сокрушительный удар чистой тьмы, нанося значительный урон, отражающий хаос, из которого она возникла."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage", ru = "Урон тьмой"),
                value = (balance.floatAttribute(ar_base) * (1f + 0.01f * balance.intAttribute(ar_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Evasive Shadows", ru = "Ускользающие Тени"),
        skin = SHADOW_OF_CHAOS_EVASIVE_SHADOWS,
        description = SbText(en = "The Shadow of Chaos melds with the surrounding darkness, rendering itself impervious to physical harm for a limited duration, evading the grasp of conventional attacks.",
            ru = "Тень Хаоса сливается с окружающей тьмой, делая себя невосприимчивой к физическому урону на ограниченное время"),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Phys. resistance buff", ru = "Увеличение физ. сопротивления"),
                value = (balance.floatAttribute(es_res_base) * (1f + 0.01f * balance.intAttribute(es_res_scale) * attributes.spirit)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Duration (turns)", ru = "Длительность (в ходах)"),
                value = balance.intAttribute(es_duration).toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Dark Aura", ru = "Тёмная Аура"),
        skin = THALENDROS_DARK_AURA,
        description = SbText(en = "Shadow of Chaos summons an aura of darkness, imbuing the target's field with malevolent energy. Skills used over these corrupted tiles trigger dark energies, causing damage to the wielder with every action taken.",
            ru = "Тень Хаоса вызывает ауру тьмы, наполняя поле цели злобной энергией. Навыки, используемые над этими испорченными плитками, активируют темную энергию, нанося урон владельцу при каждом его действии."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Dark aura tiles generated", ru = "Количество клеток тёмной ауры"),
                value = balance.intAttribute(da_tiles).toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage", ru = "Урон тьмой"),
                value = (balance.floatAttribute(da_base) * (1f + 0.01f * balance.intAttribute(da_base) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
)

fun provideShadowOfChaosTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "shadow_of_chaos.evasive_shadows" to { context, event ->
        context.useOnComplete(event, SHADOW_OF_CHAOS_EVASIVE_SHADOWS) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete

            val resistBuffs = (koef * balance.intAttribute(es_res_base) * (1f + 0.01f * balance.floatAttribute(
                es_res_scale) * character.attributes.spirit) / 100f).toInt()
            val duration = (balance.intAttribute(es_duration) * koef).toInt()

            game = game.withUpdatedCharacter(character.withAddedEffect(SbEffect(
                id = 0,
                skin = "base.resist",
                mapOf(
                    CommonKeys.Resist.PHYS to resistBuffs
                )
            )))

            events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                SHADOW_OF_CHAOS_EVASIVE_SHADOWS, character.id)))
        }
    },

    "shadow_of_chaos.dark_aura" to  { context, event ->
        context.useOnComplete(event, THALENDROS_DARK_AURA) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            allEnemies(characterId).randomOrNull()?.let { targetId ->
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            THALENDROS_DARK_AURA, characterId, targetId), SbSoundType.AOE_SPELL))

                val auraTilesCount = (balance.intAttribute(da_tiles) * koef).toInt()
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
            val thalendrosSpirit = game.characters.firstOrNull { it.skin == "MONSTER_SHADOW_OF_CHAOS" }?.attributes?.spirit ?: 0
            val damage = balance.floatAttribute(da_base) * (1f + 0.01f * balance.intAttribute(da_scale) * thalendrosSpirit)
            dealDamage(null, characterId, SbElemental(dark = damage))
            events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(THALENDROS_DARK_AURA, characterId), SbSoundType.AOE_SPELL))
        }
    },

    "shadow_of_chaos.abyssal_rend" to { context, event ->
        context.useOnComplete(event, SHADOW_OF_CHAOS_ABYSSAL_REND) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                dark = balance.floatAttribute(ar_base) * (1f + 0.01f * balance.intAttribute(ar_scale) * character.attributes.spirit),
            ).multipledBy(koef)

            rangedTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            SHADOW_OF_CHAOS_ABYSSAL_REND, characterId, target), SbSoundType.WOOSH_ATTACK))
            }
        }
    }
)
