package com.pl00t.swipe_client.map

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.items.InventoryItem
import com.game7th.items.ItemAffix
import com.game7th.swipe.game.FrontMonsterConfiguration
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.analytics.AnalyticEvents
import com.pl00t.swipe_client.monster.MonsterTinyDetailsCell
import com.pl00t.swipe_client.screen.map.FrontMonsterEntryModel
import com.pl00t.swipe_client.services.levels.FrontRaidModel
import com.pl00t.swipe_client.services.levels.LevelRewardType
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeAct
import com.pl00t.swipe_client.ux.TinyItemCellActor
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync
import kotlin.math.max

class RaidWindow(
    private val r: Resources,
    private val act: SwipeAct,
    private val level: String,
    private val onClose: () -> Unit,
    private val onMonsterClicked: (FrontMonsterConfiguration) -> Unit,
    private val onLaunch: () -> Unit
) : Group() {

    lateinit var model: FrontRaidModel

    private val content: Table = Table().apply {
        width = 480f
    }
    private val scrollPane = ScrollPane(content).apply {
        width = r.width
        height = r.height - 190f
        y = 110f
    }

    private var tier = 0

    private var title: WindowTitleActor? = null

    init {
        setSize(r.width, r.height)
        val texture = r.image(Resources.ux_atlas, "texture_screen").apply { setSize(r.width, r.height); setScaling(Scaling.fillY); setColor(r.skin().getColor("rarity_3")) }
        val backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(texture)
        addActor(backgroundShadow)
        addActor(scrollPane)
        loadData()
        addBottomPanel()
    }

    private fun loadData() {
        KtxAsync.launch {
            content.clearChildren()

            model = r.profileService.getRaidDetails(act, level)
            addTitle()

            addLocationImage()

            val tableTiers = Table()
            val tiersToDisplay = max(5, model.tiers.size)
            (0 until tiersToDisplay).forEach { i ->
                val unlocked = i < model.tiers.size && i <= r.profileService.getTierUnlocked(act, level)

                val actor = Group().apply {
                    setSize(96f, 96f)
                }
                val bg = r.image(Resources.ux_atlas, "gradient_item_background").apply {
                    setSize(96f, 96f)
                    color = r.skin().getColor(if (i == tier) "main_color" else "accent_color")
                }
                actor.addActor(bg)
                val label = r.labelWindowTitle("${UiTexts.LvlShortPrefix.value(r.l)}${i+1}").apply {
                    setSize(96f, 96f)
                    setAlignment(Align.center)
                }
                actor.addActor(label)
                if (!unlocked) {
                    val lock = r.image(Resources.ux_atlas, "icon_padlock").apply {
                        setSize(96f, 96f)
                        setScaling(Scaling.fit)
                        align = Align.center
                    }
                    actor.addActor(lock)
                }
                if (unlocked) {
                    actor.onClick {
                        tier = i
                        loadData()
                    }
                }
                tableTiers.add(actor).size(96f, 96f)
                if (i % 5 == 4) tableTiers.row()
            }
            tableTiers.add().growX().row()
            content.add(tableTiers).row()

            content.add(r.regular20White(UiTexts.RaidPossibleRewards.value(r.l)).apply {
                width = 480f
                setAlignment(Align.center)
            }).width(480f).align(Align.center).padTop(5f).padBottom(5f).row()

            val tableRewards = Table()
            model.tiers[tier].rewards.forEachIndexed { index, reward ->
                val model = when (reward.type) {
                    LevelRewardType.item -> {
                        val meta = r.itemService.getItemTemplate(reward.skin!!)!!
                        FrontItemEntryModel.InventoryItemEntryModel(
                            skin = meta.skin,
                            amount = 1,
                            level = 1,
                            rarity = reward.rarity!!,
                            name = meta.name,
                            item = InventoryItem.fromTemplate(meta, reward.rarity)
                        )
                    }
                    LevelRewardType.currency -> {
                        val meta = r.profileService.getCurrency(reward.currency!!.type)
                        FrontItemEntryModel.CurrencyItemEntryModel(
                            skin = meta.currency.toString(),
                            amount = reward.currency!!.amount,
                            level = 0,
                            rarity = meta.rarity,
                            name = meta.name,
                            currency = meta.currency,
                        )
                    }
                }
                val actor = TinyItemCellActor(r, model).apply {
                    touchable = Touchable.disabled
                }
                tableRewards.add(actor).size(60f, 60f)
                if (index % 8 == 7) tableRewards.row()
            }
            tableRewards.add().growX().row()
            content.add(tableRewards).row()

            if (model.locationType == LevelType.RAID) {
                content.add(r.regular20White(UiTexts.RaidPossibleMonsters.value(r.l)).apply {
                    width = 480f
                    setAlignment(Align.center)
                }).width(480f).align(Align.center).padTop(5f).padBottom(5f).row()
                val tableMonsters = Table()

                model.tiers[tier].monster_pool.forEachIndexed { i, entry ->
                    val meta = r.monsterService.getMonster(entry.skin)!!
                    val actor = MonsterTinyDetailsCell(r, FrontMonsterEntryModel(
                        skin = entry.skin,
                        name = meta.name,
                        level = entry.level,
                        rarity = entry.rarity,
                    ))
                    actor.onClick {
                        KtxAsync.launch {
                            val monster = r.monsterService.createMonster(meta.skin, entry.level, 0)
                            onMonsterClicked(monster)
                        }
                    }
                    tableMonsters.add(actor).size(80f, 132f)
                    if (i % 6 == 5) tableMonsters.row()
                }
                tableMonsters.add().growX().row()
                content.add(tableMonsters).row()
            }


            content.row()
            content.add().growY()
        }
    }

    private fun addBottomPanel() {
        val actions = listOf(
            ActionCompositeButton(r, Action.Attack, Mode.SingleLine(UiTexts.ButtonAttack.value(r.l))).apply {
                onClick {
                    KtxAsync.launch {
                        r.analytics.trackEvent(AnalyticEvents.BattleEvent.EVENT_BATTLE_START, AnalyticEvents.BattleEvent.create(model.act, model.locationId, tier))
                        r.battleService.createBattle(model.act, model.locationId, tier)
                        onLaunch()
                    }
                }
            }
        )
        val panel = BottomActionPanel(r, actions, 3)
        addActor(panel)
    }

    private fun addTitle() {
        title?.remove()

        val closeButton = ActionCompositeButton(r, Action.Close, Mode.NoText).apply {
            setSize(80f, 80f)
        }
        closeButton.onClick {
            onClose()
        }
        title = WindowTitleActor(r, model.locationTitle.value(r.l), closeButton, null, 3).apply {
            y = r.height - this.height
        }

        addActor(title)
    }

    private fun addLocationImage() {

        val layers = Group().apply {
            setSize(480f, 240f)
        }
        val drawable = r.atlas(Resources.actAtlas(model.act)).findRegion(model.locationBackground).let {
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
        layers.addActor(image)
        if (model.locationType == LevelType.BOSS) {
            val unitDrawable = r.atlas(Resources.units_atlas).findRegion(model.bossSkin).let {
                val x1 = it.u
                val x2 = it.u2
                val y1 = it.v
                val y2 = it.v2
                val d = y1 - y2
                TextureRegionDrawable(TextureRegion(it.texture, x1, y1, x2, y2 + d * 0.5f))
            }

            val unitImage = Image(unitDrawable).apply {
                width = 240f
                height = 240f
                setScaling(Scaling.stretch)
                setPosition(0f, 0f)
            }
            unitImage.onClick {
                KtxAsync.launch {
                    val meta = r.monsterService.createMonster(model.tiers[tier].monster_pool.first().skin, model.tiers[tier].monster_pool.first().level, 0)
                    onMonsterClicked(meta)
                }
            }
            layers.addActor(unitImage)

            val unitLevel = r.regular24White("${UiTexts.LvlPrefix.value(r.l)} ${model.tiers[tier].monster_pool.first().level}").apply {
                setPosition(unitImage.x, unitImage.y)
                setSize(unitImage.width, 30f)
                setAlignment(Align.center)
                touchable = Touchable.disabled
            }
            layers.addActor(unitLevel)
        }
        content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f)}).size(480f, 1f).row()
        content.add(layers).size(480f, 240f).row()
        content.add(r.image(Resources.ux_atlas, "background_black").apply { setSize(480f, 1f) }).size(480f, 1f).row()
    }


}
