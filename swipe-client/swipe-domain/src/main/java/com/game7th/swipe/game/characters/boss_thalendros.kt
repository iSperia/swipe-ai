package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private const val THALENDROS_THORN_WHIP = "THALENDROS_THORN_WHIP"
private const val THALENDROS_EARTHQUAKE_SLAM = "THALENDROS_EARTHQUAKE_SLAM"
private const val THALENDROS_CORRUPTED_ROOTS = "THALENDROS_CORRUPTED_ROOTS"
private const val THALENDROS_DARK_AURA = "THALENDROS_DARK_AURA"
private const val THALENDROS_DARK_TILE = "THALENDROS_DARK_TILE"

private const val tw_base = "tw_base"
private const val tw_scale = "tw_scale"
private const val es_tiles = "es_tiles"
private const val es_damage = "es_damage"
private const val es_scale = "es_scale"
private const val da_tiles = "da_tiles"
private const val da_base = "da_base"
private const val da_scale = "da_scale"

fun provideThalendrosAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Thorn Whip", ru = "Терновый Кнут"),
        skin = THALENDROS_THORN_WHIP,
        description = SbText(en = "Thalendros conjures thorny vines to lash out at the target, delivering a punishing physical strike that pierces through defenses, inflicting significant physical damage.",
            ru = "Талендрос призывает колючие лозы, которые обрушиваются на цель, нанося карательный физический удар, который пробивает защиту и наносит значительный физический урон."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Physical damage", ru = "Физический урон"),
                value = (balance.floatAttribute(tw_base) * (1f + 0.01f * balance.intAttribute(tw_scale) * attributes.body)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Corrupted Roots", ru = "Оскверенные Корни"),
        skin = THALENDROS_EARTHQUAKE_SLAM,
        description = SbText(en = "Unleashing a forceful quake, Thalendros plants corrupted roots across the target's field. When the corrupted roots intertwine, his next devastating earthquake slam deals amplified damage for each root present.",
            ru = "Вызвав мощное землетрясение, Талендрос сажает оскверненные корни на поле цели. Когда испорченные корни переплетаются, его следующий разрушительный удар землетрясением наносит усиленный урон за каждый присутствующий корень."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Corrupted roots symbols amount", ru = "Количество символов оскверненных корней"),
                value = balance.intAttribute(es_tiles).toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage per corrupted roots tile", ru = "Урон тьмой за каждый символ оскверненных корней"),
                value = (balance.floatAttribute(es_damage) * (1f + 0.01f * balance.intAttribute(es_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Dark Aura", ru = "Тёмная Аура"),
        skin = THALENDROS_DARK_AURA,
        description = SbText(en = "Thalendros summons an aura of darkness, imbuing the target's field with malevolent energy. Skills used over these corrupted tiles trigger dark energies, causing damage to the wielder with every action taken.",
            ru = "Талендрос вызывает ауру тьмы, наполняя поле цели злобной энергией. Навыки, используемые над этими испорченными плитками, активируют темную энергию, нанося урон владельцу при каждом его действии."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Dark aura tiles generated", ru = "Количество клеток тёмной ауры"),
                value = balance.intAttribute(da_tiles).toString()
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Dark damage", ru = "Урон тьмой"),
                value = (balance.floatAttribute(da_base) * (1f + 0.01f * balance.intAttribute(da_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    ),

)

fun provideThalendrosTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(

    "thalendros.thorn_whip" to { context, event ->
        context.useOnComplete(event, THALENDROS_THORN_WHIP) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                phys = balance.floatAttribute(tw_base) * (1f + 0.01f * balance.intAttribute(tw_scale) * character.attributes.body)
            ).multipledBy(koef)
            meleeTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THALENDROS_THORN_WHIP, characterId, target), SbSoundType.WOOSH_TREE_ATTACK))
            }
        }
    },

    "thalendros.earthquake_slam" to { context, event ->
        context.useOnComplete(event, THALENDROS_EARTHQUAKE_SLAM) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            allEnemies(characterId).randomOrNull()?.let { targetId ->
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
                            THALENDROS_EARTHQUAKE_SLAM, characterId, targetId
                        ),
                        SbSoundType.WOOSH_TREE_ATTACK
                    )
                )

                val rootsCount = game.character(targetId)!!.tiles.count { it.skin == THALENDROS_CORRUPTED_ROOTS }
                if (rootsCount <= 0) {
                    val tiles = balance.intAttribute(es_tiles)
                    val positions = (0 until 25).shuffled().take(tiles)
                    positions.forEach { p ->
                        val x = p % 5
                        val y = p / 5
                        game.character(targetId)?.tileAt(x, y, SbTile.LAYER_TILE)?.let { tileToDelete ->
                            destroyTile(targetId, tileToDelete.id)
                        }
                        val tile = SbTile(
                            id = 0,
                            skin = THALENDROS_CORRUPTED_ROOTS,
                            x = x,
                            y = y,
                            z = SbTile.LAYER_TILE,
                            skill = false,
                            mobility = 5,
                            mergeStrategy = SbTileMergeStrategy.SIMPLE,
                            progress = 1,
                            maxProgress = tiles,
                            maxEffectId = 0,
                            effects = emptyList()
                        )
                        game.character(targetId)?.let {
                            val character = it.withAddedTile(tile)
                            game = game.withUpdatedCharacter(character)
                            events.add(SbDisplayEvent.SbCreateTile(targetId, character.tiles.last().asDisplayed()))
                        }
                    }
                } else {
                    val damage = SbElemental(
                        dark = rootsCount * balance.floatAttribute(es_damage) * (1f + 0.01f * balance.intAttribute(
                            es_scale
                        ) * character.attributes.spirit)
                    ).multipledBy(koef)
                    dealDamage(characterId, targetId, damage)
                }
            }
        }

        context.useOnComplete(event, THALENDROS_CORRUPTED_ROOTS) { _, _, _ -> }
    },

    "thalendros.dark_aura" to { context, event ->
        context.useOnComplete(event, THALENDROS_DARK_AURA) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            allEnemies(characterId).randomOrNull()?.let { targetId ->
                events.add(
                    SbDisplayEvent.SbShowTarotEffect(
                        SbBattleFieldDisplayEffect.TarotSimpleAttack(
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
            val thalendrosSpirit = game.characters.firstOrNull { it.skin == "MONSTER_THALENDROS" }?.attributes?.spirit ?: 0
            val damage = balance.floatAttribute(da_base) * (1f + 0.01f * balance.intAttribute(da_scale) * thalendrosSpirit)
            dealDamage(null, characterId, SbElemental(dark = damage))
            events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(THALENDROS_DARK_AURA, characterId), SbSoundType.AOE_SPELL))
        }
    }

)
