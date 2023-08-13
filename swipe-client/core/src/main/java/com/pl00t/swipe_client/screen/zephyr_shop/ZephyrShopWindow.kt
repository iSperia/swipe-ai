package com.pl00t.swipe_client.screen.zephyr_shop

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.analytics.AnalyticEvents
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.ItemBrowser
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync

class ZephyrShopWindow(
    private val r: Resources,
    private val onClose: () -> Unit,
): Group() {

    private val content: Table = Table().apply {
        width = 480f
    }
    private val scrollPane = ScrollPane(content).apply {
        width = r.width
        height = r.height - 190f
        y = 110f
    }

    private var title: WindowTitleActor? = null
    private var bottomPanel: BottomActionPanel? = null

    init {
        setSize(r.width, r.height)
        val texture = r.image(Resources.ux_atlas, "texture_screen").apply { setSize(r.width, r.height); setScaling(
            Scaling.fillY); setColor(r.skin().getColor("rarity_3")) }
        val backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(texture)
        addActor(backgroundShadow)
        addActor(scrollPane)

        loadData()
        addTitle()

    }

    private fun loadData() {
        KtxAsync.launch {
            content.clearChildren()

            val drawable = r.atlas(Resources.actAtlas(SwipeAct.ACT_1)).findRegion("zephyr_shop").let {
                val x1 = it.u
                val x2 = it.u2
                val y1 = it.v
                val y2 = it.v2
                val d = y2 - y1
                TextureRegionDrawable(TextureRegion(it.texture, x1, y1 + d * 0.25f, x2, y1 + d * 0.75f))
            }
            val image = Image(drawable).apply {
                width = 480f
                height = 240f
                setScaling(Scaling.stretch)
            }
            content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f)}).colspan(3).size(480f, 1f).row()
            content.add(image).size(480f, 240f).colspan(3).row()
            content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f) }).colspan(3).size(480f, 1f).row()


            val items = r.profileService.getMysteryShop().sortedByDescending { it.first.rarity * 100000 + it.second }
            val browser = ItemBrowser(r, items.map { it.first }, null) { item ->
                val entry = items.first { it.first == item }
                val cost = entry.second
                val enoughCoins = r.profileService.getProfile().getBalance(SwipeCurrency.ETHERIUM_COIN) >= cost
                ActionCompositeButton(
                    r,
                    Action.ItemDetails(SwipeCurrency.ETHERIUM_COIN.toString()),
                    Mode.SingleLine("$cost\n${UiTexts.ZephyrShop.Buy.value(r.l)}"),
                    !enoughCoins
                ).apply {
                    onClick {
                        KtxAsync.launch {
                            r.analytics.trackEvent(AnalyticEvents.MysteryShopEvent.EVENT_PURCHASE, mapOf(AnalyticEvents.MysteryShopEvent.KEY_COST to entry.second.toString()))
                            r.profileService.buyMysteryItem(entry.first)
                            loadData()
                        }
                    }
                }
            }
            content.add(browser).row()
            addBottomPanel()


            content.add().growY().row()
        }
    }

    private fun addTitle() {
        title?.remove()

        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        title = WindowTitleActor(r, UiTexts.ZephyrShop.Title.value(r.l), closeButton, null, 3).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun addBottomPanel() {
        bottomPanel?.remove()
        KtxAsync.launch {
            val arcanum = r.profileService.getProfile().getBalance(SwipeCurrency.ARCANUM)
            val enoughForReroll = arcanum >= 300
            val coins = r.profileService.getProfile().getBalance(SwipeCurrency.ETHERIUM_COIN)
            val upgradeCost = r.profileService.getMysteryShopUpgradeCost()
            val enoughForUpgrade = upgradeCost > 0 && coins >= r.profileService.getMysteryShopUpgradeCost()
            val labelUpgrade = if (upgradeCost == 0) {
                UiTexts.ZephyrShop.MaxLevel.value(r.l)
            } else {
                "$upgradeCost\n${UiTexts.ZephyrShop.Upgrade.value(r.l)}"
            }
            val actions = listOf(
                ActionCompositeButton(r, Action.ItemDetails(SwipeCurrency.ETHERIUM_COIN.toString()), Mode.SingleLine(labelUpgrade), !enoughForUpgrade).apply {
                    onClick {
                        KtxAsync.launch {
                            r.profileService.upgradeMysteryShop()
                            r.analytics.trackEvent(AnalyticEvents.MysteryShopEvent.EVENT_UPGRADE, mapOf(AnalyticEvents.MysteryShopEvent.KEY_TIER to r.profileService.getProfile().mysteryShopLevel.toString()))
                            loadData()
                        }
                    }
                },
                ActionCompositeButton(r, Action.ItemDetails(SwipeCurrency.ARCANUM.toString()), Mode.SingleLine("300/${r.profileService.getProfile().getBalance(SwipeCurrency.ARCANUM)}\n${UiTexts.ZephyrShop.Reroll.value(r.l)}"), !enoughForReroll).apply {
                    onClick {
                        KtxAsync.launch {
                            r.analytics.trackEvent(AnalyticEvents.MysteryShopEvent.EVENT_REFRESH)
                            r.profileService.rerollMysteryShop()
                            loadData()
                        }
                    }
                }
            )
            val panel = BottomActionPanel(r, actions, 3)
            addActor(panel)

        }
    }

}
