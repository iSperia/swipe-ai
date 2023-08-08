package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val CRYSTAL_GUARDIAN_CRYSTAL_FROSTSTRIKE = "CRYSTAL_GUARDIAN_CRYSTAL_FROSTSTRIKE"
private const val CRYSTAL_GUARDIAN_GLACIAL_BIND = "CRYSTAL_GUARDIAN_GLACIAL_BIND"
private const val CRYSTAL_GUARDIAN_CRYSTAL_WARD = "CRYSTAL_GUARDIAN_CRYSTAL_WARD"

private const val cf_base = "cf_base"
private const val cf_scale = "cf_scale"
private const val gb_dmg_base = "gb_dmg_base"
private const val gb_dmg_scale = "gb_dmg_scale"
private const val gb_freeze_amount = "gb_freeze_amount"
private const val cw_res_base = "cw_res_base"
private const val cw_res_scale = "cw_res_scale"
private const val cw_duration = "cw_duration"

fun provideCrystalGuardianAttributes(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Crystal Froststrike", ru = "Кристальный Ледяной Удар"),
        skin = CRYSTAL_GUARDIAN_CRYSTAL_FROSTSTRIKE,
        description = SbText(en = "The Crystal Guardian channels icy energies into a chilling projectile, striking enemies with a combination of cold and physical damage.",
            ru = "Кристальный страж направляет ледяную энергию в леденящий снаряд, поражая врагов сочетанием холода и физического урона."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Cold damage", ru = "Урон холодом"),
                value = (balance.floatAttribute(cf_base) * (1f + 0.01f * balance.intAttribute(cf_scale) * (attributes.mind + attributes.body))).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Glacial Bind", ru = "Ледяное Сковывание"),
        skin = CRYSTAL_GUARDIAN_GLACIAL_BIND,
        description = SbText(en = "The Crystal Guardian releases an ethereal frost wave that freezes the designated symbols on the target's field, immobilizing them momentarily with a shroud of ice.",
            ru = "Кристальный страж выпускает эфирную ледяную волну, которая замораживает обозначенные символы на поле цели, на мгновение обездвиживая их ледяной пеленой."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Cold damage", ru = "Урон холодом"),
                value = (balance.floatAttribute(gb_dmg_base) * (1f + 0.01f * balance.intAttribute(gb_dmg_scale) * attributes.mind)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Amount of symbols frozen", ru = "Количество замороженных символов"),
                value = (balance.floatAttribute(gb_freeze_amount)).toInt().toString()
            ),

            )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Crystal Ward", ru = "Кристальный Щит"),
        skin = CRYSTAL_GUARDIAN_CRYSTAL_WARD,
        description = SbText(en = "The Crystal Guardian raises a shimmering crystal shield, bolstering its resistances against all but fire-based attacks for a limited number of turns.",
            ru = "Хрустальный страж поднимает мерцающий кристальный щит, повышая его сопротивляемость всем атакам, кроме огненных, на ограниченное количество ходов"),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Resistance buff", ru = "Увеличение сопротивлений"),
                value = (balance.floatAttribute(cw_res_base) * (1f + 0.01f * balance.intAttribute(cw_res_scale) * attributes.spirit)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Duration (turns)", ru = "Длительность (в ходах)"),
                value = balance.intAttribute(cw_duration).toString()
            ),
        )
    ),
)

fun provideCrystalGuardianTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(
    "crystal_guardian.crystal_froststrike" to { context, event ->
        context.useOnComplete(event, CRYSTAL_GUARDIAN_CRYSTAL_FROSTSTRIKE) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                cold = balance.floatAttribute(cf_base) * (1f + 0.01f * balance.intAttribute(cf_scale) * (character.attributes.body + character.attributes.mind)),
                phys = balance.floatAttribute(cf_base) * (1f + 0.01f * balance.intAttribute(cf_scale) * (character.attributes.body + character.attributes.mind))
            ).multipledBy(koef)

            rangedTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            CRYSTAL_GUARDIAN_CRYSTAL_FROSTSTRIKE, characterId, target), SbSoundType.WOOSH_ATTACK))
            }
        }
    },

    "crystal_guardian.glacial_bind" to  { context, event ->
        context.useOnComplete(event, CRYSTAL_GUARDIAN_GLACIAL_BIND) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                cold = balance.floatAttribute(gb_dmg_base) * (1f + 0.01f * balance.intAttribute(gb_dmg_scale) * character.attributes.mind)
            ).multipledBy(koef)

            rangedTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            CRYSTAL_GUARDIAN_GLACIAL_BIND, characterId, target), SbSoundType.WOOSH_TREE_ATTACK))

                game.character(target)?.let { target ->
                    val positions = (balance.intAttribute(gb_freeze_amount) * koef).toInt()
                    val tilesToFreeze = target.tiles.filter { it.z == SbTile.LAYER_TILE && it.effects.none { it.skin == SbEffect.FREEZE } }.shuffled().take(positions)
                    val updatedTarget = target.copy(tiles = target.tiles.map { tile ->
                        tilesToFreeze.firstOrNull { it.id == tile.id }?.let {
                            events.add(SbDisplayEvent.SbDestroyTile(target.id, it.z, it.id))
                            val newTile = tile.withAddedEffect(SbEffect(0, SbEffect.FREEZE, emptyMap())).copy(mobility = 0)
                            events.add(SbDisplayEvent.SbCreateTile(target.id, newTile.asDisplayed()))
                            newTile
                        } ?: tile
                    })
                    game = game.withUpdatedCharacter(updatedTarget)
                }
            }
        }
    },

    "crystal_guardian.crystal_ward" to { context, event ->
        context.useOnComplete(event, CRYSTAL_GUARDIAN_CRYSTAL_WARD) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete

            val resistBuffs = (koef * balance.intAttribute(cw_res_base) * (1f + 0.01f * balance.floatAttribute(cw_res_scale) * character.attributes.mind) / 100f).toInt()
            val duration = (balance.intAttribute(cw_duration) * koef).toInt()

            game = game.withUpdatedCharacter(character.withAddedEffect(SbEffect(
                id = 0,
                skin = "base.resist",
                mapOf(
                    CommonKeys.Resist.PHYS to resistBuffs,
                    CommonKeys.Resist.COLD to resistBuffs,
                    CommonKeys.Resist.DARK to resistBuffs,
                    CommonKeys.Resist.LIGHT to resistBuffs,
                    CommonKeys.Resist.SHOCK to resistBuffs,
                    CommonKeys.DURATION to duration,
                )
            )))

            events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                CRYSTAL_GUARDIAN_CRYSTAL_WARD, character.id)))
        }
    }
)
