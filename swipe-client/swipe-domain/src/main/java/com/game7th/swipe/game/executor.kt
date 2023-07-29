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

fun SbContext.initHumans(humans: List<SbHumanEntry>) {
    humans.forEach { config ->
        val maxHealth = (balance.getMonster(config.skin).balance.intAttribute("base_health") * (1f + 0.1f * config.attributes.body)).toInt()

        var physResist = 0f
        var coldResist = 0f
        var lightResist = 0f
        var fireResist = 0f
        var darkResist = 0f
        var shockResist = 0f
        var physIncrease = 0f
        var coldIncrease = 0f
        var lightIncrease = 0f
        var fireIncrease = 0f
        var darkIncrease = 0f
        var shockIncrease = 0f
        config.items.flatMap { it.affixes + it.implicit }.forEach { affix ->
            when (affix.affix) {
                ItemAffixType.PHYS_RESIST_FLAT -> physResist += affix.value
                ItemAffixType.COLD_RESIST_FLAT -> coldResist += affix.value
                ItemAffixType.DARK_RESIST_FLAT -> darkResist += affix.value
                ItemAffixType.FIRE_RESIST_FLAT -> fireResist += affix.value
                ItemAffixType.LIGHT_RESIST_FLAT -> lightResist += affix.value
                ItemAffixType.SHOCK_RESIST_FLAT -> shockResist += affix.value
                ItemAffixType.COLD_DAMAGE_INCREASE -> coldIncrease += affix.value
                ItemAffixType.DARK_DAMAGE_INCREASE -> darkIncrease += affix.value
                ItemAffixType.PHYS_DAMAGE_INCREASE -> physIncrease += affix.value
                ItemAffixType.LIGHT_DAMAGE_INCREASE -> lightIncrease += affix.value
                ItemAffixType.FIRE_DAMAGE_INCREASE -> fireIncrease += affix.value
                ItemAffixType.SHOCK_DAMAGE_INCREASE -> shockIncrease += affix.value
                else -> {}
            }
        }

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
                CommonKeys.Resist.PHYS to physResist,
                CommonKeys.Resist.COLD to coldResist,
                CommonKeys.Resist.DARK to darkResist,
                CommonKeys.Resist.LIGHT to lightResist,
                CommonKeys.Resist.FIRE to fireResist,
                CommonKeys.Resist.SHOCK to shockResist,
            )
        )).withAddedEffect(
            SbEffect(
                id = 0,
                skin = "base.damage",
                mapOf(
                    CommonKeys.Damage.PHYS to physIncrease,
                    CommonKeys.Damage.COLD to coldIncrease,
                    CommonKeys.Damage.DARK to darkIncrease,
                    CommonKeys.Damage.LIGHT to lightIncrease,
                    CommonKeys.Damage.FIRE to fireIncrease,
                    CommonKeys.Damage.SHOCK to shockIncrease,
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

fun SbContext.initWave(wave: List<SbMonsterEntry>) {
    wave.forEach { config ->
        val amountOfAttributes = ((1f + 0.01f * config.level) * config.level * 3).toInt()
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
