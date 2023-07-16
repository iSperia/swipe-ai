package com.pl00t.swipe_client.services.battle.logic.processor.skills.characters

import com.pl00t.swipe_client.services.battle.MonsterAbilityConfiguration
import com.pl00t.swipe_client.services.battle.floatAttribute
import com.pl00t.swipe_client.services.battle.intAttribute
import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.animateMeleeAttack
import com.pl00t.swipe_client.services.battle.logic.processor.skills.MeleeAttackSkillBehavior

class ThornedWhipBehavior(
    config: MonsterAbilityConfiguration
) : SkillBehavior() {

    private val physDamage = config.attributes.floatAttribute("physDamage")
    private val physPerBody = config.attributes.floatAttribute("physPerBody")

    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = physDamage * (1f + character.attributes.body * physPerBody / 100f)
        ElementalConfig(physical = physicalDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THALENDROS_THORN_WHIP)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean) = melee.skillUse(battle, character, at, lucky)
}

class EarthquakeSlam(
    config: MonsterAbilityConfiguration
) : SkillBehavior() {

    private val tilesCount = config.attributes.intAttribute("numTiles")
    private val resistBuff = config.attributes.floatAttribute("resistBuff")

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THALENDROS_EARTHQUAKE_SLAM)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        return super.skillUse(battle, character, at, lucky)
    }
}

class DarkAura(
    config: MonsterAbilityConfiguration
) : SkillBehavior() {

}
