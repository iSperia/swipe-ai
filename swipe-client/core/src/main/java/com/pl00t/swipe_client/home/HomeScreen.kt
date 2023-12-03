package com.pl00t.swipe_client.home

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.game.SbSoundType
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.SbBaseScreen
import com.pl00t.swipe_client.atlas.AtlasWindow
import com.pl00t.swipe_client.battle.EncounterResultDialog
import com.pl00t.swipe_client.battle.BattleWindow
import com.pl00t.swipe_client.heroes.HeroDetailWindow
import com.pl00t.swipe_client.heroes.HeroListWindow
import com.pl00t.swipe_client.items.InventoryItemWindow
import com.pl00t.swipe_client.items.InventoryWindow
import com.pl00t.swipe_client.map.CampaignLevelWindow
import com.pl00t.swipe_client.map.MapWindow
import com.pl00t.swipe_client.map.RaidWindow
import com.pl00t.swipe_client.mine.presentation.MineWindow
import com.pl00t.swipe_client.monster.MonsterDetailWindow
import com.pl00t.swipe_client.screen.zephyr_shop.ZephyrShopWindow
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.services.profile.SwipeAct
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.async.KtxAsync

class HomeScreen(
    r: Resources
) : SbBaseScreen(r) {

    private var stack = StackDelegate(rootGroup)

    override fun loadScreenContent() {
        r.loadAtlas(Resources.ux_atlas)
        r.loadAtlas(Resources.units_atlas)
        r.loadAtlas(Resources.skills_atlas)
        r.loadSkin(Resources.SKIN)
        r.loadAtlas(Resources.atlas_atlas)
        SbSoundType.values().filter { !it.battle }.forEach {
            r.loadSound(it)
        }

        r.onLoad {
            val background = r.image(Resources.ux_atlas, "background_solid").apply {
                width = r.width
                height = r.height
            }
            rootGroup.addActor(background)

            r.skin().getFont("caption").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("caption30").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("regular").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("regular20").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("regular24").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            r.skin().getFont("regular24outline").getRegion().texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            hideSplash()
            showMap(SwipeAct.ACT_2)
        }

        KtxAsync.launch {
            delay(3000L)
            r.profileService.arcanumAddBalance().collect {
                val g = Group().apply {
                    setSize(480f, 80f)
                    setPosition(0f, r.height - 80f)
                }
                val bg = r.image(Resources.ux_atlas, "background_black").apply {
                    alpha = 0.8f
                    setSize(480f, 80f)
                }
                val label = r.regular24Focus("+$it").apply {
                    setSize(390f, 80f)
                    setPosition(90f, 0f)
                    setAlignment(Align.left)
                }
                val icon = r.image(Resources.ux_atlas, "ARCANUM").apply {
                    setSize(76f, 76f)
                    setPosition(2f, 2f)
                }
                g.addActor(bg)
                g.addActor(icon)
                g.addActor(label)
                g.touchable = Touchable.disabled
                g.addAction(Actions.sequence(
                    Actions.moveBy(0f, 50f),
                    Actions.moveBy(0f, -50f, 0.3f, SwingOut(1.6f)),
                    Actions.delay(2f),
                    Actions.alpha(0f, 1f),
                    Actions.removeActor()
                ))
                root.addActor(g)
            }
        }
    }

    private fun showMap(act: SwipeAct) {
        stack.moveBack()
        r.loadAtlas(Resources.actAtlas(act))
        r.onLoad {
            val mapActor = MapWindow(r, act, onLocationClicked =  { locationId ->
                KtxAsync.launch {
                    r.profileService.getAct(act).levels.firstOrNull { it.locationId == locationId }?.let { levelModel ->
                        if (levelModel.enabled) {
                            if (levelModel.type == LevelType.CAMPAIGN || (levelModel.type == LevelType.BOSS && r.profileService.isFreeRewardAvailable(act, levelModel.locationId))) {
                                val window = CampaignLevelWindow(
                                    r = r,
                                    model = levelModel,
                                    onClose = {
                                        stack.moveBack()
                                    },
                                    onMonsterClicked = { skin, level, rarity ->
                                        KtxAsync.launch {
                                            r.monsterService.createMonster(skin, level, rarity).let { monsterModel ->
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
                                    },
                                    openBattle = this@HomeScreen::openBattle
                                )
                                stack.showScreen(window)
                            } else if (levelModel.type == LevelType.RAID || levelModel.type == LevelType.BOSS) {
                                val window = RaidWindow(
                                    r = r,
                                    act = act,
                                    level = levelModel.locationId,
                                    onClose = { stack.moveBack() },
                                    onLaunch = this@HomeScreen::openBattle,
                                    onMonsterClicked = { config ->
                                        stack.showScreen(MonsterDetailWindow(r, config, onClose = { stack.moveBack() }))
                                    }
                                )
                                stack.showScreen(window)
                            } else if (levelModel.type == LevelType.ZEPHYR_SHOP) {
                                val window = ZephyrShopWindow(
                                    r = r,
                                    onClose = { stack.moveBack() }
                                )
                                stack.showScreen(window)
                            } else if (levelModel.type == LevelType.CRYSTAL_MINE) {
                                val window = MineWindow(
                                    r = r,
                                    stack = stack,
                                    onClose = { stack.moveBack() }
                                )
                                stack.showScreen(window)
                            }
                        }
                    }
                }
            }, navigateParty = {
                stack.showScreen(HeroListWindow(r, onClose = { stack.moveBack() }, onHeroSelected = { skin ->
                    KtxAsync.launch {
                        val heroConfig = r.profileService.createCharacter(skin)
                        stack.showScreen(HeroDetailWindow(r, heroConfig, { stack.moveBack() }, { id ->
                            val window = InventoryItemWindow(r, id, onClose = { stack.moveBack() })
                            stack.showScreen(window)
                        }))
                    }
                }))
            }, navigateInventory = {
                stack.showScreen(InventoryWindow(r, onClose = { stack.moveBack() }, onItemClicked = { id ->
                    KtxAsync.launch {
                        val window = InventoryItemWindow(r, id, onClose = { stack.moveBack() })
                        stack.showScreen(window)
                    }
                }))
            }, navigateAtlas = {
                stack.moveBack()
                stack.showScreen(AtlasWindow(r, this::showMap))
            }).apply {
                alpha = 0f
                addAction(Actions.alpha(1f, 0.3f))
            }
            stack.showScreen(mapActor)
        }
    }

    private fun openBattle() {
        openBattle(true)
    }

    private fun openBattle(popStack: Boolean) {
        if (popStack) stack.moveBack()
        stack.showScreen(BattleWindow(r, { result ->
            stack.moveBack()
            stack.showScreen(EncounterResultDialog(r, result, onClose = {
                stack.moveBack()
            }, onItemClick = {
                stack.showScreen(InventoryItemWindow(
                    r = r,
                    id = it,
                    onClose = { stack.moveBack() }
                ))
            }, onStartLevel = this@HomeScreen::openBattle))
        }))
    }
}
