package com.game7th.swipe.battle.processor.skills

import com.game7th.swipe.battle.*
import com.game7th.swipe.battle.processor.SkillBehavior

class InflictPoisonBehavior(
    val getTargets: (Battle, Character) -> List<Character>,
    val getAmount: (Battle, Character, Character) -> Int
) : SkillBehavior() {

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val targets = getTargets(battle, character)
        val events = mutableListOf<BattleEvent>()
        var battle = battle
        targets.forEach { target ->
            var targetCharacter = target
            val amount = getAmount(battle, character, target).let { if (lucky) it * 2 else it }
            val positions = (0 until 25)
                .filter { target.field.tileAt(it % 5, it / 5, 5) == null }
                .shuffled()
            if (positions.size > 2) {
                positions.take(3).forEach { p ->
                    val tile = Tile(
                        skin = TileSkin.COMMON_POISON,
                        progress = 1,
                        maxProgress = 3,
                        x = p % 5,
                        y = p / 5,
                        id = targetCharacter.field.maxTileId,
                        layer = 5,
                        mobility = 5,
                        type = TileType.TAROT,
                        meta = PoisonBehavior.Meta(amount)
                    )
                    targetCharacter = targetCharacter.addTile(tile)
                    events.add(BattleEvent.CreateTileEvent(target.id, tile.id, tile.x, tile.y, tile.skin, tile.progress, tile.maxProgress, tile.layer, tile.type))
                    battle = battle.updateOrRemoveUnit(targetCharacter)
                }
            }
        }
        return ProcessResult(events, battle)
    }
}
