package com.pl00t.swipe_client.services.battle.logic.processor.skills

import com.pl00t.swipe_client.services.battle.logic.Battle
import com.pl00t.swipe_client.services.battle.logic.TileSkin
import com.pl00t.swipe_client.services.battle.logic.processor.*

class RadiantStrikeBehaviour : TileBehaviour() {
    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.VALERIAN_RADIANT_STRIKE)
}

class LuminousBeamBehaviour : TileBehaviour() {
    override fun animationStrategy(battle: Battle, unitId: Int) = animateDirectedAoe(battle, unitId, TileSkin.VALERIAN_LUMINOUS_BEAM)
}

class SigilOfRenewalBehavior : TileBehaviour() {
    override fun animationStrategy(battle: Battle, unitId: Int) = animateSelfStatic(battle, unitId, TileSkin.VALERIAN_SIGIL_OF_RENEWAL)
}
