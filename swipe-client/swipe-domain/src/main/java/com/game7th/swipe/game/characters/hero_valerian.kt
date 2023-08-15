package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private val VALERIAN_RADIANT_STRIKE = "VALERIAN_RADIANT_STRIKE"
private val VALERIAN_LUMINOUS_BEAM = "VALERIAN_LUMINOUS_BEAM"
private val VALERIAN_SIGIL_OF_RENEWAL = "VALERIAN_SIGIL_OF_RENEWAL"
private val VALERIAN_SIGIL_OF_RENEWAL_BG = "VALERIAN_SIGIL_OF_RENEWAL_BG"
private val VALERIAN_DIVINE_CONVERGENCE = "VALERIAN_DIVINE_CONVERGENCE"

private val RS_BASE = "rs_base"
private val RS_SCALE = "rs_scale_body"
private val LB_BASE = "lb_base"
private val LB_SCALE = "lb_scale_spirit"
private val SOR_TILES = "sor_tiles"
private val SOR_BASE = "sor_h_base"
private val SOR_SCALE = "sor_h_scale_spirit"
private val DC_BASE_DMG = "dc_dmg_base"
private val DC_SCALE_DMG = "dc_scale_dmg_spirit"
private val DC_BASE_HEAL = "dc_heal_base"
private val DC_SCALE_HEAL_BODY = "dc_scale_h_spirit"
private val DC_SCALE_HEAL_SPIRIT = "dc_scale_h_body"

fun provideValerianAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Radiant Strike", ru = "Сияющий Удар"),
        skin = VALERIAN_RADIANT_STRIKE,
        description = SbText(en = "Prince Valerian channels the brilliance of his sword to execute a melee strike that radiates pure energy, dealing focused physical damage to his target.\n\nScale: Body",
            ru = "Принц Валериан направляет сияние своего меча, чтобы нанести удар в ближнем бою, излучающий чистую энергию, нанося целенаправленный физический урон своей цели.\n\nУлучшается от: Тело"),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(RS_BASE) * (1f + 0.01f * balance.floatAttribute(RS_SCALE) * attributes.body)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Luminous Beam", ru = "Светящийся Луч"),
        skin = VALERIAN_LUMINOUS_BEAM,
        description = SbText(en = "Prince Valerian summons a luminous beam of divine light that cascades upon all adversaries, inflicting substantial damage through its radiant power.\n\nScale: Spirit",
            ru = "Принц Валериан призывает сияющий луч божественного света, который каскадом обрушивается на всех противников, нанося существенный урон своей сияющей силой.\n\nУлучшается от: Дух"),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Light damage", ru = "Урон светом"),
                value = (balance.floatAttribute(LB_BASE) * (1f + 0.01f * balance.intAttribute(LB_SCALE) * attributes.spirit)).toInt().toString(),
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Sigils of Renewal", ru = "Печати Обновления"),
        skin = VALERIAN_SIGIL_OF_RENEWAL,
        description = SbText(en = "Prince Valerian invokes sigils of renewal onto the battlefield, generating tiles that bear restorative energy. When skills are cast upon these tiles, they trigger healing for the prince.\n\nScale: Spirit",
            ru = "Принц Валериан призывает символы обновления на поле боя, создавая плитки, несущие восстанавливающую энергию. Когда на эти плитки накладываются умения, они вызывают исцеление принца.\n\nУлучшается от: Дух"),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Number of sigils generated", ru = "Количество созданных печатей"),
                value = balance.intAttribute(SOR_TILES).toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Heal amount per sigil", ru = "Размер лечения от одной печати"),
                value = (balance.intAttribute(SOR_BASE) * (1f + 0.01f * balance.intAttribute(SOR_SCALE) * attributes.spirit)).toInt().toString()
            )
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Divine Convergence", ru = "Священная Конвергенция"),
        skin = VALERIAN_DIVINE_CONVERGENCE,
        description = SbText(en = "Ultimate\n\nPrince Valerian reaches the pinnacle of his power, fusing all accumulated sigils into a divine convergence. This ultimate ability unleashes a torrent of luminous energy, dealing colossal light damage to all enemies while granting the prince substantial healing for each sigil expended.\n\nScale: Spirit, Body",
            ru = "Ультимативная способность\n\nПринц Валериан достигает вершины своего могущества, объединяя все накопленные символы в божественное слияние. Эта высшая способность высвобождает поток светящейся энергии, нанося колоссальный световой урон всем врагам и давая принцу существенное исцеление за каждый израсходованный символ.\n\nУлучшается от: Дух, Тело"),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Light damage per sigil", ru = "Урон светом от одной печати"),
                value = (balance.intAttribute(DC_BASE_DMG) * (1f + 0.01f * balance.intAttribute(SOR_SCALE) * attributes.spirit)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Heal amount per sigil", ru = "Размер лечения от одной печати"),
                value = (balance.intAttribute(DC_BASE_HEAL) * (1f + 0.01f * balance.intAttribute(DC_SCALE_HEAL_BODY) * attributes.body + 0.01f * balance.intAttribute(
                    DC_SCALE_HEAL_SPIRIT) * attributes.spirit)).toInt().toString()
            )
        )
    )
)

fun provideValerianTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(

    /**Radiant strike*/
    "valerian.radiant_strike" to { context, event ->
        context.useOnComplete(event, VALERIAN_RADIANT_STRIKE) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(RS_BASE) * (1f + 0.01f * balance.intAttribute(RS_SCALE) * character.attributes.body)
            ).multipledBy(koef)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.SimpleAttackEffect(
                    VALERIAN_RADIANT_STRIKE, characterId, target), SbSoundType.SWORD_ATTACK))
            }
        }
    },

    /** Luminous beam*/
    "valerian.luminous_beam" to { context, event ->
        context.useOnComplete(event, VALERIAN_LUMINOUS_BEAM) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                light = balance.floatAttribute(LB_BASE) * (1f + 0.01f * balance.intAttribute(LB_SCALE) * character.attributes.spirit)
            ).multipledBy(koef)
            allEnemies(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.DirectedAoeEffect(
                    VALERIAN_LUMINOUS_BEAM, characterId, 0, AoeEffectType.RAY, (0xffe07aff).toInt()), SbSoundType.MAGIC_RAY
                ))
            }
        }
    },

    /** Sigil of renewal*/
    "valerian.sigil_of_renewal" to { context, event ->
        context.useOnComplete(event, VALERIAN_SIGIL_OF_RENEWAL) { characterId, tileId, koef ->
            var character = game.character(characterId) ?: return@useOnComplete
            val tilesCount = (koef * balance.intAttribute(SOR_TILES)).toInt()
            val positions = freePositions(characterId, SbTile.LAYER_BACKGROUND, tilesCount)

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
                VALERIAN_SIGIL_OF_RENEWAL, characterId), SbSoundType.SHORT_BUFF))
        }

        context.triggerBackgroundLayerOnComplete(event, VALERIAN_SIGIL_OF_RENEWAL_BG) { characterId, tileId ->
            val character = game.character(characterId) ?: return@triggerBackgroundLayerOnComplete
            val healAmount = balance.floatAttribute(SOR_BASE) * (1f + 0.01f * balance.intAttribute(SOR_SCALE) * character.attributes.spirit)

            healCharacter(character.id, healAmount.toInt())
            events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                VALERIAN_SIGIL_OF_RENEWAL, characterId), SbSoundType.HEAL_BLOP))
        }
    },

    /**divine convergence*/
    "valerian.divine_convergence" to { context, event ->
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

                    context.events.add(SbDisplayEvent.SbShowTarotEffect(effect = SbBattleFieldDisplayEffect.UltimateEffect(
                        VALERIAN_DIVINE_CONVERGENCE, (0x9893ed).toInt()), SbSoundType.ULTIMATE))
                    context.game.character(0)?.let { c ->
                        val cc = c.withUpdatedUltimateProgress(0)
                        context.game = context.game.withUpdatedCharacter(cc)
                        context.events.add(SbDisplayEvent.SbUpdateCharacter(cc.asDisplayed()))
                    }
                }
            }
        }
    }
)

