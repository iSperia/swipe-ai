package com.pl00t.swipe_client.analytics

import com.pl00t.swipe_client.services.profile.SwipeAct

interface AnalyticsInterface {

    fun trackEvent(event: String, data: Map<String, String>)

    fun trackEvent(event: String)


}


object AnalyticEvents {
    object BattleEvent {
        val EVENT_BATTLE_START = "battle.start"
        val EVENT_BATTLE_RESTART = "battle.restart"
        val EVENT_BATTLE_VICTORY = "battle.victory"
        val EVENT_BATTLE_DEFEAT = "battle.defeat"

        val KEY_ACT = "act"
        val KEY_LEVEL = "level"
        val KEY_TIER = "tier"

        fun create(act: SwipeAct, level: String, tier: Int) = mapOf(
            KEY_ACT to act.toString(),
            KEY_LEVEL to level,
            KEY_TIER to tier.toString(),
        )
    }

    object EquipEvent {
        val EVENT_EQUIP = "item.equip"
        val EVENT_UNEQUIP = "item.unequip"
    }

    object MysteryShopEvent {
        val EVENT_UPGRADE = "mysteryshop.upgrade"
        val EVENT_REFRESH = "mysteryshop.refresh"
        val EVENT_PURCHASE = "mysteryshop.purchase"
        val KEY_TIER = "tier"
        val KEY_COST = "cost"
    }

    object MineEvent {
        val EVENT_LAUNCH = "mine.launch"
        val EVENT_UPGRADE = "mine.upgrade"
        val KEY_LEVEL = "level"
    }
}
