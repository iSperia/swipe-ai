package com.pl00t.swipe_client.battle

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.game.SbSoundType
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.services.battle.BattleResult
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.ItemCellActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class BattleResultDialog(
    private val r: Resources,
    private val result: BattleResult,
    private val onClose: () -> Unit,
    private val onItemClick: (String) -> Unit,
    private val onStartLevel: (Boolean) -> Unit
) : Group() {

    private val content = Table().apply {
        width = r.width
    }
    private val scrollPane = ScrollPane(content).apply {
        setSize(r.width, r.height - 190f)
        y = 110f
    }
    lateinit var title: WindowTitleActor
    lateinit var bottomPanel: BottomActionPanel
    lateinit protected var background: Image
    lateinit protected var backgroundShadow: Image

    private var extraRewards: List<FrontItemEntryModel>? = null
    private var hasCoins = false

    init {
        background = r.image(Resources.ux_atlas, "texture_screen").apply { setSize(r.width, r.height); alpha = 0.5f; color = r.skin().getColor("rarity_${if (result.victory) 4 else 0}") }
        backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(background)
        addActor(backgroundShadow)

        addActor(scrollPane)
        addTitle()
        addBottomPanel()

        loadData()

        if (result.victory) {
            r.loadSound(SbSoundType.FANFARE)
            r.onLoad {
                r.playSound(SbSoundType.FANFARE)
            }
        } else {
            r.loadSound(SbSoundType.DEFEAT_MUSIC)
            r.onLoad {
                r.playSound(SbSoundType.DEFEAT_MUSIC)
            }
        }
    }

    private fun addTitle() {
        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        title = WindowTitleActor(r, if (result.victory) UiTexts.BattleVictory.value(r.l) else UiTexts.BattleDefeat.value(r.l),
            closeButton, null, if (result.victory) 4 else 0).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun addBottomPanel() {
        KtxAsync.launch {
            var actions = mutableListOf<ActionCompositeButton>()

            if (result.victory && result.extraRewardsCost > 0) {
                val enoughCoins = r.profileService.getProfile().getBalance(SwipeCurrency.ETHERIUM_COIN) >= result.extraRewardsCost
                val collectReward = ActionCompositeButton(r, Action.ItemDetails(SwipeCurrency.ETHERIUM_COIN.toString()), Mode.Purchase(UiTexts.RaidCollectRewards.value(r.l), SwipeCurrency.ETHERIUM_COIN, result.extraRewardsCost))

                if (enoughCoins) {
                    collectReward.onClick {
                        KtxAsync.launch {
                            collectReward.touchable = Touchable.disabled
                            collectReward.alpha = 0.5f
                            extraRewards = r.profileService.collectRichReward(result.act, result.level, result.tier, result.extraRewardsCost)
                            loadData()
                        }
                    }
                } else {
                    collectReward.touchable = Touchable.disabled
                    collectReward.alpha = 0.5f
                }
                actions.add(collectReward)
            }

            if (!result.victory) {
                val retry = ActionCompositeButton(r, Action.Complete, Mode.SingleLine(UiTexts.RetryLevel.value(r.l)))
                retry.onClick {
                    KtxAsync.launch {
                        r.battleService.createBattle(result.act, result.level, result.tier)
                        onStartLevel(true)
                    }
                }
                actions.add(retry)
            }

            bottomPanel = BottomActionPanel(r, actions, if (result.victory) 4 else 0)
            addActor(bottomPanel)
        }
    }

    private fun loadData() {
        KtxAsync.launch {
            content.clearChildren()

            val enoughCoins = extraRewards != null || r.profileService.getProfile().getBalance(SwipeCurrency.ETHERIUM_COIN) >= result.extraRewardsCost

            content.add(r.image(Resources.ux_atlas, "background_black").apply { alpha = 0.5f; setSize(480f, 1f) }).size(480f, 1f).colspan(4).row()
            val imageBackground = if (result.victory) "background_victory" else "background_defeat"
            content.add(r.image(Resources.ux_atlas, imageBackground).apply { setSize(480f, 240f) }).size(480f, 240f).colspan(4).row()
            content.add(r.image(Resources.ux_atlas, "background_black").apply { alpha = 0.5f; setSize(480f, 1f) }).size(480f, 1f).colspan(4).row()

            if (result.exp != null) {
                val group = Group().apply {
                    setSize(480f, 100f)
                }
                val skin = r.image(Resources.units_atlas, result.exp.skin).apply {
                    setSize(60f, 100f)
                }
                val name = r.regular24Focus(result.exp.name.value(r.l)).apply {
                    setSize(410f, 30f)
                    setPosition(70f, 70f)
                    setAlignment(Align.left)
                }
                val expCount = r.regular24White(UiTexts.ExpBoost.value(r.l).replace("$", result.exp.expBoost.toString())).apply {
                    setSize(410f, 30f)
                    setPosition(70f, 40f)
                    setAlignment(Align.left)
                }
                group.addActor(name)
                group.addActor(expCount)
                group.addActor(skin)

                content.add(group).size(480f, 100f).padTop(10f).padBottom(10f).colspan(4).row()
            }

            if (!enoughCoins) {
                content.add(r.regular24Error(UiTexts.RaidLittleCoins.value(r.l)).apply { width = 480f }).width(480f).colspan(5).row()
            }

            extraRewards?.let { rewards ->
                content.add(r.regular24Focus(UiTexts.RaidRichRewards.value(r.l)).apply { width = 480f; setAlignment(Align.center) }).width(480f).colspan(5).row()
                rewards.forEachIndexed { i, reward ->
                    val actor = ItemCellActor(r, reward).apply {
                        if (reward.currency != null) {
                            touchable = Touchable.disabled
                        } else {
                            onClick { onItemClick(reward.item!!.id) }
                        }
                    }
                    content.add(actor).size(120f, 160f)
                    if (i % 4 == 3) content.row()
                }
                content.add().growX()
                content.row()
            }

            if (result.freeRewards.isNotEmpty()) {
                content.add(r.regular24Focus(UiTexts.RaidFreeRewards.value(r.l)).apply { width = 480f; setAlignment(Align.center) }).width(480f).colspan(5).row()
            }
            result.freeRewards.forEachIndexed { i, reward ->
                val actor = ItemCellActor(r, reward).apply {
                    if (reward.currency != null) {
                        touchable = Touchable.disabled
                    } else {
                        onClick { onItemClick(reward.item!!.id) }
                    }
                }
                content.add(actor).size(120f, 160f)
                if (i % 4 == 3) content.row()
            }
            content.add().growX()
            content.row()

            content.row()
            content.add().growY()
        }
    }

}
