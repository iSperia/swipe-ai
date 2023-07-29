package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.levels.FrontLevelDetails
import com.pl00t.swipe_client.services.levels.LevelType
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.ScreenTitle
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync
import kotlin.math.max

interface LevelDetailsCallback {
    fun processMonsterClicked(skin: String)
}

private val tiers = arrayOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX")

class LevelDetailsActor(
    private val levelDetails: FrontLevelDetails,
    private val context: SwipeContext,
    private val skin: Skin,
    private val attackAction: (String) -> Unit
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

    private var tier: Int = 0

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
            locationTitle = ScreenTitle.createScreenTitle(context, skin, levelDetails.locationTitle).apply {
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
            startButton.onClick { attackAction(levelDetails.locationId) }

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
                LevelType.CAMPAIGN -> {
                    drawCampaign()
                }
                LevelType.RAID -> {

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
            tier = 0
//            tier = max(0, character.level.level / 5 - 1)
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
                    println("this.name = ${this.name}")
                }
                bg.onClick {
                    println("bg.onClick { ${this.name} }")
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

                if (index > 0) {
                    //TODO: add chek what tiers are available
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
        }
    }

    private fun selectTier() {
        println("selectTier($tier)")
        tierGroup?.children?.forEach { actor ->
            val regionName = if (actor.name == tier.toString()) "bg_blue" else "bg_dark_blue"
            val recommendedLevel = tier * 5 + 5
            tierRecomendation?.setText("Level recommended: $recommendedLevel")
            if (actor is Image && actor.name == tier.toString()) {
                actor.drawable = TextureRegionDrawable(context.commonAtlas(Atlases.COMMON_UX).findRegion(regionName))
            }
        }
    }

    private fun drawCampaign() {
        val needWaveLabel = levelDetails.waves.size > 1

        val loreLabel = Label(levelDetails.locationDescription, skin, "lore_small").apply {
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
