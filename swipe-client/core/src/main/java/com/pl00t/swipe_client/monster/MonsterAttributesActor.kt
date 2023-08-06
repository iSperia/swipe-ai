package com.pl00t.swipe_client.monster

import com.game7th.swipe.game.FrontMonsterConfiguration
import com.pl00t.swipe_client.R

class MonsterAttributesActor(
    r: R,
    model: FrontMonsterConfiguration,
) : AttributesActor(r, model) {

    init {
        handleDataLoaded()
    }
}
