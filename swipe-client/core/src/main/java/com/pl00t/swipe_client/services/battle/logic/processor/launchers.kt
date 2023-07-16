package com.pl00t.swipe_client.services.battle.logic.processor

import com.google.gson.JsonObject
import com.pl00t.swipe_client.services.battle.MonsterAbilityConfiguration
import com.pl00t.swipe_client.services.battle.UnitSkin
import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.skills.PoisonBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.WeaknessBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.characters.*
import com.pl00t.swipe_client.services.monsters.MonsterService

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

class BehaviorFactory(private val monsterService: MonsterService) {

    private val cache = mutableMapOf<TileSkin, SkillBehavior>()

    suspend fun behavior(skin: TileSkin): SkillBehavior {
        return cache[skin] ?: when (skin) {
            TileSkin.COMMON_POISON -> PoisonBehavior()
            TileSkin.COMMON_WEAKNESS -> WeaknessBehavior()

            TileSkin.VALERIAN_RADIANT_STRIKE -> RadiantStrikeBehaviour(getMonsterAbilityConfig(UnitSkin.CHARACTER_VALERIAN, skin))
            TileSkin.VALERIAN_LUMINOUS_BEAM -> LuminousBeamBehaviour(getMonsterAbilityConfig(UnitSkin.CHARACTER_VALERIAN, skin))
            TileSkin.VALERIAN_SIGIL_OF_RENEWAL -> SigilOfRenewalBehavior(getMonsterAbilityConfig(UnitSkin.CHARACTER_VALERIAN, skin))
            TileSkin.VALERIAN_SIGIL_OF_RENEWAL_BG -> SigilOfRenewalBackgroundBehavior(getMonsterAbilityConfig(UnitSkin.CHARACTER_VALERIAN, TileSkin.VALERIAN_SIGIL_OF_RENEWAL))
            TileSkin.VALERIAN_DIVINE_CONVERGENCE -> DivineConvergenceBehavior(getMonsterAbilityConfig(UnitSkin.CHARACTER_VALERIAN, skin))

            TileSkin.THORNSTALKER_RESILIENT_GROWTH -> ResilentGrowth(getMonsterAbilityConfig(UnitSkin.MONSTER_THORNSTALKER, skin))
            TileSkin.THORNSTALKER_VENOMOUS_BARRAGE -> VenomousBarrageBehavior(getMonsterAbilityConfig(UnitSkin.MONSTER_THORNSTALKER, skin))
            TileSkin.THORNSTALKER_PRIMAL_ASSAULT -> PrimalAssaultBehaviour(getMonsterAbilityConfig(UnitSkin.MONSTER_THORNSTALKER, skin))

            TileSkin.CORRUPTED_DRYAD_VILE_SIPHON -> VileSiphonSkill(getMonsterAbilityConfig(UnitSkin.MONSTER_CORRUPTED_DRYAD, skin))
            TileSkin.CORRUPTED_DRYAD_SHADOWED_ANNIHILATION -> ShadowedAnnihinlation(getMonsterAbilityConfig(UnitSkin.MONSTER_CORRUPTED_DRYAD, skin))
            TileSkin.CORRUPTED_DRYAD_ARBOREAL_FANGS -> ArborealFangsSkill(getMonsterAbilityConfig(UnitSkin.MONSTER_CORRUPTED_DRYAD, skin))

            TileSkin.THORNED_CRAWLER_VICIOUS_PINCERS -> ViciousPincers(getMonsterAbilityConfig(UnitSkin.MONSTER_THORNED_CRAWLER, skin))
            TileSkin.THORNED_CRAWLER_LEECHING_SHADOWS -> LeechingShadows(getMonsterAbilityConfig(UnitSkin.MONSTER_THORNED_CRAWLER, skin))
            TileSkin.THORNED_CRAWLER_DEBILIATING_STRIKE -> DebiliatingStrike(getMonsterAbilityConfig(UnitSkin.MONSTER_THORNED_CRAWLER, skin))

            TileSkin.THALENDROS_EARTHQUAKE_SLAM -> EarthquakeSlam(getMonsterAbilityConfig(UnitSkin.MONSTER_THALENDROS, skin))
            TileSkin.THALENDROS_DARK_AURA -> DarkAura(getMonsterAbilityConfig(UnitSkin.MONSTER_THALENDROS, skin))
            TileSkin.THALENDROS_THORN_WHIP -> ThornedWhipBehavior(getMonsterAbilityConfig(UnitSkin.MONSTER_THALENDROS, skin))

            else -> PoisonBehavior()
        }.also { cache[skin] = it }
    }

    private suspend fun getMonsterAbilityConfig(unitSkin: UnitSkin, skin: TileSkin): MonsterAbilityConfiguration {
        return monsterService.getMonster(unitSkin).abilities?.firstOrNull { it.skin == skin } ?: MonsterAbilityConfiguration("", skin, JsonObject(), emptyList(), "", "")
    }
}
