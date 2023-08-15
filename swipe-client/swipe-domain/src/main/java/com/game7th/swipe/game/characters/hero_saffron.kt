package com.game7th.swipe.game.characters

import com.game7th.swipe.SbText
import com.game7th.swipe.game.floatAttribute
import com.game7th.swipe.game.intAttribute
import com.game7th.swipe.game.*
import com.google.gson.JsonObject

private val SAFFRON_IGINITE_INSPIRATION = "SAFFRON_IGINITE_INSPIRATION"
private val SAFFRON_INFERNO_STRIKE = "SAFFRON_INFERNO_STRIKE"
private val SAFFRON_BLAZING_BACKDRAFT = "SAFFRON_BLAZING_BACKDRAFT"
private val SAFFRON_PHOENIX_REBIRTH = "SAFFRON_PHOENIX_REBIRTH"

private val ii_tiles = "ii_tiles"
private val ii_scale = "ii_scale"
private val is_base = "is_base"
private val is_scale = "is_scale"
private val bb_base = "bb_base"
private val bb_scale = "bb_scale"
private val bb_back_percent = "bb_back_percent"
private val pb_percent = "pb_percent"

fun provideSaffronAbilities(balance: JsonObject, attributes: CharacterAttributes) = listOf(
    FrontMonsterAbility(
        title = SbText(en = "Inspiration Spark", ru = "Искра Вдохновения"),
        skin = SAFFRON_IGINITE_INSPIRATION,
        description = SbText(en = "Saffron ignites her inner creativity, conjuring new tiles on her field that burn with potential. These tiles hold the power to fuel her other abilities.\n\nScale: Spirit",
            ru = "Шафран зажигает свое внутреннее творчество, создавая на своем поле новые плитки, полные потенциала. Эти плитки обладают силой, подпитывающей другие ее способности.\n\nУлучшается от: Дух"),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Amount of symbols generated", ru = "Количество сгенерированных символов"),
                value = (balance.floatAttribute(ii_tiles) * (1f + 0.01f * balance.floatAttribute(ii_scale) * attributes.spirit)).toInt().toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Inferno Strike", ru = "Инфернальный удар"),
        skin = SAFFRON_INFERNO_STRIKE,
        description = SbText(en = "Focusing her fiery essence, Saffron targets the lowest maximum health enemy, delivering a scorching strike that deals moderate fire damage. Her flames seek out vulnerabilities, ensuring the attack is always impactful\n\nScale: Mind",
            ru = "Сосредоточив свою огненную сущность, Шафран нацеливается на врага с наименьшим максимальным здоровьем, нанося обжигающий удар, наносящий умеренный урон от огня. Ее пламя выискивает уязвимые места, гарантируя, что атака всегда будет результативной.\n\nУлучшается от: Разум"),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Fire damage", ru = "Урон огнём"),
                value = (balance.floatAttribute(is_base) * (1f + 0.01f * balance.intAttribute(is_scale) * attributes.mind)).toInt().toString(),
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Blazing Breath", ru = "Пылающее Дыхание"),
        skin = SAFFRON_BLAZING_BACKDRAFT,
        description = SbText(en = "Saffron launches a fire-laden strike at the closest target, creating a fiery backdraft that engulfs the enemy behind. The main target takes immediate fire damage, while the secondary target endures residual damage from the scorching aftermath.\n\nScale: Mind",
            ru ="Шафран наносит огненный удар по ближайшей цели, создавая огненную обратную тягу, которая поглощает врага позади. Основная цель получает немедленный урон от огня, в то время как второстепенная цель получает остаточный урон от палящих последствий.\n\nУлучшается от: Разум"),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Fire damage", ru = "Урон огнём"),
                value = (balance.floatAttribute(bb_base) * (1f + 0.01f * balance.intAttribute(bb_scale) * attributes.mind)).toInt().toString(),
            ),
            FrontMonsterAbilityField(
                title = SbText(en = "Damage to second target, %", ru = "Урон второй цели, %"),
                value = balance.intAttribute(bb_back_percent).toString()
            ),
        )
    ),
    FrontMonsterAbility(
        title = SbText(en = "Phoenix Rebirth", ru = "Возрождение Феникса"),
        skin = SAFFRON_PHOENIX_REBIRTH,
        description = SbText(en = "Ultimate\n\nHarnessing her phoenix heritage, Saffron taps into the primal energies within her. When her health reaches a critical point, her ultimate ability resurrects her from the brink of death, rejuvenating her with a portion of health. This phoenix-like revival enables her to continue the fight, demonstrating her indomitable spirit.",
            ru = "Используя свое наследие феникса, Шафран подключается к первобытной энергии внутри себя. Когда ее здоровье достигает критической точки, ее высшая способность воскрешает ее с грани смерти, омолаживая ее порцией здоровья. Это возрождение, подобное фениксу, позволяет ей продолжать борьбу, демонстрируя свой неукротимый дух."),
        fields = listOf(
            FrontMonsterAbilityField(
                title = SbText(en = "Health restored, %", ru = "Восстанавливаемое здоровье, %"),
                value = balance.intAttribute(pb_percent).toString()
            )
        )
    )
)

fun provideSaffronTriggers(balance: JsonObject): Map<String, SbTrigger> = mapOf(

    "saffron.ignite_inspiration" to { context, event ->
        context.useOnComplete(event, SAFFRON_IGINITE_INSPIRATION) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val tilesAmount = (balance.floatAttribute(ii_tiles) * (1f + 0.01f * balance.floatAttribute(ii_scale) * character.attributes.spirit)).toInt()

            generateTiles(characterId, tilesAmount)
            events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.TarotStatic(
                SAFFRON_IGINITE_INSPIRATION, characterId
            )))
        }
    },

    "saffron.inferno_strike" to { context, event ->
        context.useOnComplete(event, SAFFRON_INFERNO_STRIKE) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                fire = balance.floatAttribute(is_base) * (1f + 0.01f * balance.intAttribute(is_scale) * character.attributes.mind)
            ).multipledBy(koef)
            rangedTarget(characterId).forEach { target ->
                dealDamage(characterId, target, damage)
                events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.SimpleAttackEffect(
                    SAFFRON_INFERNO_STRIKE, characterId, target), SbSoundType.AOE_SPELL))
            }
        }
    },

    "saffron.blazing_backdraft" to { context, event ->
        context.useOnComplete(event, SAFFRON_BLAZING_BACKDRAFT) { characterId, tileId, koef ->
            val character = game.character(characterId) ?: return@useOnComplete
            val damage = SbElemental(
                fire = balance.floatAttribute(bb_base) * (1f + 0.01f * balance.intAttribute(bb_scale) * character.attributes.mind)
            ).multipledBy(koef)
            val secondaryDamage = damage.multipledBy(balance.floatAttribute(bb_back_percent) * 0.01f)
            val allEnemies = allEnemies(characterId)
            allEnemies(characterId).lastOrNull()?.let { target ->
                dealDamage(characterId, target, damage)
                events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.SimpleAttackEffect(
                    SAFFRON_INFERNO_STRIKE, characterId, target), SbSoundType.AOE_SPELL))
                if (allEnemies.size > 1) {
                    allEnemies[allEnemies.size - 2].let { secondTarget ->
                        dealDamage(characterId, secondTarget, secondaryDamage)
                        events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.SimpleAttackEffect(
                            SAFFRON_INFERNO_STRIKE, characterId, secondTarget), SbSoundType.AOE_SPELL))
                    }
                }
            }
        }
    },

    "saffron.phoenix_rebirth" to { context, event ->
        if (event is SbEvent.CharacterPreDeath) {
            context.game.character(event.characterId)?.let { deadCharacter ->
                if (deadCharacter.skin == "CHARACTER_SAFFRON" && deadCharacter.ultimateProgress >= deadCharacter.maxUltimateProgress) {
                    val health = (deadCharacter.maxHealth * balance.intAttribute(pb_percent) * 0.01f).toInt()
                    val updatedDeadCharacter = deadCharacter.copy(health = health, ultimateProgress = 0, maxUltimateProgress = (deadCharacter.maxUltimateProgress * 1.25f).toInt())
                    context.game = context.game.withUpdatedCharacter(updatedDeadCharacter)
                    context.events.add(SbDisplayEvent.SbUpdateCharacter(updatedDeadCharacter.asDisplayed()))
                    context.events.add(SbDisplayEvent.SbShowTarotEffect(SbBattleFieldDisplayEffect.UltimateEffect(
                        SAFFRON_PHOENIX_REBIRTH, (0xffb100ff).toInt()), SbSoundType.ULTIMATE
                    ))
                }
            }
        }
    },

)

