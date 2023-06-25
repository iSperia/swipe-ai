package com.pl00t.swipe_client.services.profile

enum class SwipeCurrency {
    ETHERIUM_COIN, TIME_SHARD, SPARK_OF_INSIGHT, EXPERIENCE_CRYSTAL, EXPERIENCE_RELIC
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
    val levelsAvailable: List<String>
)

data class SwipeProfile(
    val balances: List<CurrencyBalance>,
    val actProgress: List<ActProgress>
) {
    fun getBalance(currency: SwipeCurrency) = balances.firstOrNull { it.currency == currency }?.amount ?: 0
}
