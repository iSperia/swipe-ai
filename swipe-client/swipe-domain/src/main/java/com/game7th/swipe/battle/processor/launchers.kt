package com.game7th.swipe.battle.processor

import com.game7th.swipe.battle.*
import com.game7th.swipe.battle.processor.skills.characters.*
import com.google.gson.JsonObject
import com.game7th.swipe.battle.processor.skills.PoisonBehavior
import com.game7th.swipe.battle.processor.skills.WeaknessBehavior
import com.game7th.swipe.monsters.MonsterService

sealed abstract class TarotAnimation(val skin: TileSkin) {
    class TarotFromSourceTargets(
        skin: TileSkin,
        val from: Int,
        val targets: List<Int>
    ) : TarotAnimation(skin) {
        override fun toString(): String {
            return "TarotFromSourceTargets(from=$from, targets=$targets)"
        }
    }
    class TarotFromSourceDirected(
        skin: TileSkin,
        val from: Int
    ) : TarotAnimation(skin)

    class TarotAtSourceRotate(
        skin: TileSkin,
        val at: Int
    ) : TarotAnimation(skin)

    object TarotNoAnimation : TarotAnimation(TileSkin.VALERIAN_RADIANT_STRIKE)
}
abstract class SkillBehavior {
    open fun animationStrategy(battle: Battle, unitId: Int): TarotAnimation = TarotAnimation.TarotNoAnimation

    open fun autoDelete(): Boolean = true

    open fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult = ProcessResult(emptyList(), battle)

    open fun ultimateUse(battle: Battle, character: Character, lucky: Boolean): ProcessResult = ProcessResult(emptyList(), battle)

    open fun getBaseLuck(): Float = 0.05f

    open fun afterTileUsed(battle: Battle, character: Character, self: Tile, target: Tile): ProcessResult = ProcessResult(emptyList(), battle)

    open fun onEndOfTurn(battle: Battle, characterId: Int, self: Tile): ProcessResult = ProcessResult(emptyList(), battle)
}
