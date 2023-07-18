package com.game7th.swipe.game

import com.game7th.swipe.battle.CharacterAttributes
import com.game7th.swipe.battle.HumanConfiguration
import com.game7th.swipe.battle.SbHumanEntry
import com.game7th.swipe.battle.SbMonsterEntry
import com.game7th.swipe.game.di.SbComponent
import kotlin.random.Random

class SbContext(
    var game: SbGame,
    var balance: SbBalanceProvider,
    val triggers: List<SbTrigger>,
) {
    val events = mutableListOf<SbDisplayEvent>()
}

fun SbContext.handleEvent(event: SbEvent) = triggers.forEach { it(event) }

fun SbContext.destroyCharacter(characterId: Int) {
    game = game.withRemovedCharacter(characterId)
    events.add(SbDisplayEvent.SbDestroyCharacter(characterId))
}

fun SbContext.initHumans(component: SbComponent, humans: List<SbHumanEntry>) {
    humans.forEach { config ->
        val factory = component.characterFactories()[config.skin]
        val character = factory?.createCharacter(balance)?.copy(
            human = true,
            team = 0,
            attributes = config.attributes
        ) ?: return
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
        val character = component.characterFactories()[config.skin]?.createCharacter(balance)?.copy(
            human = false,
            team = 1,
            attributes = attributes
        ) ?: return
        game = game.withAddedCharacter(character)
        events.add(SbDisplayEvent.SbCreateCharacter(
            personage = game.characters.last().asDisplayed()
        ))
        generateTiles(game.characters.last().id, 5)
    }
}

fun SbContext.dealDamage(sourceCharacterId: Int?, targetCharacterId: Int, damage: SbElemental) {
    var target = game.character(targetCharacterId) ?: return
    val targetResist = SbElemental(
        phys = target.sumFloat(CommonKeys.Resist.PHYS),
        cold = target.sumFloat(CommonKeys.Resist.COLD),
        light = target.sumFloat(CommonKeys.Resist.LIGHT),
        dark = target.sumFloat(CommonKeys.Resist.DARK),
        shock = target.sumFloat(CommonKeys.Resist.SHOCK),
        fire = target.sumFloat(CommonKeys.Resist.FIRE),
    )
    val damageAfterResist = damage.reducedByResist(targetResist)
    val damageTotal = damageAfterResist.total().toInt()
    val healthAfter = target.health - damageTotal
    target = target.withUpdatedHealth(healthAfter)

    game = game.withUpdatedCharacter(target)
    val icons = listOf(
        if (damageAfterResist.phys > 0f) "phys" else null,
        if (damageAfterResist.cold > 0f) "cold" else null,
        if (damageAfterResist.light > 0f) "light" else null,
        if (damageAfterResist.dark > 0f) "dark" else null,
        if (damageAfterResist.shock > 0f) "shock" else null,
        if (damageAfterResist.fire > 0f) "fire" else null,
    ).mapNotNull { it }

    events.add(SbDisplayEvent.SbShowPopup(targetCharacterId, damageTotal.toString(), icons))

    if (healthAfter <= 0) {
        destroyCharacter(targetCharacterId)
    }
}

