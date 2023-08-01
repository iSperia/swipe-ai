package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.items.InventoryItem
import com.game7th.items.ItemCategory
import com.game7th.swipe.Language
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.items.CurrencyCellActor
import com.pl00t.swipe_client.screen.items.InventoryCellActor
import com.pl00t.swipe_client.services.levels.FrontLevelModel
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.LabelFactory
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync
import kotlin.math.max
import kotlin.math.min

interface LevelDetailsCallback {
    fun processMonsterClicked(skin: String)
}

private val tiers = arrayOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX")

class LevelDetailsActor(
    private val levelDetails: FrontLevelModel,
    private val context: SwipeContext,
    private val skin: Skin,
    private val attackAction: (String, Int) -> Unit
): Group(), LevelWaveCallback {

    lateinit var backgroundImage: Image
    lateinit var locationForeground: Image
    lateinit var startButton: TextButton
    lateinit var bossInfo: TextButton
    lateinit var locationTitle: Group
    lateinit var scroll: ScrollPane

    lateinit var table: Table
    private var tierGroup: Group? = null
    private var tierRecomendation: Label? = null

    private var tier: Int = -1

    var callback: LevelDetailsCallback? = null

    init {
        KtxAsync.launch {
            val line = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_line")).apply {
                y = 598f
                width = 480f
                height = 4f
            }
            backgroundImage = Image(context.commonAtlas(Atlases.ACT(levelDetails.act)).findRegion(levelDetails.locationBackground)).apply {
                setScaling(Scaling.fill)
                width = 480f
                height = 600f
            }
            locationForeground = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("black_pixel")).apply {
                width = 480f
                height = 600f
                setScaling(Scaling.stretch)
            }
            locationTitle = LabelFactory.createScreenTitle(context, skin, levelDetails.locationTitle.value(Language.EN)).apply {
                x = 60f
                y = 570f
            }

            val panel = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_blue")).apply {
                width = 480f
                height = 60f
            }
            startButton = Buttons.createActionButton("To Battle!", skin).apply {
                x = 300f
                y = 12f
            }
            startButton.onClick { attackAction(levelDetails.locationId, tier) }

            bossInfo = Buttons.createActionButton("Info", skin).apply {
                x = 10f
                y = 12f
                isVisible = false
            }
            bossInfo.onClick {
                processMonsterClicked(levelDetails.waves[0][0].skin)
            }

            table = Table()
            table.width = 360f

            scroll = ScrollPane(table).apply {
                width = 360f
                height = 510f
                x = 60f
                y = 60f
            }

            addActor(backgroundImage)
            addActor(locationForeground)
            addActor(line)

            when (levelDetails.type) {
                LevelType.BOSS -> {
                    if (context.profileService().isFreeRewardAvailable(levelDetails.act, levelDetails.locationId)) {
                        drawCampaign()
                    } else {
                        drawBoss()

                    }
                }
                LevelType.RAID -> {
                    drawRaid()
                }
                LevelType.CAMPAIGN -> {
                    drawCampaign()
                }
            }

            addActor(panel)
            addActor(startButton)
            addActor(bossInfo)
            addActor(locationTitle)
            addActor(scroll)
        }
    }

    private suspend fun drawBoss() {
        tier = 0
        bossInfo.isVisible = true
        scroll.touchable = Touchable.disabled
        context.profileService().getProfile().characters.first().let { character ->
            val boss = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion(levelDetails.waves[0][0].skin)).apply {
                width = 400f
                height = 600f
                x = -200f
                touchable = Touchable.disabled
            }
            addActor(boss)

            val label = Label("Choose tier:", skin, "affix_text").apply {
                x = 150f
                y = 530f
                touchable = Touchable.disabled
                setAlignment(Align.left)
            }
            addActor(label)
            tierRecomendation = Label("", skin, "lore_small").apply {
                x = 150f
                y = 455f
                touchable = Touchable.disabled
                setAlignment(Align.left)
            }
            addActor(tierRecomendation)

            val maxTier = context.profileService().getTierUnlocked(levelDetails.act, levelDetails.locationId)
            tier = min(maxTier, max(0, character.level.level / 5 - 1))

            tierGroup?.clearChildren()
            tierGroup?.remove()
            tierGroup = Group().apply {
                x = 150f
                y = 470f
            }
            (0 until 20).forEach { index ->
                val x = (index % 10) * 32f + 1f
                val y = 26f - (index / 10) * 26f
                val bg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_dark_blue")).apply {
                    this.width = 30f
                    this.height = 24f
                    this.x = x
                    this.y = y
                    this.name = index.toString()
                    this.setOrigin(Align.center)
                }
                bg.onClick {
                    tier = this.name.toInt()
                    selectTier()
                }
                val fg = Label(tiers[index], skin, "text_small").apply {
                    width = 30f
                    height = 24f
                    setAlignment(Align.center)
                    this.x = x
                    this.y = y
                    touchable = Touchable.disabled
                }

                tierGroup?.addActor(bg)
                tierGroup?.addActor(fg)

                if (index > maxTier) {
                    val padlock = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("icon_padlock")).apply {
                        width = 30f
                        height = 24f
                        this.x = x
                        this.y = y
                    }
                    tierGroup?.addActor(padlock)
                }
            }
            addActor(tierGroup)
            selectTier()

            drawSpecificLoot()
        }
    }

    private fun selectTier() {
        tierGroup?.children?.forEach { actor ->
            val regionName = if (actor.name == tier.toString()) "bg_blue" else "bg_dark_blue"
            val recommendedLevel = tier * 5 + 5
            tierRecomendation?.setText("Level recommended: $recommendedLevel")
            if (actor is Image && actor.name != null) {
                actor.drawable = TextureRegionDrawable(context.commonAtlas(Atlases.COMMON_UX).findRegion(regionName))
            }
        }
    }

    private suspend fun drawSpecificLoot() {
        val entries = context.levelService().getLevelSpecificDrops(levelDetails.act, levelDetails.locationId, (tier + 1) * 5).sortedByDescending { it.value }
        val lootEntryTable = Table().apply {
            width = 320f
        }
        val lootScroll = ScrollPane(lootEntryTable).apply {
            y = 100f
            x = 150f
            width = 320f
            height = 330f
        }
        val titleLabel = Label("Special rewards:", skin, "wave_caption").apply {
            width = 320f
            height = 24f
            wrap = true
            setAlignment(Align.left)
        }
        lootEntryTable.add(titleLabel).width(320f).height(24f).colspan(4).row()

        entries.forEachIndexed { index, entry ->
            val currencyRewardActor: Actor? = if (entry.currency != null) {
                val currencyMeta = context.profileService().getCurrency(entry.currency)
                CurrencyCellActor(context, skin, 78f, currencyMeta)
            } else if (entry.item != null) {
                val item = context.itemService().getItemTemplate(entry.item)!!
                InventoryCellActor(context, skin, 78f, InventoryItem("", item.skin, emptyList(), emptyList(), 0, 0, entry.rarity, ItemCategory.GLOVES, null, 0))
            } else {
                null
            }

            if (currencyRewardActor != null) {
                lootEntryTable.add(currencyRewardActor).width(80f).height(80f).align(Align.center)
                if (index % 4 == 3) lootEntryTable.row()
            }
        }

        lootEntryTable.row()
        lootEntryTable.add().growY()
        addActor(lootScroll)
    }

    private suspend fun drawRaid() {
        tier = 0
        scroll.touchable = Touchable.disabled
        context.profileService().getProfile().characters.first().let { character ->
            val labelMonsters = Label("Possible monsters: ", skin, "affix_text").apply {
                setAlignment(Align.left)
                width = 120f
                height = 20f
                y = 530f
                x = 10f
            }
            addActor(labelMonsters)
            val monstersRoot = Group().apply {
                height = 120f * levelDetails.monsterPool.size
                width = 120f
                y = 100f
            }
            val scroll = ScrollPane(monstersRoot).apply {
                width = 120f
                height = 420f
                y = 100f
                x = 10f
            }
            addActor(scroll)

            levelDetails.monsterPool.forEachIndexed { index, skin ->
                val monsterBg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_semi")).apply {
                    width = 116f
                    height = 116f
                    x = 2f
                    y = monstersRoot.height - (index + 1) * 120f + 2f
                }
                val monsterImage = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion(skin)).apply {
                    width = 80f
                    height = 120f
                    x = 20f
                    y = monstersRoot.height - (index + 1) * 120f
                    onClick {
                        processMonsterClicked(skin)
                    }
                }
                val monsterLabel = Label(context.monsterService().getMonster(skin)?.name?.en, this.skin, "affix_text").apply {
                    width = 120f
                    x = 0f
                    y = monstersRoot.height - (index + 1) * 120f
                    height = 30f
                    touchable = Touchable.disabled
                    setAlignment(Align.bottom)
                }
                monstersRoot.addActor(monsterBg)
                monstersRoot.addActor(monsterImage)
                monstersRoot.addActor(monsterLabel)
            }

            val label = Label("Choose tier:", skin, "affix_text").apply {
                x = 150f
                y = 530f
                touchable = Touchable.disabled
                setAlignment(Align.left)
            }
            addActor(label)
            tierRecomendation = Label("", skin, "lore_small").apply {
                x = 150f
                y = 455f
                touchable = Touchable.disabled
                setAlignment(Align.left)
            }
            addActor(tierRecomendation)

            val maxTier = context.profileService().getTierUnlocked(levelDetails.act, levelDetails.locationId)
            tier = min(maxTier, max(0, character.level.level / 5 - 1))

            tierGroup?.clearChildren()
            tierGroup?.remove()
            tierGroup = Group().apply {
                x = 150f
                y = 470f
            }
            (0 until 20).forEach { index ->
                val x = (index % 10) * 32f + 1f
                val y = 26f - (index / 10) * 26f
                val bg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_dark_blue")).apply {
                    this.width = 30f
                    this.height = 24f
                    this.x = x
                    this.y = y
                    this.name = index.toString()
                    this.setOrigin(Align.center)
                }
                bg.onClick {
                    tier = this.name.toInt()
                    selectTier()
                }
                val fg = Label(tiers[index], skin, "text_small").apply {
                    width = 30f
                    height = 24f
                    setAlignment(Align.center)
                    this.x = x
                    this.y = y
                    touchable = Touchable.disabled
                }

                tierGroup?.addActor(bg)
                tierGroup?.addActor(fg)

                if (index > maxTier) {
                    val padlock = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("icon_padlock")).apply {
                        width = 30f
                        height = 24f
                        this.x = x
                        this.y = y
                    }
                    tierGroup?.addActor(padlock)
                }
            }
            addActor(tierGroup)
            selectTier()

            drawSpecificLoot()
        }
    }

    private fun drawCampaign() {
        val needWaveLabel = levelDetails.waves.size > 1

        val loreLabel = Label(levelDetails.locationDescription.value(Language.EN), skin, "lore_small").apply {
            wrap = true
            width = 360f
            setAlignment(Align.topLeft)
        }
        table.add(loreLabel).colspan(3).width(360f).padBottom(30f).padTop(30f)
        table.row()

        levelDetails.waves.forEachIndexed { index, wave ->
            if (needWaveLabel) {
                table.add(Label("Wave ${index + 1}", skin, "wave_caption").apply {
                    width = 360f
                    setAlignment(Align.center)
                }).colspan(3)
                table.row()
            }
            wave.forEach { monster ->
                val group = Group().apply {
                    width = 120f
                    height = 180f
                }
                val label = Label("${monster.name}\nlvl. ${monster.level}", skin, "text_small").apply {
                    wrap = true
                    width = 110f
                    height = 40f
                    x = 5f
                    setAlignment(Align.center)
                }
                val monsterImage =
                    Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion(monster.skin.toString())).apply {
                        width = 120f
                        height = 180f
                    }
                monsterImage.onClick {
                    processMonsterClicked(monster.skin)
                }
                group.addActor(monsterImage)
                group.addActor(label)
                table.add(group)
            }
            table.row()
        }
    }

    override fun processMonsterClicked(skin: String) {
        callback?.processMonsterClicked(skin)
    }
}
