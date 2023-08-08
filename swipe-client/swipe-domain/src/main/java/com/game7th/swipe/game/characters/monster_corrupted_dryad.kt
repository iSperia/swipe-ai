package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val CORRUPTED_DRYAD_ARBOREAL_FANGS = "CORRUPTED_DRYAD_ARBOREAL_FANGS"
private const val CORRUPTED_DRYAD_VILE_SIPHON = "CORRUPTED_DRYAD_VILE_SIPHON"
private const val CORRUPTED_DRYAD_SHADOWED_ANNIHILATION = "CORRUPTED_DRYAD_SHADOWED_ANNIHILATION"

private const val af_base = "af_base"
private const val af_scale = "af_scale"
private const val vs_dmg_base = "vs_dmg_base"
private const val vs_dmg_scale = "vs_dmg_scale"
private const val vs_heal_base = "vs_heal_base"
private const val vs_heal_scale = "vs_heal_scale"
private const val sa_base = "sa_base"
private const val sa_scale = "sa_scale"
private const val sa_tiles = "sa_tiles"

fun provideCorruptedDryadAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Arboreal Fangs", ru = "Древесные Клыки"),
        skin = CORRUPTED_DRYAD_ARBOREAL_FANGS,
        description = SbText(en = "The Corrupted Dryad lunges forward, striking with its menacing arboreal fangs to deliver a powerful melee blow that inflicts substantial physical damage.",
            ru = "Оскверненная дриада бросается вперед, атакуя своими грозными древесными клыками и нанося мощный удар в ближнем бою, наносящий существенный физический урон."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(af_base) * (1f + 0.01f * balance.intAttribute(af_scale) * attributes.body)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Vile Siphon", ru = "Жуткий Сифон"),
        skin = CORRUPTED_DRYAD_VILE_SIPHON,
        description = SbText(en = "The Corrupted Dryad's touch drains vitality from its target while delivering a physical attack, replenishing its own health as it inflicts damage upon its foe.",
            ru = "Прикосновение Оскверненной Дриады высасывает жизненные силы из ее цели, нанося физическую атаку, восполняя свое собственное здоровье, когда она наносит урон противнику."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(vs_dmg_base) * (1f + 0.01f * balance.intAttribute(vs_dmg_scale) * attributes.body)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Heal amount", ru = "Лечение"),
                value = (balance.floatAttribute(vs_heal_base) * (1f + 0.01f * balance.intAttribute(vs_heal_scale) * attributes.spirit)).toInt().toString()
            ),

        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Shadowed Annihilation", ru = "Теневое Уничтожение"),
        skin = CORRUPTED_DRYAD_SHADOWED_ANNIHILATION,
        description = SbText(en = "The Corrupted Dryad invokes a shadowy onslaught that randomly selects tiles on the target's field. Should the chosen tile bear a skill symbol, it is obliterated, whereas barren tiles unleash a dark barrage, inflicting damage.",
            ru = "Оскверненная дриада вызывает теневой натиск, который случайным образом выбирает плитки на поле цели. Если на выбранной плитке есть символ навыка, она уничтожается, тогда как бесплодные плитки выпускают темный шквал, нанося урон."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage, per tile", ru = "Урон тьмой, за клетку"),
                value = (balance.floatAttribute(sa_base) * (1f + 0.01f * balance.intAttribute(sa_scale) * attributes.body)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Number of tiles", ru = "Количество клеток"),
                value = balance.intAttribute(sa_tiles).toString()
            ),
        )
    ),
)

fun provideCorruptedDryadTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "corrupted_dryad.arboreal_fangs" to { context, event ->
        context.useOnComplete(event, CORRUPTED_DRYAD_ARBOREAL_FANGS) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(af_base) * (1f + 0.01f * balance.intAttribute(af_scale) * character.attributes.body)
            ).multipledBy(koef)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            CORRUPTED_DRYAD_ARBOREAL_FANGS, characterId, target), SbSoundType.WOOSH_ATTACK))
            }
        }
    },

    "corrupted_dryad.vile_siphon" to  { context, event ->
        context.useOnComplete(event, CORRUPTED_DRYAD_VILE_SIPHON) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                dark = balance.floatAttribute(vs_dmg_base) * (1f + 0.01f * balance.intAttribute(vs_dmg_scale) * character.attributes.body)
            ).multipledBy(koef)

            val healAmount = balance.floatAttribute(vs_heal_base) * (1f + 0.01f * balance.intAttribute(vs_heal_scale) * character.attributes.spirit)

            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            CORRUPTED_DRYAD_VILE_SIPHON, characterId, target), SbSoundType.WOOSH_TREE_ATTACK))
            }

            healCharacter(character.id, healAmount.toInt())
        }
    },

    "corrupted_dryad.shadowed_annihilation" to { context, event ->
        context.useOnComplete(event, CORRUPTED_DRYAD_SHADOWED_ANNIHILATION) { characterId, tileId, koef ->
            val character= game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                dark = balance.floatAttribute(sa_base) * (1f + 0.01f * balance.intAttribute(sa_scale) * character.attributes.spirit)
            ).multipledBy(koef)

            val tiles = balance.intAttribute(sa_tiles)

            allEnemies(characterId).forEach { target ->
                val positions = (0 until 25).shuffled().take(tiles)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            CORRUPTED_DRYAD_SHADOWED_ANNIHILATION, characterId, target), SbSoundType.AOE_SPELL))

                game.character(target)?.let { targetCharacter ->
                    var missedCount = 0
                    positions.forEach { p ->
                        game.character(target)?.tileAt(p % 5, p / 5, SbTile.LAYER_TILE)?.let { tile ->
                            if (tile.skill) {
                                destroyTile(target, tile.id)
                            } else {
                                missedCount++
                            }
                        } ?: missedCount++

                        events.add(SbDisplayEvent.SbShowTileFieldEffect(target, SbTileFieldDisplayEffect.TarotOverPosition(
                            skin = CORRUPTED_DRYAD_SHADOWED_ANNIHILATION,
                            x = p % 5,
                            y = p / 5
                        )))
                    }
                    if (missedCount > 0) {
                        val totalDamage = damage.multipledBy(missedCount.toFloat())
                        dealDamage(character.id, target, totalDamage)
                    }
                }
            }
        }
    }
)
