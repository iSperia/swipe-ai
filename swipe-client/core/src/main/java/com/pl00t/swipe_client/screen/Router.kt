package com.pl00t.swipe_client.screen

import com.pl00t.swipe_client.services.profile.SwipeAct

interface Router {
    fun navigateBattle(act: SwipeAct, locationId: String)
    fun navigateMap(act: SwipeAct)

}
