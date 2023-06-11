package com.pl00t.swipe_client.services.battle.logic.processor.skills

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior
import kotlin.math.min

class HealBehavior(
    val getTargets: (Battle, Character) -> List<Character>,
    val getAmount: (Battle, Character, Character) -> Int
) : SkillBehavior() {

    override fun skillUse(battle: Battle, character: Character, lucky: Boolean): ProcessResult {
        val targets = getTargets(battle, character)
        val events = mutableListOf<BattleEvent>()
        var battle = battle
        targets.forEach { target ->
            val amount = getAmount(battle, character, target).let { if (lucky) it * 2 else it }

            val healthAfterHeal = min(target.maxHealth, target.health + amount)
            battle.updateOrRemoveUnit(target.copy(health = healthAfterHeal))
            events.add(BattleEvent.UnitPopupEvent(target.id, UnitPopup(listOf("heal"), amount.toString())))
        }
        return ProcessResult(events, battle)
    }
}
