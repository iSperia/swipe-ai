package com.pl00t.swipe_client.monster

import com.game7th.swipe.game.FrontMonsterConfiguration
import com.pl00t.swipe_client.Resources

class MonsterAttributesActor(
    r: Resources,
    model: FrontMonsterConfiguration,
) : AttributesActor(r, model) {

    init {
        handleDataLoaded()
    }
}
