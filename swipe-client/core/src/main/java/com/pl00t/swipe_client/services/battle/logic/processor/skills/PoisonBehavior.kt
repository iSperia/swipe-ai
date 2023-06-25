package com.pl00t.swipe_client.services.battle.logic.processor.skills

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior
import kotlin.math.max

class PoisonBehavior : SkillBehavior() {

    data class Meta(val amount: Int)

    override fun onEndOfTurn(battle: Battle, characterId: Int, self: Tile): ProcessResult {
        battle.unitById(characterId)?.let { character ->
            (self.meta as? Meta)?.let { meta ->
                val amount = meta.amount
                val events = mutableListOf<BattleEvent>()
                val newHealth = max(0, character.health - amount)
                if (amount > 0) {
                    val character = character.copy(health = newHealth)
                    val battle = battle.updateOrRemoveUnit(character)
                    events.add(
                        BattleEvent.UnitPopupEvent(
                            character.id, UnitPopup(
                                icons = listOf("dark"),
                                text = "$amount (poison)"
                            )
                        )
                    )
                    if (character.health <= 0) events.add(BattleEvent.UnitDeathEvent(character.id))
                    events.add(BattleEvent.UnitHealthEvent(character.id, newHealth))

                    return ProcessResult(events, battle)
                }
            }
        }
        return ProcessResult(emptyList(), battle)
    }
}
