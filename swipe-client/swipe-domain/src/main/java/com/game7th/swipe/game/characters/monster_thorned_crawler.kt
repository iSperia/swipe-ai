package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val THORNED_CRAWLER_VICIOUS_PINCERS = "THORNED_CRAWLER_VICIOUS_PINCERS"
private const val THORNED_CRAWLER_DEBILIATING_STRIKE = "THORNED_CRAWLER_DEBILIATING_STRIKE"
private const val THORNED_CRAWLER_LEECHING_SHADOWS = "THORNED_CRAWLER_LEECHING_SHADOWS"

private const val vp_base_phys = "vp_base_phys"
private const val vp_base_dark = "vp_base_dark"
private const val vp_scale_body = "vp_scale_body"
private const val vp_scale_spirit = "vp_scale_spirit"
private const val ds_base = "ds_base"
private const val ds_scale = "ds_scale"
private const val ds_tiles = "ds_tiles"
private const val ls_base = "ls_base"
private const val ls_scale = "ls_scale"
private const val ls_heal_base = "ls_heal_base"
private const val ls_heal_scale = "ls_heal_scale"

fun provideThornedCrawlerAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Vicious Pincers", ru = "Злобные клешни"),
        skin = THORNED_CRAWLER_VICIOUS_PINCERS,
        description = SbText(en = "The Thorned Crawler deploys its cruel pincers, delivering a relentless melee attack that combines physical and dark damage, rendering its victims doubly vulnerable.",
            ru = "Шипастый ползун использует свои жестокие клешни, нанося безжалостную рукопашную атаку, сочетающую физический и темный урон, делая своих жертв вдвойне уязвимыми."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(vp_base_phys) * (1f + 0.01f * balance.intAttribute(vp_scale_body) * attributes.body)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage", ru = "Урон тьмой"),
                value = (balance.floatAttribute(vp_base_dark) * (1f + 0.01f * balance.intAttribute(vp_scale_spirit) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Leeching Shadows", ru = "Вытягивающие тени"),
        skin = THORNED_CRAWLER_LEECHING_SHADOWS,
        description = SbText(en = "The Thorned Crawler's touch draws upon shadowy energies, inflicting dark damage upon its target while simultaneously restoring its own vitality through a vampiric effect.",
            ru = "Прикосновение Шипастого Ползуна притягивает теневые энергии, нанося темные повреждения своей цели и одновременно восстанавливая собственные жизненные силы за счет вампирического эффекта."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage", ru = "Урон тьмой"),
                value = (balance.floatAttribute(ls_base) * (1f + 0.01f * balance.intAttribute(ls_scale) * attributes.spirit)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Heal amount", ru = "Лечение"),
                value = (balance.floatAttribute(ls_heal_base) * (1f + 0.01f * balance.intAttribute(ls_heal_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Debiliating Strike", ru = "Ослабляющий Удар"),
        skin = THORNED_CRAWLER_DEBILIATING_STRIKE,
        description = SbText(en = "The Thorned Crawler's cunning strike not only inflicts physical damage but also generates weakness tiles upon its target's field. These tiles diminish the target's strength for each present, only disappearing with actions taken by the victim.",
            ru = "Хитрый удар Шипастого Ползуна не только наносит физический урон, но и создает плитки слабости на поле его цели. Эти плитки уменьшают силу цели за каждую такую плитку и исчезают только при действиях жертвы."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(ds_base) * (1f + 0.01f * balance.intAttribute(ds_scale) * attributes.body)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Amount of weakness tiles", ru = "Количество клеток слабости"),
                value = balance.intAttribute(ds_tiles).toString()
            ),
        )
    ),
)

fun provideThornedCrawlerTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "thorned_crawler.vicious_pincers" to { context, event ->
        context.useOnComplete(event, THORNED_CRAWLER_VICIOUS_PINCERS) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(vp_base_phys) * (1f + 0.01f * balance.intAttribute(vp_scale_body) * character.attributes.body),
                dark = balance.floatAttribute(vp_base_dark) * (1f + 0.01f * balance.intAttribute(vp_scale_spirit) * character.attributes.spirit)
            ).multipledBy(koef)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THORNED_CRAWLER_VICIOUS_PINCERS, characterId, target), SbSoundType.WOOSH_ATTACK))
            }
        }
    },

    "thorned_crawler.debiliating_stirke" to { context, event ->
        context.useOnComplete(event, THORNED_CRAWLER_DEBILIATING_STRIKE) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(ds_base) * (1f + 0.01f * balance.intAttribute(ds_scale) * character.attributes.body)
            ).multipledBy(koef)
            val tilesCount = balance.intAttribute(ds_tiles)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THORNED_CRAWLER_DEBILIATING_STRIKE, characterId, target), SbSoundType.WOOSH_ATTACK))

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
    },

    "thorned_crawler.leeching_shadows" to { context, event ->
        context.useOnComplete(event, THORNED_CRAWLER_LEECHING_SHADOWS) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                dark = balance.floatAttribute(ls_base) * (1f + 0.01f * balance.intAttribute(ls_scale) * character.attributes.spirit)
            ).multipledBy(koef)

            val healAmount = balance.floatAttribute(ls_heal_base) * (1f + 0.01f * balance.intAttribute(ls_heal_scale) * character.attributes.spirit)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THORNED_CRAWLER_DEBILIATING_STRIKE, characterId, target), SbSoundType.HEAL_MONSTER))
            }

            healCharacter(character.id, healAmount.toInt())
        }
    }
)
