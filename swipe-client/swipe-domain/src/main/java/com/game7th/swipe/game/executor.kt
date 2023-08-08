package com.game7th.swipe.game

import com.game7th.items.ItemAffixType
import kotlin.math.min
import kotlin.random.Random

class SbContext(
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

fun SbContext.createCharacter(skin: String): SbCharacter {
    val config = balance.getMonster(skin)
    val generators = config.tiles.mapIndexed { index, tileConfig ->
        SbEffect(
            id = index,
            skin = "COMMON_GENERATOR",
            mapOf(CommonKeys.Generator.GENERATOR to tileConfig)
        )
    }
    return SbCharacter(
        id = 0,
        skin = skin,
        human = true,
        health = config.balance.intAttribute("base_health"),
        maxHealth = config.balance.intAttribute("base_health"),
        ultimateProgress = 0,
        maxUltimateProgress = 1000,
        team = 0,
        attributes = CharacterAttributes.ZERO,
        maxTileId = 0,
        tiles = emptyList(),
        maxEffectId = generators.size,
        effects = generators,
        scale = config.scale,
    )
}

fun SbContext.initHumans(humans: List<FrontMonsterConfiguration>) {
    humans.forEach { config ->
        val maxHealth = config.health

        var character = createCharacter(config.skin).copy(
            human = true,
            team = 0,
            attributes = config.attributes,
            maxHealth = maxHealth,
            health = maxHealth,
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
        generateTiles(game.characters.last().id, 5)
    }
}

fun SbContext.initWave(wave: List<FrontMonsterConfiguration>) {
    wave.forEach { config ->
        val character = createCharacter(config.skin).copy(
            human = false,
            team = 1,
            attributes = config.attributes,
            maxHealth = config.health,
            health = config.health,
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
        generateTiles(game.characters.last().id, 5)
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

    if (target.health <= 0) {
        game = game.withRemovedCharacter(target.id)
        events.add(SbDisplayEvent.SbDestroyCharacter(target.id))
    }

    if (healthAfter <= 0) {
        destroyCharacter(targetCharacterId)
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
