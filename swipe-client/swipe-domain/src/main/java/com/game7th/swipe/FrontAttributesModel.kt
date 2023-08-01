package com.game7th.swipe

import com.game7th.swipe.game.SbElemental
import com.game7th.swipe.game.SbMonsterConfiguration
import com.game7th.swipe.game.intAttribute

data class FrontAttributesModel(
    val body: String,
    val spirit: String,
    val mind: String,
    val health: String,
    val luck: Float,
    val ultProgress: Int,
    val ultMax: Int,
    val resists: SbElemental,
)

fun SbMonsterConfiguration.mapAsMonster() = FrontAttributesModel(
    body = "${attributes.body}%",
    spirit = "${attributes.spirit}%",
    mind = "${attributes.mind}%",
    health = balance.intAttribute("base_health").toString(),
    luck = 5.0f,
    ultProgress = 10,
    ultMax = 1000,
    resists = SbElemental()
)
