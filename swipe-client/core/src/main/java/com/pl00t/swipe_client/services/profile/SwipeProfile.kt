package com.pl00t.swipe_client.services.profile

import com.pl00t.swipe_client.services.battle.UnitSkin
import com.pl00t.swipe_client.services.battle.logic.CharacterAttributes

enum class SwipeCurrency {
    ETHERIUM_COIN, ESSENCE_FRAGMENT, TIME_SHARD, SPARK_OF_INSIGHT, EXPERIENCE_CRYSTAL, EXPERIENCE_RELIC
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
    val experience: Long,
    val maxExperience: Long,
    val level: Int,
)

data class ActCollectedReward(
    val act: SwipeAct,
    val level: String
)

data class SwipeCharacter(
    val skin: UnitSkin,
    val attributes: CharacterAttributes,
    val level: SwipeCharacterLevelInfo,
)

data class SwipeProfile(
    val balances: List<CurrencyBalance>,
    val actProgress: List<ActProgress>,
    val rewardsCollected: List<ActCollectedReward>?,
//    val characters: List<SwipeCharacter>
) {
    private fun getRewardsCollectedOrEmpty() = rewardsCollected ?: emptyList()

    fun addBalance(currency: SwipeCurrency, amount: Int): SwipeProfile {
        var profile = this
        val now = balances.firstOrNull { it.currency == currency } ?: CurrencyBalance(currency, 0).also { profile = profile.copy(balances = profile.balances + it) }
        val new = now.copy(amount = now.amount + amount)
        return profile.copy(balances = profile.balances.map { if (it.currency == currency) new else it })
    }

    fun getBalance(currency: SwipeCurrency) = balances.firstOrNull { it.currency == currency }?.amount ?: 0

    fun isRewardCollected(act: SwipeAct, level: String) = getRewardsCollectedOrEmpty().none { it.act == act && it.level == level }
}
