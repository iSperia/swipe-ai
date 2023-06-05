package com.pl00t.swipe_client.screen.battle

import com.pl00t.swipe_client.services.battle.logic.UnitSkin

object BattleScaleMapper {

    fun map(unit: UnitSkin): Float = when (unit) {
        UnitSkin.MONSTER_THALENDROS -> 1.5f
        UnitSkin.MONSTER_CORRUPTED_DRYAD -> 0.92f
        UnitSkin.MONSTER_THORNSTALKER -> 1.05f
        else -> 1f
    }
}
