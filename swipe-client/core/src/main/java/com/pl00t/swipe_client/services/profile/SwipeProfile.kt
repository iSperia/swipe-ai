package com.pl00t.swipe_client.services.profile

import com.game7th.items.InventoryItem
import com.game7th.swipe.game.CharacterAttributes

enum class SwipeCurrency {
    ETHERIUM_COIN,
    SCROLL_OF_WISDOM,
    TOME_OF_ENLIGHTMENT,
    CODEX_OF_ASCENDANCY,
    GRIMOIRE_OF_OMNISCENCE,
    INFUSION_ORB,
    INFUSION_SHARD,
    INFUSION_CRYSTAL,
    ASCENDANT_ESSENCE,
}

enum class SwipeAct {
    ACT_1, ACT_2, ACT_3, ACT_4, ACT_5, ACT_6, ACT_7
}

data class CurrencyBalance(
    val currency: SwipeCurrency,
    val amount: Int
)

data class ActProgress(
    val act: SwipeAct,
    val levelsAvailable: List<String>,
)

data class SwipeCharacterLevelInfo(
    val experience: Int,
    val maxExperience: Int,
    val level: Int,
)

data class ActCollectedReward(
    val act: SwipeAct,
    val level: String
)

data class SwipeCharacter(
    val name: String,
    val skin: String,
    val attributes: CharacterAttributes,
    val level: SwipeCharacterLevelInfo,
)

data class SwipeProfile(
    val balances: List<CurrencyBalance>,
    val actProgress: List<ActProgress>,
    val rewardsCollected: List<ActCollectedReward>?,
    val characters: List<SwipeCharacter>,
    val items: List<InventoryItem>,
) {
    private fun getRewardsCollectedOrEmpty() = rewardsCollected ?: emptyList()

    fun addBalance(currency: SwipeCurrency, amount: Int): SwipeProfile {
        var profile = this
        val now = balances.firstOrNull { it.currency == currency } ?: CurrencyBalance(currency, 0).also { profile = profile.copy(balances = profile.balances + it) }
        val new = now.copy(amount = now.amount + amount)
        return profile.copy(balances = profile.balances.map { if (it.currency == currency) new else it })
    }

    fun addItem(item: InventoryItem): SwipeProfile {
        val items = this.items + item
        return copy(items = items)
    }

    fun getBalance(currency: SwipeCurrency) = balances.firstOrNull { it.currency == currency }?.amount ?: 0

    fun updateLevel(skin: String, level: SwipeCharacterLevelInfo): SwipeProfile {
        return copy(characters = characters.map { c ->
            if (c.skin == skin) c.copy(level = level) else c
        })
    }

    fun isRewardCollected(act: SwipeAct, level: String) = getRewardsCollectedOrEmpty().none { it.act == act && it.level == level }
    fun modifyAttributes(skin: String, attributes: CharacterAttributes): SwipeProfile {
        return copy(characters = characters.map { c ->
            if (c.skin == skin) c.copy(attributes = attributes) else c
        })
    }
}
