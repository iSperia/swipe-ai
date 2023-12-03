package com.pl00t.swipe_client.mine.presentation

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.analytics.AnalyticEvents
import com.pl00t.swipe_client.home.StackDelegate
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class MineWindow(
    private val r: Resources,
    private val stack: StackDelegate,
    private val onClose: () -> Unit
) : Group() {

    private val content = Table().apply {
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

        addTitle()
        loadData()
    }

    private fun addTitle() {
        title?.remove()

        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        title = WindowTitleActor(r, UiTexts.Mine.Title.value(r.l), closeButton, null, 3).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun loadData() {
        KtxAsync.launch {
            addBottomPanel()

            content.clearChildren()

            val drawable = r.atlas(Resources.actAtlas(SwipeAct.ACT_2)).findRegion("crystal_mines").let {
                val x1 = it.u
                val x2 = it.u2
                val y1 = it.v
                val y2 = it.v2
                val d = y2 - y1
                TextureRegionDrawable(TextureRegion(it.texture, x1, y1 + d * 0.25f, x2, y1 + d * 0.75f))
            }
            val g = Group().apply {
                setSize(480f, 240f)
            }
            val image = Image(drawable).apply {
                width = 480f
                height = 240f
                setScaling(Scaling.stretch)
            }
            val balance = r.profileService.getProfile().getBalance(SwipeCurrency.ETHERIUM_COIN)
            val label = r.regular20Focus("${UiTexts.Mine.Balance.value(r.l)}$balance").apply {
                setSize(450f, 30f)
                setPosition(30f, 0f)
                setAlignment(Align.left)
            }
            val shadow = r.image(Resources.ux_atlas, "background_black").apply {
                alpha = 0.5f
                setSize(480f, 30f)
            }
            val icon = r.image(Resources.ux_atlas, SwipeCurrency.ETHERIUM_COIN.toString()).apply {
                setSize(30f, 30f)
            }
            g.addActor(image)
            g.addActor(shadow)
            g.addActor(label)
            g.addActor(icon)
            content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f)}).colspan(3).size(480f, 1f).row()
            content.add(g).size(480f, 240f).colspan(3).row()
            content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f) }).colspan(3).size(480f, 1f).row()

            content.add(r.regular24Focus(UiTexts.Mine.CaptionLevel.value(r.l))).height(60f).pad(10f).fillX().row()
            content.add(r.regular20White((r.mineService.level() + 1).toString()).apply { wrap = true }).width(460f).pad(10f).row()

            content.add(r.regular24Focus(UiTexts.Mine.CaptionMaxGemTier.value(r.l))).height(60f).pad(10f).fillX().row()
            content.add(r.regular20White(r.mineService.getMaxTier().toString()).apply { wrap = true }).width(460f).pad(10f).row()

            content.add(r.regular24Focus(UiTexts.Mine.CaptionAttempts.value(r.l))).height(60f).pad(10f).fillX().row()
            content.add(r.regular20White(r.mineService.getAttemptsPerTry().toString()).apply { wrap = true }).width(460f).pad(10f).row()


            content.add().growY().row()
        }
    }

    private fun addBottomPanel() {
        bottomPanel?.remove()
        KtxAsync.launch {
            val arcanum = r.profileService.getProfile().getBalance(SwipeCurrency.ARCANUM)
            val enoughForMine = arcanum >= 300
            val coins = r.profileService.getProfile().getBalance(SwipeCurrency.ETHERIUM_COIN)
            val upgradeCost = r.mineService.getUpgradeCost()
            val enoughForUpgrade = upgradeCost > 0 && coins >= r.mineService.getUpgradeCost()
            val labelUpgrade = if (r.mineService.maxLevel() <= r.mineService.level()) {
                UiTexts.ZephyrShop.MaxLevel.value(r.l)
            } else {
                "$upgradeCost\n${UiTexts.Mine.Upgrade.value(r.l)}"
            }
            val actions = listOf(
                ActionCompositeButton(r, Action.ItemDetails(SwipeCurrency.ETHERIUM_COIN.toString()), Mode.SingleLine(labelUpgrade), !enoughForUpgrade).apply {
                    onClick {
                        KtxAsync.launch {
                            r.mineService.levelUp()
                            r.profileService.upgradeMysteryShop()
                            r.analytics.trackEvent(AnalyticEvents.MineEvent.EVENT_UPGRADE, mapOf(AnalyticEvents.MineEvent.KEY_LEVEL to r.mineService.level().toString()))
                            loadData()
                        }
                    }
                },
                ActionCompositeButton(r, Action.ItemDetails(SwipeCurrency.ARCANUM.toString()), Mode.SingleLine("300/${r.profileService.getProfile().getBalance(SwipeCurrency.ARCANUM)}\n${UiTexts.Mine.Delve.value(r.l)}"), !enoughForMine).apply {
                    onClick {
                        KtxAsync.launch {
                            r.analytics.trackEvent(AnalyticEvents.MineEvent.EVENT_LAUNCH, mapOf(AnalyticEvents.MineEvent.KEY_LEVEL to r.mineService.level().toString()))
                            stack.showScreen(DelveWindow(
                                r = r
                            ))
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
