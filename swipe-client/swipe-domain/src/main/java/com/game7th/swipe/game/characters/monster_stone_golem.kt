package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val STONE_GOLEM_STONE_SMASH = "STONE_GOLEM_STONE_SMASH"
private const val STONE_GOLEM_PETRIFYING_STRIKE = "STONE_GOLEM_PETRIFYING_STRIKE"
private const val STONE_GOLEM_CRUSHING_MOMENTUM = "STONE_GOLEM_CRUSHING_MOMENTUM"
private const val COMMON_STUN = "COMMON_STUN"

private const val ss_base = "ss_base"
private const val ss_scale = "ss_scale"
private const val ps_base = "ps_base"
private const val ps_scale = "ps_scale"
private const val ps_tiles = "ps_tiles"
private const val cm_base = "cm_base"
private const val cm_scale = "cm_scale"
private const val cm_per_tile = "cm_per_tile"
private const val cm_per_tile_scale = "cm_per_tile_scale"

fun provideStoneGolemAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Stone Smash", ru = "Удар камнем"),
        skin = STONE_GOLEM_STONE_SMASH,
        description = SbText(en = "The Stone Golem delivers a monumental physical strike, bringing its massive weight crashing down to deal substantial physical damage to its target.",
            ru = "Каменный голем наносит монументальный физический удар, сбрасывая свой массивный вес и нанося существенный физический урон своей цели."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(ss_base) * (1f + 0.01f * balance.intAttribute(ss_scale) * attributes.body)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Petrifying Strike", ru = "Окаменевший удар"),
        skin = STONE_GOLEM_PETRIFYING_STRIKE,
        description = SbText(en = "With an impactful blow, the Stone Golem shatters the earth beneath its enemy's feet, leaving behind symbols of stone that momentarily immobilize the target in a state of petrification.",
            ru = "Мощным ударом каменный голем разбивает землю под ногами врага, оставляя после себя каменные символы, которые на мгновение обездвиживают цель в состоянии окаменения."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(ps_base) * (1f + 0.01f * balance.intAttribute(ps_scale) * attributes.body)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Amount of stun symbols", ru = "Количество символов оглушения"),
                value = (balance.floatAttribute(ps_tiles)).toInt().toString()
            ),

            )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Crushing Momentum", ru = "Сокрушительный импульс"),
        skin = STONE_GOLEM_CRUSHING_MOMENTUM,
        description = SbText(en = "The Stone Golem harnesses the momentum of its attack to amplify its power, inflicting additional physical damage for each stunned symbol present on the target's field.",
            ru = "Каменный голем использует импульс своей атаки для усиления своей силы, нанося дополнительный физический урон за каждый оглушенный символ, присутствующий на поле цели."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(cm_base) * (1f + 0.01f * balance.intAttribute(cm_scale) * attributes.body)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage, per stun symbol", ru = "Физический урон, за символ оглушения"),
                value = (balance.floatAttribute(cm_per_tile) * (1f + 0.01f * balance.intAttribute(cm_per_tile_scale) * attributes.body)).toInt().toString()
            )
        )
    ),
)

fun provideStoneGolemTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "stone_golem.stone_smash" to { context, event ->
        context.useOnComplete(event, STONE_GOLEM_STONE_SMASH) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(ss_base) * (1f + 0.01f * balance.intAttribute(ss_scale) * character.attributes.body)
            ).multipledBy(koef)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            STONE_GOLEM_STONE_SMASH, characterId, target), SbSoundType.WOOSH_ATTACK))
            }
        }
    },

    "stone_golem.petrifying_strike" to  { context, event ->
        context.useOnComplete(event, STONE_GOLEM_PETRIFYING_STRIKE) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                cold = balance.floatAttribute(ps_base) * (1f + 0.01f * balance.intAttribute(ps_scale) * character.attributes.body)
            ).multipledBy(koef)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            STONE_GOLEM_PETRIFYING_STRIKE, characterId, target), SbSoundType.WOOSH_TREE_ATTACK))

                game.character(target)?.let { target ->
                    val freePositions = (0 until 25).filter { p -> target.tiles.none { it.z == SbTile.LAYER_TILE && it.x == p%5 && it.y == p/5 } }.shuffled()
                    val positions = (balance.intAttribute(ps_tiles))
                    var updatedTarget = target
                    if (freePositions.size >= positions) {
                        freePositions.take(positions).forEach { p ->
                            val tile = SbTile(
                                id = 0,
                                skin = "COMMON_STUN",
                                x = p % 5,
                                y = p / 5,
                                z = SbTile.LAYER_TILE,
                                mobility = 1,
                                mergeStrategy = SbTileMergeStrategy.SIMPLE,
                                progress = 1,
                                maxProgress = positions,
                                maxEffectId = 0,
                                skill = false,
                                effects = emptyList()
                            )
                            updatedTarget = updatedTarget.withAddedTile(tile)
                            events.add(SbDisplayEvent.SbCreateTile(characterId = target.id, tile = updatedTarget.tiles.last().asDisplayed()))
                        }
                    }
                    game = game.withUpdatedCharacter(updatedTarget)
                }
            }
        }
    },

    "stone_golem.crushing_momentum" to { context, event ->
        context.useOnComplete(event, STONE_GOLEM_CRUSHING_MOMENTUM) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(cm_base) * (1f + 0.01f * balance.intAttribute(cm_scale) * character.attributes.body)
            ).multipledBy(koef)

            meleeTarget(characterId).forEach { target ->
                game.character(target)?.let { character ->
                    val countStuns = character.tiles.count { it.skin == "COMMON_STUN" }
                    val extraDamage = balance.floatAttribute(cm_per_tile) * countStuns * (1f + 0.01f * balance.intAttribute(
                        cm_per_tile_scale) * character.attributes.body)

                    dealDamage(characterId, target, damage.copy(phys = damage.phys + extraDamage))
                    events.add(
                        SbDisplayEvent.SbShowTarotEffect(
                            SbBattleFieldDisplayEffect.SimpleAttackEffect(
                                STONE_GOLEM_STONE_SMASH, characterId, target), SbSoundType.WOOSH_ATTACK))
                }
            }
        }
    }
)
