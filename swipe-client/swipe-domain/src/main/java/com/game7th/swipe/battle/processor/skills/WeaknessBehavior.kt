package com.game7th.swipe.battle.processor.skills

import com.game7th.swipe.battle.*
import com.game7th.swipe.battle.processor.SkillBehavior

class WeaknessBehavior : SkillBehavior() {

    override fun afterTileUsed(battle: Battle, character: Character, self: Tile, target: Tile): ProcessResult {
        if (self.x == target.x && self.y == target.y) {
            var unit = character
            unit = unit.copy(field = unit.field.copy(tiles = unit.field.tiles.filterNot { it.id == self.id }))
            var battle = battle.updateOrRemoveUnit(unit)
            val events = mutableListOf<BattleEvent>()
            events.add(BattleEvent.DestroyTileEvent(character.id, self.id, self.layer))

            return ProcessResult(events, battle)
        } else {
            return ProcessResult(emptyList(), battle)
        }
    }
}
