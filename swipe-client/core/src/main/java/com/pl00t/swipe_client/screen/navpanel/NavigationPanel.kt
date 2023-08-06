package com.pl00t.swipe_client.screen.navpanel

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.map.MapScreenRouter
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync

class NavigationPanel(
    private val context: SwipeContext,
    private val skin: Skin,
    private val router: MapScreenRouter,
) : Group() {

    val characterShadow = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion("CHARACTER_VALERIAN")).apply {
        width = 80f
        height = 116f
        setScaling(Scaling.stretch)
        x = 486f
        y = -2f
        scaleX = -1f
        color = Color.BLACK
    }

    val characterBg = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion("CHARACTER_VALERIAN")).apply {
        width = 72f
        height = 108f
        setScaling(Scaling.stretch)
        x = 480f
        scaleX = -1f
    }

    val characterLabel = Label("", skin, "text_small").apply {
        width = 72f
        height = 36f
        x = 408f
        wrap = true
        setAlignment(Align.center)
    }

    val panelBg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_dark_blue")).apply {
        width = 480f
        height = 72f
    }

    val buttonParty = NavigationButton("nav_party", "Heroes", skin).apply {
        width = 48f
        height = 60f
    }

    val buttonInventory = NavigationButton("nav_inventory", "Crafting", skin).apply {
        width = 48f
        height = 60f
    }

    val buttonMap = NavigationButton("nav_map", "Kingdoms", skin).apply {
        width = 48f
        height = 60f
    }

    val buttonShop = NavigationButton("nav_shop", "Mystic Shop", skin).apply {
        width = 48f
        height = 60f
    }

    init {
        val table = Table().apply {
            height = 60f
            width = 460f
        }

        table.add(buttonMap).width(48f).padLeft(8f).padRight(8f)
        table.add(buttonShop).width(48f).padLeft(8f).padRight(8f)
        table.add(buttonInventory).width(48f).padLeft(8f).padRight(8f)
        table.add(buttonParty).width(48f).padLeft(8f).padRight(8f)

        buttonParty.onClick {
            router.showHeroesList()
        }
        buttonInventory.onClick {
            router.showInventory()
        }
        buttonShop.onClick {
            router.showShop()
        }

        reloadActiveHeroLabel()

        addActor(characterShadow)
        addActor(characterBg)
        addActor(characterLabel)
//        addActor(panelBg)
        addActor(table)

        val buttons = listOf(buttonMap, buttonInventory, buttonParty)
        val buttonCount = buttons.size
        val wid = 480f - 72f
        val totalButtonWidth = 80f * buttonCount
        val padding = (wid - totalButtonWidth) / 2f
        buttons.forEachIndexed { index, button ->
            button.x = padding + index * 80f + 11f
        }
    }

    fun reloadActiveHeroLabel() {
    }
}
