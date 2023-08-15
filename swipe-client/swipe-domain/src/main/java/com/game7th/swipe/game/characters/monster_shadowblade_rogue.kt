package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private val SKIN = "MONSTER_SHADOWBLADE_ROGUE"

private val SHADOWBLADE_ROGUE_DARKSTRIKE = "SHADOWBLADE_ROGUE_DARKSTRIKE"
private val SHADOWBLADE_ROGUE_VENOMOUS_LUNGE = "SHADOWBLADE_ROGUE_VENOMOUS_LUNGE"
private val SHADOWBLADE_ROGUE_ENFEEBLING_SLASH = "SHADOWBLADE_ROGUE_ENFEEBLING_SLASH"
private val ds_base = "ds_base"
private val ds_scale = "ds_scale"
private val vl_base = "vl_base"
private val vl_scale = "vl_scale"
private val vl_poison = "vl_poison"
private val vl_poison_scale = "vl_poison_scale"
private val es_base = "es_base"
private val es_scale = "es_scale"
private val es_tiles = "es_tiles"

fun provideShadowbladeRogueAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Darkstrike", ru = "Темный удар"),
        skin = SHADOWBLADE_ROGUE_DARKSTRIKE,
        description = SbText(en = "A swift and precise melee attack infused with dark energy.",
            ru = "Быстрая и точная рукопашная атака, наполненная темной энергией."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage", ru = "Урон тьмой"),
                value = (balance.floatAttribute(ds_base) * (1f + 0.01f * balance.intAttribute(ds_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Venomous Lunge", ru = "Ядовитый выпад"),
        skin = SHADOWBLADE_ROGUE_VENOMOUS_LUNGE,
        description = SbText(en = "A vicious melee attack that deals physical damage and poisons the target, causing ongoing damage.",
            ru = "Жестокая рукопашная атака, наносящая физический урон и отравляющая цель, нанося продолжительный урон."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(vl_base) * (1f + 0.01f * balance.intAttribute(vl_scale) * attributes.body)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Poison amount", ru = "Количество яда"),
                value = (balance.floatAttribute(vl_poison) * (1f + 0.01f * balance.intAttribute(vl_poison_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Enfeebling Slash", ru = "Ослабляющий Взмах"),
        skin = SHADOWBLADE_ROGUE_ENFEEBLING_SLASH,
        description = SbText(en = "A calculated melee attack that weakens the target, reducing their damage output.",
            ru = "Просчитанная рукопашная атака, ослабляющая цель и уменьшающая наносимый ею урон."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical Damage", ru = "Физический урон"),
                value = (balance.floatAttribute(es_base) * (1f + 0.01f * balance.intAttribute(es_scale) * attributes.body)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Amount of weakness tiles", ru = "Количество клеток слабости"),
                value = balance.intAttribute(es_tiles).toString()
            ),
        )
    )
)

fun provideShadowbladeRogueTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "shadowblade_rogue.darkstrike" to { context, event ->
        context.useOnComplete(event, SHADOWBLADE_ROGUE_DARKSTRIKE) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                dark = balance.floatAttribute(ds_base) * (1f + 0.01f * balance.intAttribute(ds_scale) * character.attributes.spirit)
            ).multipledBy(koef)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            SHADOWBLADE_ROGUE_DARKSTRIKE, characterId, target), SbSoundType.WOOSH_ATTACK))
            }
        }
    },

    "shadowblade_rogue.venomous_lunge" to { context, event ->
        context.useOnComplete(event, SHADOWBLADE_ROGUE_VENOMOUS_LUNGE) { characterId, tileId, koef ->
            game.character(characterId)?.let { character ->
                val damage = SbElemental(
                    phys = balance.floatAttribute(vl_base) * (1f + 0.01f * balance.intAttribute(vl_scale) * character.attributes.body)
                ).multipledBy(koef)
                val amount = balance.floatAttribute(vl_poison) * (1f + 0.01f * balance.intAttribute(vl_poison_scale) * character.attributes.spirit) * koef
                meleeTarget(characterId).forEach { target ->
                    inflictPoison(target, amount.toInt())
                    dealDamage(characterId, target, damage)
                    events.add(
                        SbDisplayEvent.SbShowTarotEffect(
                            SbBattleFieldDisplayEffect.SimpleAttackEffect(
                                SHADOWBLADE_ROGUE_VENOMOUS_LUNGE, characterId, target), SbSoundType.WOOSH_ATTACK))
                }
            }
        }
    },

    "shadowblade_rogue.enfeebling_slash" to { context, event ->
        context.useOnComplete(event, SHADOWBLADE_ROGUE_ENFEEBLING_SLASH) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(es_base) * (1f + 0.01f * balance.intAttribute(es_scale) * character.attributes.body)
            ).multipledBy(koef)
            val tilesCount = balance.intAttribute(es_tiles)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            SHADOWBLADE_ROGUE_ENFEEBLING_SLASH, characterId, target), SbSoundType.WOOSH_ATTACK))

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
    }
)
