package com.pl00t.swipe_client.battle

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.swipe.game.SbSoundType
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.analytics.AnalyticEvents
import com.pl00t.swipe_client.services.battle.EncounterResultModel
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.*
import com.pl00t.swipe_client.ux.dialog.DialogScriptActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync

class EncounterResultDialog(
    private val r: Resources,
    private val result: EncounterResultModel,
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
    private var isVictory = false
    lateinit var title: WindowTitleActor
    lateinit var bottomPanel: BottomActionPanel
    lateinit protected var background: Image
    lateinit protected var backgroundShadow: Image

    private var extraRewards: List<FrontItemEntryModel>? = null
    private var hasCoins = false

    init {
        isVictory = when (result) {
            is EncounterResultModel.BattleResult -> result.victory
            is EncounterResultModel.MineResult -> result.gems.isNotEmpty()
        }
        background = r.image(Resources.ux_atlas, "texture_screen").apply {
            setSize(r.width, r.height); alpha = 0.5f; color =
            r.skin().getColor("rarity_${if (isVictory) 4 else 0}")
        }
        backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(background)
        addActor(backgroundShadow)

        addActor(scrollPane)
        addTitle()
        addBottomPanel()

        loadData()

        if (isVictory) {
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

        when (result) {
            is EncounterResultModel.BattleResult -> {
                if (isVictory) {
                    r.analytics.trackEvent(AnalyticEvents.BattleEvent.EVENT_BATTLE_VICTORY, AnalyticEvents.BattleEvent.create(result.act, result.level, result.tier))
                } else {
                    r.analytics.trackEvent(AnalyticEvents.BattleEvent.EVENT_BATTLE_DEFEAT, AnalyticEvents.BattleEvent.create(result.act, result.level, result.tier))
                }
            }
            is EncounterResultModel.MineResult -> {
                r.analytics.trackEvent(AnalyticEvents.MineEvent.EVENT_RESULT, AnalyticEvents.MineEvent.createResult(result.level, result.gems.size))
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
        val windowTitle = when (result) {
            is EncounterResultModel.BattleResult -> {
                if (isVictory) UiTexts.BattleVictory.value(r.l) else UiTexts.BattleDefeat.value(r.l)
            }
            is EncounterResultModel.MineResult -> UiTexts.MineResult.value(r.l)
        }

        title = WindowTitleActor(
            r, windowTitle,
            closeButton, null, if (isVictory) 4 else 0
        ).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun addBottomPanel() {
        KtxAsync.launch {
            var actions = mutableListOf<ActionCompositeButton>()

            when (result) {
                is EncounterResultModel.BattleResult -> {
                    if (result.victory && result.extraRewardsCost > 0) {
                        val enoughArcanum =
                            r.profileService.getProfile().getBalance(SwipeCurrency.ARCANUM) >= result.extraRewardsCost
                        val collectReward = ActionCompositeButton(
                            r,
                            Action.ItemDetails(SwipeCurrency.ARCANUM.toString()),
                            Mode.Purchase(
                                UiTexts.RaidCollectRewards.value(r.l),
                                SwipeCurrency.ETHERIUM_COIN,
                                result.extraRewardsCost
                            )
                        )

                        if (enoughArcanum) {
                            collectReward.onClick {
                                KtxAsync.launch {
                                    collectReward.touchable = Touchable.disabled
                                    collectReward.alpha = 0.5f
                                    extraRewards = r.profileService.collectRichReward(
                                        result.act,
                                        result.level,
                                        result.tier,
                                        result.extraRewardsCost
                                    )
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
                                r.analytics.trackEvent(AnalyticEvents.BattleEvent.EVENT_BATTLE_START, AnalyticEvents.BattleEvent.create(result.act, result.level, result.tier))
                                r.battleService.createBattle(result.act, result.level, result.tier)
                                onStartLevel(true)
                            }
                        }
                        actions.add(retry)
                    }
                }
                else -> Unit
            }

            bottomPanel = BottomActionPanel(r, actions, if (isVictory) 4 else 0)
            addActor(bottomPanel)
        }
    }

    private fun loadData() {
        KtxAsync.launch {
            content.clearChildren()

            when (result) {
                is EncounterResultModel.BattleResult -> loadBattleResult()
                is EncounterResultModel.MineResult -> loadMineResult()
            }



            content.row()
            content.add().growY()

            checkTutorial()
        }
    }

    private suspend fun loadBattleResult() {
        val result = this@EncounterResultDialog.result as? EncounterResultModel.BattleResult ?: return

        val enoughArcanum = extraRewards != null || r.profileService.getProfile()
            .getBalance(SwipeCurrency.ARCANUM) >= result.extraRewardsCost

        content.add(r.image(Resources.ux_atlas, "background_black").apply { alpha = 0.5f; setSize(480f, 1f) })
            .size(480f, 1f).colspan(4).row()
        val imageBackground = if (result.victory) "background_victory" else "background_defeat"
        content.add(r.image(Resources.ux_atlas, imageBackground).apply { setSize(480f, 240f) }).size(480f, 240f)
            .colspan(4).row()
        content.add(r.image(Resources.ux_atlas, "background_black").apply { alpha = 0.5f; setSize(480f, 1f) })
            .size(480f, 1f).colspan(4).row()

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
            val expCount =
                r.regular24White(UiTexts.ExpBoost.value(r.l).replace("$", result.exp.expBoost.toString())).apply {
                    setSize(410f, 30f)
                    setPosition(70f, 40f)
                    setAlignment(Align.left)
                }
            group.addActor(name)
            group.addActor(expCount)
            group.addActor(skin)

            content.add(group).size(480f, 100f).padTop(10f).padBottom(10f).colspan(4).row()
        }

        if (!enoughArcanum) {
            content.add(r.regular24Error("${UiTexts.RaidLittleArcanum.value(r.l)} (${r.profileService.getProfile().getBalance(SwipeCurrency.ARCANUM)}/${result.extraRewardsCost})").apply { width = 460f }).width(460f)
                .colspan(5).row()
        }

        extraRewards?.let { rewards ->
            content.add(
                r.regular24Focus(UiTexts.RaidRichRewards.value(r.l))
                    .apply { width = 480f; setAlignment(Align.center) }).width(480f).colspan(5).row()
            val browser = ItemBrowser(r, rewards, null, null)
            content.add(browser).row()
        }

        if (result.freeRewards.isNotEmpty()) {
            content.add(
                r.regular24Focus(UiTexts.RaidFreeRewards.value(r.l))
                    .apply { width = 480f; setAlignment(Align.center) }).width(480f).colspan(5).row()

            val browser = ItemBrowser(r, result.freeRewards, null, null)
            content.add(browser).row()
        }
    }

    private suspend fun loadMineResult() {
        val result = result as? EncounterResultModel.MineResult ?: return

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
        g.addActor(image)
        content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f)}).colspan(3).size(480f, 1f).row()
        content.add(g).size(480f, 240f).colspan(3).row()
        content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f) }).colspan(3).size(480f, 1f).row()

        if (isVictory) {
            val browser = ItemBrowser(r, result.gems, null, null)
            content.add(browser).row()
        } else {
            content.add(r.regular24Error("${UiTexts.MineNoGemsFound.value(r.l)}").apply { width = 460f }).width(460f)
                .colspan(5).row()
        }
    }

    private suspend fun checkTutorial() {
        val result = result as? EncounterResultModel.BattleResult ?: return
        if (!r.profileService.getTutorial().a1c1ResultPassed && result.act == SwipeAct.ACT_1 && result.level == "c1") {
            addActor(DialogScriptActor(r, r.profileService.getDialogScript("a1c1result")) {
                addActor(
                    TutorialHover(
                        r,
                        content.getChild(3).bounds(),
                        UiTexts.Tutorials.A1C1R1,
                        HoverAction.HoverClick(false) {
                            if (content.children.size > 5) {
                                addActor(
                                    TutorialHover(
                                        r,
                                        content.getChild(5).bounds(),
                                        UiTexts.Tutorials.A1C1R2,
                                        HoverAction.HoverClick(false) {
                                            r.profileService.saveTutorial(
                                                r.profileService.getTutorial().copy(a1c1ResultPassed = true)
                                            )
                                        })
                                )
                            } else {
                                r.profileService.saveTutorial(
                                    r.profileService.getTutorial().copy(a1c1ResultPassed = true)
                                )
                            }
                        })
                )
            })
        } else if (result.tier == -1) {
            if (result.victory) {
                val dialogScript = r.profileService.getDialogScript("${result.act}.${result.level}.f")
                if (dialogScript.replicas.isNotEmpty()) {
                    addActor(DialogScriptActor(r, dialogScript) {

                    })
                }
            }
        }
    }
}
