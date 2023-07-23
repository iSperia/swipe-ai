package com.game7th.swipe.game

import com.game7th.swipe.battle.*
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

fun SbContext.initHumans(component: SbComponent, humans: List<SbHumanEntry>) {
    humans.forEach { config ->
        val maxHealth = (balance.getMonster(config.skin).balance.intAttribute("base_health") * (1f + 0.1f * config.attributes.body)).toInt()
        val character = createCharacter(config.skin).copy(
            human = true,
            team = 0,
            attributes = config.attributes,
            maxHealth = maxHealth,
            health = maxHealth
        )
        game = game.withAddedCharacter(character)
        events.add(SbDisplayEvent.SbCreateCharacter(
            personage = game.characters.last().asDisplayed()
        ))
        generateTiles(game.characters.last().id, 5)
    }
}

fun SbContext.initWave(component: SbComponent, wave: List<SbMonsterEntry>) {
    wave.forEach { config ->
        val amountOfAttributes = (Math.pow(1.05, config.level.toDouble()).toFloat() * config.level * 3).toInt()
        var body = 0
        var spirit = 0
        var mind = 0
        (1..amountOfAttributes).forEach {
            val r = Random.nextFloat()
            when {
                r <= 0.3333f -> body++
                r <= 0.6666f -> spirit++
                else -> mind++
            }
        }
        val attributes = CharacterAttributes(mind, body, spirit)
        val maxHealth = (balance.getMonster(config.skin).balance.intAttribute("base_health") * (1f + 0.1f * body)).toInt()
        val character = createCharacter(config.skin).copy(
            human = false,
            team = 1,
            attributes = attributes,
            maxHealth = maxHealth,
            health = maxHealth
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

    //count weakness of source
    val weaknessCoefficient: Float = 1f - (sourceCharacterId?.let {
        val sourceCharacter = game.character(sourceCharacterId) ?: return@let 0f
        val weaknessEffects = sourceCharacter.effects.count { it.skin == "COMMON_WEAKNESS" }
        weaknessEffects * 0.025f
    } ?: 0f)
    val damageAfterWeakness = damage.multipledBy(weaknessCoefficient)

    //count resist
    val targetResist = SbElemental(
        phys = target.sumFloat(CommonKeys.Resist.PHYS),
        cold = target.sumFloat(CommonKeys.Resist.COLD),
        light = target.sumFloat(CommonKeys.Resist.LIGHT),
        dark = target.sumFloat(CommonKeys.Resist.DARK),
        shock = target.sumFloat(CommonKeys.Resist.SHOCK),
        fire = target.sumFloat(CommonKeys.Resist.FIRE),
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
    events.add(SbDisplayEvent.SbShowPopup(targetCharacterId, damageTotal.toString(), icons))

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

    events.add(SbDisplayEvent.SbShowPopup(characterId, amount.toString(), listOf("heal")))
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
