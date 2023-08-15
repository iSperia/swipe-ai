package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val MALACHI_ABYSSAL_WEAKNESS = "MALACHI_ABYSSAL_WEAKNESS"
private const val MALACHI_UMBRAL_ENSHROUDMENT = "MALACHI_UMBRAL_ENSHROUDMENT"
private const val MALACHI_VOID_ESSENCE_DRAIN = "MALACHI_VOID_ESSENCE_DRAIN"
private const val THALENDROS_DARK_TILE = "THALENDROS_DARK_TILE"

private const val aw_tiles = "aw_tiles"
private const val ue_base = "ue_base"
private const val ue_scale = "ue_scale"
private const val ue_tiles = "ue_tiles"
private const val ced_base = "ced_base"
private const val ced_scale = "ced_scale"
private const val ced_heal_base = "ced_heal_base"
private const val ced_heal_scale = "ced_heal_scale"

fun provideMalachiAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Abyssal Weakness", ru = "Бездонная слабость"),
        skin = MALACHI_ABYSSAL_WEAKNESS,
        description = SbText(en = "Malachi inflicts a massive weakness on the enemy, reducing their damage output significantly. This ability also destroys all tiles in its path, symbolizing the destructive nature of the darkness he commands.",
            ru = "Малахия сильно ослабляет врага, значительно уменьшая его урон. Эта способность также уничтожает все плитки на своем пути, символизируя разрушительную природу тьмы, которой он командует."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Amount of weakness tiles", ru = "Количество клеток слабости"),
                value = balance.intAttribute(aw_tiles).toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Umbral Enshroudment", ru = "Теневая оболочка"),
        skin = MALACHI_UMBRAL_ENSHROUDMENT,
        description = SbText(en = "Malachi creates massive dark aura tiles on the enemy's field, enveloping them in a realm of shadows. Using abilities over these tiles can lead to fatal consequences, as the darkness consumes the energy released.",
            ru = "Малахия создает массивные плитки темной ауры на поле противника, окутывая его царством теней. Использование способностей над этими тайлами может привести к фатальным последствиям, так как тьма поглощает высвободившуюся энергию."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Dark aura tiles generated", ru = "Количество клеток тёмной ауры"),
                value = balance.intAttribute(ue_tiles).toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage", ru = "Урон тьмой"),
                value = (balance.floatAttribute(ue_base) * (1f + 0.01f * balance.intAttribute(ue_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Void Essence Drainm", ru = "Похищение Сущности Бездны"),
        skin = MALACHI_VOID_ESSENCE_DRAIN,
        description = SbText(en = "Malachi releases a powerful surge of dark energy, dealing massive dark damage to the enemy while siphoning its essence to heal himself.",
            ru = "Малахия высвобождает мощный поток темной энергии, нанося огромный темный урон противнику и перекачивая ее сущность для исцеления себя."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage", ru = "Урон Тьмой"),
                value = (balance.floatAttribute(ced_base) * (1f + 0.01f * balance.intAttribute(ced_scale) * attributes.spirit)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Heal amount", ru = "Лечение"),
                value = (balance.floatAttribute(ced_heal_base) * (1f + 0.01f * balance.intAttribute(ced_heal_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    ),

    )

fun provideMalachiTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(

    "malachi.abyssal_weakness" to { context, event ->
        context.useOnComplete(event, MALACHI_ABYSSAL_WEAKNESS) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val tilesCount = balance.intAttribute(aw_tiles)

            meleeTarget(characterId).forEach { target ->
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            MALACHI_ABYSSAL_WEAKNESS, characterId, target), SbSoundType.WOOSH_ATTACK))

                val positions = (0 until 25).shuffled().take(tilesCount)
                positions.forEach { p ->
                    game.character(target)?.tiles?.firstOrNull { it.z == SbTile.LAYER_BACKGROUND && it.x == p % 5 && it.y == p / 5 }?.let {
                        destroyTile(target, it.id)
                    }
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

    "malachi.umbral_enshroudment" to { context, event ->
        context.useOnComplete(event, MALACHI_UMBRAL_ENSHROUDMENT) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            randomTarget(characterId)?.firstOrNull()?.let { targetId ->
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            MALACHI_UMBRAL_ENSHROUDMENT, characterId, targetId), SbSoundType.AOE_SPELL))

                val auraTilesCount = (balance.intAttribute(ue_tiles) * koef).toInt()

                val positions = (0 until 25).shuffled().take(auraTilesCount)

                positions.forEach { p ->
                    game.character(targetId)?.tiles?.firstOrNull { it.z == SbTile.LAYER_BACKGROUND && it.x == p % 5 && it.y == p / 5 }?.let {
                        destroyTile(targetId, it.id)
                    }
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
            val thalendrosSpirit = game.characters.firstOrNull { it.skin == "MONSTER_MALACHI" }?.attributes?.spirit ?: 0
            val damage = balance.floatAttribute(ue_base) * (1f + 0.01f * balance.intAttribute(ue_scale) * thalendrosSpirit)
            dealDamage(null, characterId, SbElemental(dark = damage))
            events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                MALACHI_UMBRAL_ENSHROUDMENT, characterId), SbSoundType.AOE_SPELL))
        }
    },

    "malachi.coid_essence_drain" to { context, event ->
        context.useOnComplete(event, MALACHI_VOID_ESSENCE_DRAIN) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                dark = balance.floatAttribute(ced_base) * (1f + 0.01f * balance.intAttribute(ced_scale) * character.attributes.spirit)
            ).multipledBy(koef)

            val healAmount = balance.floatAttribute(ced_heal_base) * (1f + 0.01f * balance.intAttribute(ced_heal_scale) * character.attributes.spirit)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            MALACHI_VOID_ESSENCE_DRAIN, characterId, target), SbSoundType.WOOSH_TREE_ATTACK))
            }

            healCharacter(character.id, healAmount.toInt())
        }
    }

)
