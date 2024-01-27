package com.game7th.swipe.game

import kotlin.math.min
import kotlin.random.Random

data class SbContext(
    var game: SbGame,
    var balance: SbBalanceProvider,
    val triggers: List<SbTrigger>,
) {
    val events = mutableListOf<SbDisplayEvent>()
}

fun SbContext.handleEvent(event: SbEvent) = triggers.forEach { it(this, event) }

fun SbContext.destroyCharacter(characterId: Int) {
    game = game.withRemovedCharacter(characterId)
    events.add(SbDisplayEvent.SbDestroyCharacter(characterId))
}

fun SbContext.meleeTarget(characterId: Int): List<Int> = game.character(characterId)?.let { character ->
    game.characters.filter { c ->
        c.team != character.team
    }.maxByOrNull { it.maxHealth }?.id?.let { listOf(it) } ?: emptyList()
} ?: emptyList()

fun SbContext.rangedTarget(characterId: Int): List<Int> = game.character(characterId)?.let { character ->
    game.characters.filter { c ->
        c.team != character.team
    }.minByOrNull { it.maxHealth }?.id?.let { listOf(it) } ?: emptyList()
} ?: emptyList()

fun SbContext.allEnemies(characterId: Int): List<Int> = game.character(characterId)?.let { character ->
    game.characters.filter { c ->
        c.team != character.team
    }.map { it.id }
} ?: emptyList()

fun SbContext.randomTarget(characterId: Int): List<Int> = game.character(characterId)?.let { character ->
    game.characters.filter { c ->
        c.team != character.team
    }.randomOrNull()?.id?.let { listOf(it) } ?: emptyList()
} ?: emptyList()

fun SbContext.createCharacter(skin: String, rarity: Int = 0): SbCharacter {
    val config = balance.getMonster(skin)
    val generators = if (config.tiles != null) {
        config.tiles.mapIndexed { index, tileConfig ->
            SbEffect(
                id = index,
                skin = "COMMON_GENERATOR",
                mapOf(CommonKeys.Generator.GENERATOR to tileConfig)
            )
        }
    } else emptyList()
    return SbCharacter(
        id = 0,
        skin = skin,
        human = true,
        health = config.balance.intAttribute("base_health"),
        maxHealth = config.balance.intAttribute("base_health"),
        ultimateProgress = 0,
        maxUltimateProgress = config.balance.intAttribute("ult_max"),
        team = 0,
        attributes = CharacterAttributes.ZERO,
        maxTileId = 0,
        tiles = emptyList(),
        maxEffectId = generators.size,
        effects = generators,
        scale = config.scale,
        rarity = rarity,
        cap = config.cap,
    )
}

fun SbContext.initHumans(humans: List<FrontMonsterConfiguration>) {
    humans.forEach { config ->
        val maxHealth = config.health

        var character = createCharacter(config.skin, config.rarity).copy(
            human = true,
            team = 0,
            attributes = config.attributes,
            maxHealth = maxHealth,
            health = maxHealth,
            ultimateProgress = config.ultMax * config.ultPrefillPercent / 100
        ).withAddedEffect(
            SbEffect(
            id = 0,
            skin = "base.resist",
            mapOf(
                CommonKeys.Resist.PHYS to config.resist.phys,
                CommonKeys.Resist.COLD to config.resist.cold,
                CommonKeys.Resist.DARK to config.resist.dark,
                CommonKeys.Resist.LIGHT to config.resist.light,
                CommonKeys.Resist.FIRE to config.resist.fire,
                CommonKeys.Resist.SHOCK to config.resist.shock,
            )
        )).withAddedEffect(
            SbEffect(
                id = 0,
                skin = "base.damage",
                mapOf(
                    CommonKeys.Damage.PHYS to config.damage.phys,
                    CommonKeys.Damage.COLD to config.damage.cold,
                    CommonKeys.Damage.DARK to config.damage.dark,
                    CommonKeys.Damage.LIGHT to config.damage.light,
                    CommonKeys.Damage.FIRE to config.damage.fire,
                    CommonKeys.Damage.SHOCK to config.damage.shock,
                )
            )
        )

        game = game.withAddedCharacter(character)
        events.add(SbDisplayEvent.SbCreateCharacter(
            personage = game.characters.last().asDisplayed()
        ))
        events.add(SbDisplayEvent.SbUpdateCharacter(game.characters.last().asDisplayed()))
        if (game.tutorialMetadata.isFirstTutorial) {
            game = game.withUpdatedCharacter(game.character(0)!!.withAddedTile(SbTile(0, "VALERIAN_RADIANT_STRIKE", 1, 1, SbTile.LAYER_TILE, true, 5, SbTileMergeStrategy.SIMPLE, 1, 3, 0, emptyList())))
            events.add(SbDisplayEvent.SbCreateTile(0, game.character(0)!!.tiles.last().asDisplayed()))
            game = game.withUpdatedCharacter(game.character(0)!!.withAddedTile(SbTile(0, "VALERIAN_RADIANT_STRIKE", 3, 3, SbTile.LAYER_TILE, true, 5, SbTileMergeStrategy.SIMPLE, 1, 3, 0, emptyList())))
            events.add(SbDisplayEvent.SbCreateTile(0, game.character(0)!!.tiles.last().asDisplayed()))
            game = game.withUpdatedCharacter(game.character(0)!!.withAddedTile(SbTile(0, "VALERIAN_SIGIL_OF_RENEWAL", 4, 1, SbTile.LAYER_TILE, true, 5, SbTileMergeStrategy.SIMPLE, 1, 3, 0, emptyList())))
            events.add(SbDisplayEvent.SbCreateTile(0, game.character(0)!!.tiles.last().asDisplayed()))
            game = game.withUpdatedCharacter(game.character(0)!!.withAddedTile(SbTile(0, "VALERIAN_LUMINOUS_BEAM", 0, 0, SbTile.LAYER_TILE, true, 5, SbTileMergeStrategy.SIMPLE, 1, 4, 0, emptyList())))
            events.add(SbDisplayEvent.SbCreateTile(0, game.character(0)!!.tiles.last().asDisplayed()))
        } else {
            generateTiles(game.characters.last().id, 5)
        }
    }
}

fun SbContext.initWave(wave: List<FrontMonsterConfiguration>) {
    wave.forEach { config ->
        val allResBonus = (config.rarityAffixes.count { it == SbMonsterRarityAffix.ALL_RESIST }) * 0.3f
        val allDamageBonus = (config.rarityAffixes.count { it == SbMonsterRarityAffix.ALL_DAMAGE }) * 0.07f
        val mindBonus = (config.rarityAffixes.count { it == SbMonsterRarityAffix.EXTRA_MIND }) * 0.2f
        val bodyBonus = (config.rarityAffixes.count { it == SbMonsterRarityAffix.EXTRA_BODY }) * 0.2f
        val spiritBonus = (config.rarityAffixes.count { it == SbMonsterRarityAffix.EXTRA_SPIRIT }) * 0.2f
        val allAttributesMultiplier = (config.rarityAffixes.count { it == SbMonsterRarityAffix.ALL_ATTRIBUTES }) * 0.1f
        val hpBonus = (config.rarityAffixes.count { it == SbMonsterRarityAffix.EXTRA_HP }) * 0.25f
        val luckBonus = (config.rarityAffixes.count { it == SbMonsterRarityAffix.EXTRA_LUCK }) * 0.25f
        val ultBonus = (config.rarityAffixes.count { it == SbMonsterRarityAffix.EXTRA_ULT_PROGRESS }) * 0.25f

        val character = createCharacter(config.skin, config.rarity).copy(
            human = false,
            team = 1,
            attributes = config.attributes.copy(
                mind = (config.attributes.mind * (1f + allAttributesMultiplier + mindBonus)).toInt(),
                body = (config.attributes.body * (1f + allAttributesMultiplier + bodyBonus)).toInt(),
                spirit = (config.attributes.spirit * (1f + allAttributesMultiplier + spiritBonus)).toInt()
            ),
            maxHealth = (config.health * (1f + hpBonus)).toInt(),
            health = (config.health * (1f + hpBonus)).toInt(),
        ).withAddedEffect(
            SbEffect(
                id = 0,
                skin = "base.resist",
                mapOf(
                    CommonKeys.Resist.PHYS to config.resist.phys + allResBonus,
                    CommonKeys.Resist.COLD to config.resist.cold + allResBonus,
                    CommonKeys.Resist.DARK to config.resist.dark + allResBonus,
                    CommonKeys.Resist.LIGHT to config.resist.light + allResBonus,
                    CommonKeys.Resist.FIRE to config.resist.fire + allResBonus,
                    CommonKeys.Resist.SHOCK to config.resist.shock + allResBonus,
                )
            )
        ).withAddedEffect(
            SbEffect(
                id = 0,
                skin = "base.damage",
                mapOf(
                    CommonKeys.Damage.PHYS to config.damage.phys + allDamageBonus,
                    CommonKeys.Damage.COLD to config.damage.cold + allDamageBonus,
                    CommonKeys.Damage.DARK to config.damage.dark + allDamageBonus,
                    CommonKeys.Damage.LIGHT to config.damage.light + allDamageBonus,
                    CommonKeys.Damage.FIRE to config.damage.fire + allDamageBonus,
                    CommonKeys.Damage.SHOCK to config.damage.shock + allDamageBonus,
                )
            )
        ).withAddedEffect(
            SbEffect(
                id = 0,
                skin = CommonKeys.LUCK.EXTRA_LUCK,
                mapOf(
                    CommonKeys.LUCK.EXTRA_LUCK to luckBonus
                )
            )
        ).withAddedEffect(
            SbEffect(
                id = 0,
                skin = CommonKeys.ULT_PROGRESS.EXTRA_ULT_PROGRESS,
                mapOf(
                    CommonKeys.ULT_PROGRESS.EXTRA_ULT_PROGRESS to ultBonus
                )
            )
        ).withAddedEffect(
            SbEffect(
                id = 0,
                skin = CommonKeys.MONSTER_COMMON.ABILITY_POOL,
                mapOf(
                    CommonKeys.MONSTER_COMMON.ABILITY_POOL to config.abilities,
                    CommonKeys.MONSTER_COMMON.ACTIVE_ABILITY_TICKS to 0,
                    CommonKeys.MONSTER_COMMON.ACTIVE_ABILITY to ""
                )
            )
        )
        game = game.withAddedCharacter(character)
        checkMonsterIntent(game.characters.last().id)
        events.add(SbDisplayEvent.SbCreateCharacter(
            personage = game.characters.last().asDisplayed()
        ))
    }
}

fun SbContext.checkMonsterIntent(id: Int) {
    val monster = game.character(id) ?: return
    if (!monster.human) {
        val abilityConfig: SbEffect = monster.effects.firstOrNull { it.skin == CommonKeys.MONSTER_COMMON.ABILITY_POOL } ?: return

        val ticks = (abilityConfig.data[CommonKeys.MONSTER_COMMON.ACTIVE_ABILITY_TICKS] as? Int) ?: 0

        if (ticks <= 0) {
            //ok, we have no active ability to go, so please generate new one
            val config = monster.collect<List<SbMonsterAbilityConfiguration>>(CommonKeys.MONSTER_COMMON.ABILITY_POOL).firstOrNull() ?: return
            val totalWeight = config.sumOf { it.weight }
            val roll = Random.nextInt(totalWeight)
            var sum = 0
            val ability = config.firstOrNull {
                sum += it.weight
                sum > roll
            } ?: return

            val newData = abilityConfig.data.toMutableMap()
            newData[CommonKeys.MONSTER_COMMON.ACTIVE_ABILITY_TICKS] = ability.timeout
            newData[CommonKeys.MONSTER_COMMON.ACTIVE_ABILITY] = ability.id

            val newCharacter = monster.withUpdatedEffect(abilityConfig.copy(data = newData))
            events.add(SbDisplayEvent.SbUpdateCharacter(newCharacter.asDisplayed()))
            game = game.withUpdatedCharacter(newCharacter)
        }
    }
}

fun SbContext.dealDamage(sourceCharacterId: Int?, targetCharacterId: Int, damage: SbElemental) {
    var target = game.character(targetCharacterId) ?: return

    val damageCoefficient = sourceCharacterId?.let { game.character(sourceCharacterId) }?.let { source ->
        SbElemental(
            phys = source.sumFloat(CommonKeys.Damage.PHYS) / 100f,
            cold = source.sumFloat(CommonKeys.Damage.COLD) / 100f,
            light = source.sumFloat(CommonKeys.Damage.LIGHT) / 100f,
            dark = source.sumFloat(CommonKeys.Damage.DARK) / 100f,
            shock = source.sumFloat(CommonKeys.Damage.SHOCK) / 100f,
            fire = source.sumFloat(CommonKeys.Damage.FIRE) / 100f,
        )
    } ?: SbElemental(0f,0f,0f,0f,0f,0f)
    //count weakness of source
    val weaknessCoefficient: Float = 1f - (sourceCharacterId?.let {
        val sourceCharacter = game.character(sourceCharacterId) ?: return@let 0f
        val weaknessEffects = sourceCharacter.effects.count { it.skin == "COMMON_WEAKNESS" }
        weaknessEffects * 0.025f
    } ?: 0f)
    val damageAfterWeakness = damage.scaledBy(damageCoefficient).multipledBy(weaknessCoefficient)

    //count resist
    val targetResist = SbElemental(
        phys = target.sumFloat(CommonKeys.Resist.PHYS) / 100f,
        cold = target.sumFloat(CommonKeys.Resist.COLD) / 100f,
        light = target.sumFloat(CommonKeys.Resist.LIGHT) / 100f,
        dark = target.sumFloat(CommonKeys.Resist.DARK) / 100f,
        shock = target.sumFloat(CommonKeys.Resist.SHOCK) / 100f,
        fire = target.sumFloat(CommonKeys.Resist.FIRE) / 100f,
    )
    val damageAfterResist = damageAfterWeakness.reducedByResist(targetResist)


    val damageTotal = damageAfterResist.total().toInt()
    val healthAfter = target.health - damageTotal
    target = target.withUpdatedHealth(healthAfter)

    game = game.withUpdatedCharacter(target)
    val icons = listOf(
        if (damageAfterResist.phys > 0f) "physical" else null,
        if (damageAfterResist.cold > 0f) "cold" else null,
        if (damageAfterResist.light > 0f) "light" else null,
        if (damageAfterResist.dark > 0f) "dark" else null,
        if (damageAfterResist.shock > 0f) "shock" else null,
        if (damageAfterResist.fire > 0f) "fire" else null,
    ).mapNotNull { it }

    events.add(SbDisplayEvent.SbUpdateCharacter(target.asDisplayed()))
    events.add(SbDisplayEvent.SbShowPopup(targetCharacterId, "${damageTotal}", icons))

    handleEvent(SbEvent.DamageDealt(target.id, sourceCharacterId, damageAfterResist))

    if (target.health <= 0) {
        handleEvent(SbEvent.CharacterPreDeath(target.id))
        target = game.character(target.id) ?: target
        if (target.health <= 0) {
            game = game.withRemovedCharacter(target.id)
            events.add(SbDisplayEvent.SbDestroyCharacter(target.id))
            destroyCharacter(targetCharacterId)
        }
    }
}

fun SbContext.healCharacter(characterId: Int, amount: Int) {
    var target = game.character(characterId) ?: return
    val healthAfter = min(target.maxHealth, target.health + amount)

    target = target.withUpdatedHealth(healthAfter)
    game = game.withUpdatedCharacter(target)

    events.add(SbDisplayEvent.SbShowPopup(characterId, amount.toString(), listOf("heal"), SbSoundType.HEAL_SPELL))
}

fun SbContext.inflictPoison(characterId: Int, amount: Int) {
    var target = game.character(characterId) ?: return
    if (target.tiles.none { it.skin == "COMMON_POISON" }) {
        val positions = freePositions(characterId, SbTile.LAYER_TILE, 3)

        positions.forEach { p ->
            val tile = SbTile(
                id = 0,
                skin = "COMMON_POISON",
                x = p % 5,
                y = p / 5,
                z = SbTile.LAYER_TILE,
                mobility = 5,
                mergeStrategy = SbTileMergeStrategy.SIMPLE,
                progress = 1,
                maxProgress = 3,
                maxEffectId = 0,
                skill = false,
                effects = emptyList()
            )
            target = target.withAddedTile(tile)
            game = game.withUpdatedCharacter(target)
            events.add(SbDisplayEvent.SbCreateTile(characterId = target.id, tile = target.tiles.last().asDisplayed()))
        }
    }
    game = game.withUpdatedCharacter(target.withAddedEffect(
        SbEffect(
        id = 0,
        skin = "COMMON_POISON",
        data = mapOf(CommonKeys.Poison.POISON to amount)
    )))
}
