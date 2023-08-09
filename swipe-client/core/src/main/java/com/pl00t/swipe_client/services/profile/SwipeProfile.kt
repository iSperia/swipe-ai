package com.pl00t.swipe_client.services.profile

import com.game7th.items.InventoryItem
import com.game7th.swipe.game.CharacterAttributes

enum class SwipeCurrency(val expBonus: Int = 0, val coins: Int = 1) {
    ETHERIUM_COIN,
    SCROLL_OF_WISDOM(expBonus = 750, coins = 100),
    TOME_OF_ENLIGHTMENT(expBonus = 2500, coins = 300),
    CODEX_OF_ASCENDANCY(expBonus = 7500, coins = 900),
    GRIMOIRE_OF_OMNISCENCE(expBonus = 25000, coins = 3000),
    INFUSION_ORB(expBonus = 750, coins = 100),
    INFUSION_SHARD(expBonus = 2500, coins = 300),
    INFUSION_CRYSTAL(expBonus = 7500, coins = 900),
    ASCENDANT_ESSENCE(expBonus = 25000, coins = 3000),
    ELIXIR_AMBER(expBonus = 0, coins = 1000),
    ELIXIR_TURQUOISE(expBonus = 0, coins = 1000),
    ELIXIR_LAPIS(expBonus = 0, coins = 1000),
    ELIXIR_CITRINE(expBonus = 0, coins = 1000),
    ELIXIR_JADE(expBonus = 0, coins = 1000),
    ELIXIR_AGATE(expBonus = 0, coins = 1000),
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
    val skin: String,
    val attributes: CharacterAttributes,
    val experience: Int,
) {
    companion object {
        val experience = mutableListOf<Int>()

        init {
            (0 until 100).forEach { i ->
                if (i == 0) {
                    0
                } else {
                    experience[i-1] + 1000 + (i-1) * 500
                }.let { experience.add(it) }
            }
        }

        fun getLevel(exp: Int): Int {
            return experience.indexOfFirst { it > exp }
        }
    }
}

data class LevelTierUnlocked(
    val tier: Int,
    val level: String,
    val act: SwipeAct
)

data class SwipeProfile(
    val balances: List<CurrencyBalance>,
    val actProgress: List<ActProgress>,
    val rewardsCollected: List<ActCollectedReward>?,
    val tiersUnlocked: List<LevelTierUnlocked>?,
    val characters: List<SwipeCharacter>,
    val items: List<InventoryItem>,
    val mysteryShop: List<SbMysteryItem>?,
    val activeCharacter: String?,
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

    fun updateLevel(skin: String, experience: Int): SwipeProfile {
        return copy(characters = characters.map { c ->
            if (c.skin == skin) c.copy(experience = experience) else c
        })
    }

    fun isRewardCollected(act: SwipeAct, level: String) = getRewardsCollectedOrEmpty().none { it.act == act && it.level == level }
    fun modifyAttributes(skin: String, attributes: CharacterAttributes): SwipeProfile {
        return copy(characters = characters.map { c ->
            if (c.skin == skin) c.copy(attributes = attributes) else c
        })
    }
}
