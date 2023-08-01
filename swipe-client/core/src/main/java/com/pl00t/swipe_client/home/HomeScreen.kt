package com.pl00t.swipe_client.home

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.SbBaseScreen
import com.pl00t.swipe_client.map.CampaignLevelWindow
import com.pl00t.swipe_client.map.MapWindow
import com.pl00t.swipe_client.monster.MonsterDetailWindow
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.services.profile.SwipeAct
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.async.KtxAsync

class HomeScreen(
    r: R
) : SbBaseScreen(r) {

    lateinit var actId: SwipeAct

    private var stack = StackDelegate(root)

    override fun loadScreenContent() {
        actId = SwipeAct.ACT_1

        r.loadAtlas(R.actAtlas(actId))
        r.loadAtlas(R.ux_atlas)
        r.loadAtlas(R.units_atlas)
        r.loadSkin(R.SKIN)

        r.onLoad {
            val background = r.image(R.ux_atlas, "background_solid").apply {
                width = r.width
                height = r.height
            }
            root.addActor(background)

            r.skin().getFont("caption").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("caption30").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("regular").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("regular20").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("regular24").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            hideSplash()
            showMap()
        }
    }

    private fun showMap() {
        val mapActor = MapWindow(r, actId) { locationId ->
            KtxAsync.launch {
                r.profileService.getAct(actId).levels.firstOrNull { it.locationId == locationId }?.let { levelModel ->
                    if (levelModel.enabled) {
                        if (levelModel.type == LevelType.CAMPAIGN) {
                            val window = CampaignLevelWindow(
                                r = r,
                                model = levelModel,
                                onClose = {
                                    stack.moveBack()
                                },
                                onMonsterClicked = { skin ->
                                    KtxAsync.launch {
                                        r.monsterService.getMonster(skin)?.let { monsterModel ->
                                            val window = MonsterDetailWindow(
                                                r = r,
                                                model = monsterModel,
                                                onClose = {
                                                    stack.moveBack()
                                                }
                                            )
                                            stack.showScreen(window)
                                        }
                                    }
                                }
                            )
                            stack.showScreen(window)
                        }
                    }
                }
            }
        }.apply {
            alpha = 0f
            addAction(Actions.alpha(1f, 0.3f))
        }
        stack.showScreen(mapActor)
    }
}
