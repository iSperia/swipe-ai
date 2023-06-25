package com.pl00t.swipe_client.services.battle.logic.processor

import com.pl00t.swipe_client.services.battle.logic.Battle
import com.pl00t.swipe_client.services.battle.logic.TileSkin

inline fun animateMeleeAttack(battle: Battle, unitId: Int, skin: TileSkin) = battle.unitById(unitId)?.let { unit ->
    battle.meleeTarget(unit)?.let { target ->
        TarotAnimation.TarotFromSourceTargets(skin, unitId, listOf(target.id))
    } ?: TarotAnimation.TarotNoAnimation
} ?: TarotAnimation.TarotNoAnimation

inline fun animateAoeSkill(battle: Battle, unitId: Int, skin: TileSkin) = battle.unitById(unitId)?.let { unit ->
    TarotAnimation.TarotFromSourceTargets(skin, unitId, battle.enemies(unit).reversed().map { it.id })
} ?: TarotAnimation.TarotNoAnimation

inline fun animateSelfStatic(battle: Battle, unitId: Int, skin: TileSkin) = battle.unitById(unitId)?.let { unit ->
    TarotAnimation.TarotAtSourceRotate(skin, unit.id)
} ?: TarotAnimation.TarotNoAnimation

inline fun animateDirectedAoe(battle: Battle, unitId: Int, skin: TileSkin) = battle.unitById(unitId)?.let { unit ->
    TarotAnimation.TarotFromSourceDirected(skin, unit.id)
} ?: TarotAnimation.TarotNoAnimation
