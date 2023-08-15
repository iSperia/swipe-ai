package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject
import kotlin.random.Random

private const val XALITHAR_FROZEN_IMPACT = "XALITHAR_FROZEN_IMPACT"
private const val XALITHAR_MIRRORWEAVE = "XALITHAR_MIRRORWEAVE"
private const val XALITHAR_CRYSTALINE_CATACLYSM = "XALITHAR_CRYSTALINE_CATACLYSM"

private const val fi_phys_base = "fi_phys_base"
private const val fi_cold_base = "fi_cold_base"
private const val fi_phys_scale = "fi_phys_scale"
private const val fi_cold_scale = "fi_cold_scale"
private const val m_percent = "m_percent"
private const val cc_per_tile_base = "cc_per_tile_base"
private const val cc_per_tile_scale = "cc_per_tile_scale"

fun provideXalitharAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Frozen Impact", ru = "Ледяной удар"),
        skin = XALITHAR_FROZEN_IMPACT,
        description = SbText(en = "Xalithar summons a chilling storm of frozen shards to execute a powerful melee attack. This attack inflicts considerable physical and cold damage upon impact.",
            ru = "Ксалитар призывает ледяную бурю замороженных осколков, чтобы провести мощную атаку в ближнем бою. Эта атака наносит значительный физический урон и урон от холода при ударе."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(fi_phys_base) * (1f + 0.01f * balance.intAttribute(fi_phys_scale) * attributes.body)).toInt().toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Cold damage", ru = "Урон холодом"),
                value = (balance.floatAttribute(fi_cold_base) * (1f + 0.01f * balance.intAttribute(fi_cold_scale) * attributes.mind)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Mirrorweave", ru = "Зеркальное плетение"),
        skin = XALITHAR_MIRRORWEAVE,
        description = SbText(en = "Xalithar harnesses a crystalline shield, reflecting a portion of incoming damage back towards the assailant. This passive ability allows Xalithar to concentrate energy, then redirect the force to counteract the harm sustained",
            ru = "Ксалитар использует кристаллический щит, отражающий часть входящего урона обратно в нападавшего. Эта пассивная способность позволяет Ксалитару концентрировать энергию, а затем перенаправлять ее для противодействия полученному урону."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "% of damage reflected", ru = "% отражаемого урона"),
                value = balance.intAttribute(m_percent).toString()
            )
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Crystalline Cataclysm", ru = "Кристаллический Катаклизм"),
        skin = XALITHAR_CRYSTALINE_CATACLYSM,
        description = SbText(en = "Xalithar commands a devastating onslaught, selecting a row and column on the enemy's field. The crystal behemoth shatters everything in its path, obliterating tiles and skills alike. This destruction culminates in a cold outburst, dealing damage based on the number of demolished symbols.",
            ru = "Ксалитар командует сокрушительным натиском, выбирая ряд и столбец на вражеском поле. Хрустальный бегемот крушит все на своем пути, стирая плитки и умения. Кульминацией этого разрушения является холодная вспышка, наносящая урон в зависимости от количества уничтоженных символов."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Cold damage, per destroyed tile or symbol", ru = "Урон холодом, за каждый уничтоженный символ или плитку"),
                value = (balance.floatAttribute(cc_per_tile_base) * (1f + 0.01f * balance.intAttribute(cc_per_tile_scale) * attributes.mind)).toInt().toString()
            )
        )
    ),

    )

fun provideXalitharTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(

    "xalithar.frozen_impact" to { context, event ->
        context.useOnComplete(event, XALITHAR_FROZEN_IMPACT) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(fi_phys_base) * (1f + 0.01f * balance.intAttribute(fi_phys_scale) * character.attributes.body),
                cold = balance.floatAttribute(fi_cold_base) * (1f + 0.01f * balance.intAttribute(fi_cold_scale) * character.attributes.mind)
            ).multipledBy(koef)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            XALITHAR_FROZEN_IMPACT, characterId, target), SbSoundType.WOOSH_TREE_ATTACK))
            }
        }
    },

    "xalithar.mirrorweave" to { context, event ->
        context.onDamage(event) { event ->
            if (event.sourceId != null) {
                val fullMirrorWeaveTile = game.character(event.characterId)?.tiles?.filter { it.progress == it.maxProgress && it.skin == XALITHAR_MIRRORWEAVE }?.randomOrNull()
                fullMirrorWeaveTile?.let { tile ->
                    destroyTile(event.characterId, tile.id)
                    val reflectedDamage = event.damage.multipledBy(balance.floatAttribute(m_percent) / 100f)
                    events.add(
                        SbDisplayEvent.SbShowTarotEffect(
                            SbBattleFieldDisplayEffect.SimpleAttackEffect(
                                XALITHAR_FROZEN_IMPACT, event.characterId, event.sourceId), SbSoundType.WOOSH_TREE_ATTACK))
                    dealDamage(event.characterId, event.sourceId, reflectedDamage)
                }
            }
        }
    },

    "xalithar.crystalline_cataclysm" to { context, event ->
        context.useOnComplete(event, XALITHAR_CRYSTALINE_CATACLYSM) { characterId, tileId, koef ->
            val character= game.character(characterId) ?: return@useOnComplete

            rangedTarget(characterId).forEach { target ->
                val position = Random.nextInt(25)
                val x = position % 5
                val y = position / 5

                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            XALITHAR_CRYSTALINE_CATACLYSM, characterId, target), SbSoundType.AOE_SPELL))

                var tilesCount = 0
                game.character(target)?.let { targetCharacter ->
                    targetCharacter.tiles.filter { (it.x == x || it.y == y) && (it.skill || it.z == SbTile.LAYER_BACKGROUND) }.forEach { tile ->
                        tilesCount++
                        destroyTile(target, tile.id)
                    }
                }
                (0 until 25).forEach { p ->
                    if (p % 5==x || p / 5==y) {
                        events.add(SbDisplayEvent.SbShowTileFieldEffect(target, SbTileFieldDisplayEffect.TarotOverPosition(
                            XALITHAR_CRYSTALINE_CATACLYSM, p % 5, p / 5)))
                    }
                }

                val damage = SbElemental(
                    cold = balance.floatAttribute(cc_per_tile_base) * (1f + 0.01f * balance.intAttribute(
                        cc_per_tile_scale) * character.attributes.mind)
                ).multipledBy(koef * tilesCount)
                dealDamage(characterId, target, damage)
            }
        }
    }

)
