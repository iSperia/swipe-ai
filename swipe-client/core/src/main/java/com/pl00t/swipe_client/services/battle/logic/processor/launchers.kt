package com.pl00t.swipe_client.services.battle.logic.processor

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.skills.PoisonBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.WeaknessBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.characters.*

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

object BehaviorFactory {

    private val cache = mutableMapOf<TileSkin, SkillBehavior>()
    fun behavior(skin: TileSkin): SkillBehavior {
        return cache[skin] ?: when (skin) {
            TileSkin.COMMON_POISON -> PoisonBehavior()
            TileSkin.COMMON_WEAKNESS -> WeaknessBehavior()

            TileSkin.VALERIAN_RADIANT_STRIKE -> RadiantStrikeBehaviour()
            TileSkin.VALERIAN_LUMINOUS_BEAM -> LuminousBeamBehaviour()
            TileSkin.VALERIAN_SIGIL_OF_RENEWAL -> SigilOfRenewalBehavior()
            TileSkin.VALERIAN_SIGIL_OF_RENEWAL_BG -> SigilOfRenewalBackgroundBehavior()
            TileSkin.VALERIAN_DIVINE_CONVERGENCE -> DivineConvergenceBehavior()

            TileSkin.THORNSTALKER_RESILIENT_GROWTH -> ResilentGrowth()
            TileSkin.THORNSTALKER_VENOMOUS_BARRAGE -> VenomousBarrageBehavior()
            TileSkin.THORNSTALKER_PRIMAL_ASSAULT -> PrimalAssaultBehaviour()

            TileSkin.CORRUPTED_DRYAD_VILE_SIPHON -> VileSiphonSkill()
            TileSkin.CORRUPTED_DRYAD_SHADOWED_ANNIHILATION -> ShadowedAnnihinlation()
            TileSkin.CORRUPTED_DRYAD_ARBOREAL_FANGS -> ArborealFangsSkill()

            TileSkin.THORNED_CRAWLER_VICIOUS_PINCERS -> ViciousPincers()
            TileSkin.THORNED_CRAWLER_LEECHING_SHADOWS -> LeechingShadows()
            TileSkin.THORNED_CRAWLER_DEBILIATING_STRIKE -> DebiliatingStrike()
            else -> PrimalAssaultBehaviour()
        }.also { cache[skin] = it }
    }
}
