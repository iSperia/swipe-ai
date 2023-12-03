package com.pl00t.swipe_client.items

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.game7th.items.InventoryItem
import com.game7th.items.ItemAffix
import com.game7th.items.ItemCategory
import com.game7th.swipe.SbText
import com.game7th.swipe.game.SbSoundType
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.ItemBrowser
import com.pl00t.swipe_client.ux.ItemCellActor
import com.pl00t.swipe_client.ux.ItemRowActor
import com.pl00t.swipe_client.ux.LevelProgressActor
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync
import java.util.Currency
import kotlin.math.min

private enum class BrowseMode {
    DETAILS, DUST,
}

class InventoryItemWindow(
    protected val r: Resources,
    protected var id: String,
    private val onClose: () -> Unit
) : Group() {

    private lateinit var model: FrontItemEntryModel.InventoryItemEntryModel

    private val content = Table().apply {
        width = 480f
    }
    private val scrollPane = ScrollPane(content).apply {
        setSize(480f, r.height - 190f)
        setPosition(0f, 110f)
    }

    private var browseMode = BrowseMode.DETAILS

    lateinit var title: WindowTitleActor
    lateinit var bottomPanel: BottomActionPanel
    lateinit protected var background: Image
    lateinit protected var backgroundShadow: Image

    private var currencyIndexCache: Int? = null
    private var gemIndexCache: Int? = null

    init {
        setSize(r.width, r.height)

        KtxAsync.launch {
            loadData()

            background = r.image(Resources.ux_atlas, "texture_screen")
                .apply { setSize(r.width, r.height); alpha = 0.5f; color = r.skin().getColor("rarity_${model.rarity}") }
            backgroundShadow =
                r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
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
                            browseMode = BrowseMode.DETAILS
                            loadData()
                        }
                    }
                },
                ActionCompositeButton(r, Action.Close, Mode.SingleLine(UiTexts.Dust.value(r.l))).apply {
                    onClick {
                        KtxAsync.launch {
                            if (browseMode == BrowseMode.DUST) {
                                (model as? FrontItemEntryModel.InventoryItemEntryModel)?.let { model ->
                                    r.profileService.dustItem(model.item.id)
                                    onClose()
                                }
                            } else {
                                browseMode = BrowseMode.DUST
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
        model = FrontItemEntryModel.InventoryItemEntryModel(
            skin = item.skin,
            amount = 1,
            level = SwipeCharacter.getLevel(item.experience),
            rarity = item.rarity,
            name = template.name,
            item = item
        )

        when (browseMode) {
            BrowseMode.DETAILS -> {
                showDetails(item)
            }

            BrowseMode.DUST -> {
                showDust()
            }
        }

        content.row()
        content.add().growY()
    }

    private suspend fun showDetails(item: InventoryItem) {
        val cs = arrayOf(
            SwipeCurrency.INFUSION_ORB,
            SwipeCurrency.INFUSION_SHARD,
            SwipeCurrency.INFUSION_CRYSTAL,
            SwipeCurrency.ASCENDANT_ESSENCE
        )
        val actor = ItemRowActor(
            r = r,
            model = model,
            action = null,
            onItemClick = null
        )
        val levelProgressActor = LevelProgressActor(r).apply {
            val nowExp = SwipeCharacter.experience[SwipeCharacter.getLevel(item.experience) - 1]
            val newExp = SwipeCharacter.experience[SwipeCharacter.getLevel(item.experience)]

            setState(
                SwipeCharacter.getLevel(item.experience),
                0,
                0,
                item.experience - nowExp,
                item.experience - nowExp,
                newExp - nowExp,
                SwipeCharacter.getLevel(item.maxExperience)
            )
        }

        val profile = r.profileService.getProfile()
        val items: List<FrontItemEntryModel.CurrencyItemEntryModel> = cs.map {
            val meta = r.profileService.getCurrency(it)
            FrontItemEntryModel.CurrencyItemEntryModel(
                skin = meta.currency.toString(),
                amount = profile.getBalance(it),
                level = 0,
                rarity = meta.rarity,
                name = meta.name,
                currency = it,
            )
        }.filter { it.amount > 0 }
        val itemBrowser = ItemBrowser(r, items, onItemClick = null, actionProvider = { actionModel ->
            currencyIndexCache =
                items.indexOfFirst { it.currency == (actionModel as FrontItemEntryModel.CurrencyItemEntryModel).currency }
            ActionCompositeButton(r, Action.Complete, Mode.SingleLine(UiTexts.UseItem.value(r.l))).apply {
                onClick {
                    KtxAsync.launch {
                        (this@InventoryItemWindow.model as? FrontItemEntryModel.InventoryItemEntryModel)?.let { model ->
                            (actionModel as? FrontItemEntryModel.CurrencyItemEntryModel)?.let { actionModel ->
                                r.playSound(SbSoundType.USE_TOME)
                                val oldExp = SwipeCharacter.getLevel(model.item.experience)
                                r.profileService.spendCurrency(arrayOf(actionModel.currency), arrayOf(1))
                                r.profileService.addItemExperience(model.item.id, actionModel.currency.expBonus)
                                val newExp =
                                    SwipeCharacter.getLevel(model.item.experience + actionModel.currency.expBonus)
                                if (newExp != oldExp) {
                                    r.playSound(SbSoundType.LEVELUP)
                                }
                                loadData()
                            }

                        }
                    }
                }
            }
        }).apply {
            selectedIndex = currencyIndexCache
            drawItems()
        }

        content.add(actor).size(actor.width, actor.height).row()
        content.add(levelProgressActor).padTop(10f).padBottom(10f).row()
        content.add(itemBrowser).padTop(10f).row()

        if (model.item.category == ItemCategory.RING || model.item.category == ItemCategory.AMULET) {
            val allGems = r.mineService.listGems()
            if (allGems.isNotEmpty()) {
                val gemItems = allGems.mapIndexed { index, gem ->
                    FrontItemEntryModel.GemItemEntryModel(
                        skin = gem.skin,
                        amount = 0,
                        level = gem.tier,
                        rarity = gem.tier,
                        name = r.mineService.getGemTemplate(gem.skin).name,
                        gem = gem
                    )
                }
                val gemBrowser = ItemBrowser(
                    r = r,
                    items = gemItems,
                    onItemClick = { null },
                    actionProvider = { gemModel ->
                        currencyIndexCache = null
                        ActionCompositeButton(r, Action.Complete, Mode.SingleLine(UiTexts.UseItem.value(r.l))).apply {
                            onClick {
                                KtxAsync.launch {
                                    val template = r.mineService.getGemTemplate(gemModel.skin)
                                    val affix = r.itemService.getAffix(template.affix)!!

                                    val item = r.profileService.getItems().first { it.id == this@InventoryItemWindow.id }
                                    val enchantedItem = item.copy(
                                        enchant = ItemAffix(
                                            affix = template.affix,
                                            value = affix.valuePerTier * (1f + gemModel.level),
                                            scalable = true,
                                            level = gemModel.level + 1
                                        )
                                    )
                                    r.profileService.updateItem(enchantedItem)
                                    r.playSound(SbSoundType.LEVELUP)

                                    r.mineService.spendGem(gemModel.skin, gemModel.level)
                                    loadData()
                                }
                            }
                        }
                    }
                ).apply {
                    selectedIndex = gemIndexCache
                    drawItems()
                }

                content.add(gemBrowser).padTop(10f).row()
            }
        }
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
        val dustResult = r.profileService.previewDust((model as FrontItemEntryModel.InventoryItemEntryModel).item.id)
            .filter { it.amount > 0 }
        dustResult.forEachIndexed { i, balance ->
            val meta = r.profileService.getCurrency(balance.currency)
            val actor = ItemCellActor(
                r, FrontItemEntryModel.CurrencyItemEntryModel(
                    skin = meta.currency.toString(),
                    amount = balance.amount,
                    level = 0,
                    rarity = meta.rarity,
                    name = meta.name,
                    currency = meta.currency,
                )
            ).apply {
                setPosition(i * 120f, 0f)
            }
            curGroup.addActor(actor)
        }
        content.add(curGroup).size(480f, 140f).row()
    }
}
