package com.pl00t.swipe_client.items

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.ItemCellActor
import com.pl00t.swipe_client.ux.ItemRowActor
import com.pl00t.swipe_client.ux.LevelProgressActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync
import kotlin.math.min

private enum class BrowseMode {
    DETAILS, DUST,
}

class InventoryItemWindow(
    protected val r: Resources,
    protected var id: String,
    private val onClose: () -> Unit
) : Group() {

    private lateinit var model: FrontItemEntryModel

    protected var balances = arrayOf(0, 0, 0, 0)
    protected var useCount = arrayOf(0, 0, 0, 0)
    protected var baseLevel: Int = 0
    protected var baseExp: Int = 0
    protected var boostExp: Int = 0

    private var levelProgressActor: LevelProgressActor? = null

    private val content = Table().apply {
        width = 480f
    }
    private val scrollPane = ScrollPane(content).apply {
        setSize(480f, r.height - 190f)
        setPosition(0f, 110f)
    }

    private var mode = BrowseMode.DETAILS

    lateinit var title: WindowTitleActor
    lateinit var bottomPanel: BottomActionPanel
    lateinit protected var background: Image
    lateinit protected var backgroundShadow: Image

    init {
        setSize(r.width, r.height)

        KtxAsync.launch {
            loadData()

            background = r.image(Resources.ux_atlas, "texture_screen").apply { setSize(r.width, r.height); alpha = 0.5f; color = r.skin().getColor("rarity_${model.rarity}") }
            backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
            addActor(background)
            addActor(backgroundShadow)

            addActor(scrollPane)

            addTitle()
            addBottomPanel()
        }
    }

    private fun addTitle() {
        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        title = WindowTitleActor(r, model.name.value(r.l), closeButton, null, model.rarity).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun addBottomPanel() {
        KtxAsync.launch {
            val actions = listOf(
                ActionCompositeButton(r, Action.Stats, Mode.SingleLine(UiTexts.ButtonStats.value(r.l))).apply {
                    onClick {
                        KtxAsync.launch {
                            mode = BrowseMode.DETAILS
                            loadData()
                        }
                    }
                },
                ActionCompositeButton(r, Action.Close, Mode.SingleLine(UiTexts.Dust.value(r.l))).apply {
                    onClick {
                        KtxAsync.launch {
                            if (mode == BrowseMode.DUST) {
                                r.profileService.dustItem(model.item!!.id)
                                onClose()
                            } else {
                                mode = BrowseMode.DUST
                                loadData()
                            }
                        }
                    }
                }
            )
            bottomPanel = BottomActionPanel(r, actions, model.rarity)
            addActor(bottomPanel)
        }
    }

    private suspend fun loadData() {
        content.clearChildren()
        val item = r.profileService.getItems().first { it.id == id }
        val template = r.itemService.getItemTemplate(item.skin)!!
        model = FrontItemEntryModel(
            skin = item.skin,
            amount = 1,
            level = SwipeCharacter.getLevel(item.experience),
            rarity = item.rarity,
            name = template.name,
            currency = null,
            item = item
        )

        when (mode) {
            BrowseMode.DETAILS -> {
                showDetails()
            }
            BrowseMode.DUST -> {
                showDust()
            }
        }

        content.row()
        content.add().growY()
    }

    private suspend fun showDetails() {
        val cs = arrayOf(SwipeCurrency.INFUSION_ORB, SwipeCurrency.INFUSION_SHARD, SwipeCurrency.INFUSION_CRYSTAL, SwipeCurrency.ASCENDANT_ESSENCE)
        val actor = ItemRowActor(
            r = r,
            model = model,
            action = null,
            onItemClick = null
        )
        if (levelProgressActor == null) {
            levelProgressActor = LevelProgressActor(r)
        }
        val levelGroup = Group().apply {
            setSize(r.width, levelProgressActor!!.height)
        }
        val maxLevelLabel = r.regular24Focus("MAX").apply {
            setSize(70f, 24f)
            setPosition(60f, 10f)
            setAlignment(Align.center)
            isVisible = model.item!!.experience == model.item!!.maxExperience
        }
        levelGroup.addActor(levelProgressActor)
        levelGroup.addActor(maxLevelLabel)
        levelGroup.addActor(r.image(Resources.ux_atlas, "fg_complete").apply {
            setSize(36f, 36f)
            setPosition(levelProgressActor!!.width, (levelProgressActor!!.height - 36f) / 2f)

            onClick {
                KtxAsync.launch {
                    if (useCount.any { it > 0 }) {
                        r.profileService.spendCurrency(cs, useCount)
                        r.profileService.addItemExperience(id, boostExp)
                    }
                    useCount.indices.forEach { useCount[it] = 0 }
                    loadData()
                }
            }
        })

        val currencyGroup = Group().apply {
            setSize(480f, 160f)
        }
        baseLevel = SwipeCharacter.getLevel(model.item!!.experience)
        baseExp = model.item!!.experience - SwipeCharacter.experience[baseLevel - 1]

        boostExp = 0
        cs.forEachIndexed { i, c ->
            val balance = r.profileService.getProfile().getBalance(c)
            boostExp += c.expBonus * useCount[i]
            balances[i] = balance
            val meta = r.profileService.getCurrency(c)
            val item = ItemCellActor(
                r = r,
                model = FrontItemEntryModel(
                    skin = c.toString(),
                    amount = balance - useCount[i],
                    level = 0,
                    rarity = meta.rarity,
                    name = meta.name,
                    currency = c,
                    item = null
                )
            ).apply {
                if (balances[i] - useCount[i] <= 0) {
                    alpha = 0.5f
                    touchable = Touchable.disabled
                } else {
                    onClick {
                        if (model.item!!.experience + boostExp < model.item!!.maxExperience) {
                            KtxAsync.launch {
                                useCount[i]++
                                loadData()
                            }
                        }
                    }
                }
                setPosition(120f * i, 0f)
            }
            currencyGroup.addActor(item)
        }

        val newExp = min(model.item!!.maxExperience, model.item!!.experience + boostExp)
        val newLevel = SwipeCharacter.getLevel(newExp)
        val oldLevel = SwipeCharacter.getLevel(model.item!!.experience)

        levelProgressActor!!.setState(SwipeCharacter.getLevel(model.item!!.experience), newLevel - oldLevel, boostExp, baseExp, newExp - SwipeCharacter.experience[newLevel - 1], SwipeCharacter.experience[newLevel] - SwipeCharacter.experience[newLevel - 1])
        content.add(actor).size(actor.width, actor.height).row()
        content.add(levelGroup).size(levelGroup.width, levelGroup.height).padTop(10f).row()
        content.add(currencyGroup).size(currencyGroup.width, currencyGroup.height).padTop(10f).row()
    }

    private suspend fun showDust() {
        val label = r.regular24Error(UiTexts.DustWarning.value(r.l)).apply {
            width = 460f
            wrap = true
            setAlignment(Align.bottomLeft)
        }
        content.add(label).width(460f).pad(10f).align(Align.bottom).row()

        val curGroup = Group().apply {
            setSize(480f, 160f)
        }
        val dustResult = r.profileService.previewDust(model.item!!.id).filter { it.amount > 0 }
        dustResult.forEachIndexed { i, balance ->
            val meta = r.profileService.getCurrency(balance.currency)
            val actor = ItemCellActor(r, FrontItemEntryModel(
                skin = meta.currency.toString(),
                amount = balance.amount,
                level = 0,
                rarity = meta.rarity,
                name = meta.name,
                currency = meta.currency,
                item = null
            )).apply {
                setPosition(i * 120f, 0f)
            }
            curGroup.addActor(actor)
        }
        content.add(curGroup).size(480f, 160f).row()
    }
}
