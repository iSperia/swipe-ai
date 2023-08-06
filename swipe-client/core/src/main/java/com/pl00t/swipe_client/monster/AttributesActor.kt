package com.pl00t.swipe_client.monster

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.swipe.game.FrontMonsterConfiguration
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.ItemCellActor
import com.pl00t.swipe_client.ux.LevelProgressActor
import ktx.actors.alpha

abstract class AttributesActor(
    protected val r: R,
    protected var model: FrontMonsterConfiguration,
) : Group() {

    protected val contentRoot = Group()
    protected val tableLeft = Table().apply {
        width = 220f
        height = 340f
        x = 10f
        y = getTableHeight() - 340f
    }
    protected val tableRight = Table().apply {
        width = 220f
        height = 340f
        x = r.width - 230f
        y = getTableHeight() - 340f
    }
    protected var characterImage: Image

    protected val currencies = arrayOf<SwipeCurrency>(SwipeCurrency.SCROLL_OF_WISDOM, SwipeCurrency.SCROLL_OF_WISDOM, SwipeCurrency.SCROLL_OF_WISDOM, SwipeCurrency.SCROLL_OF_WISDOM)

    lateinit var progressActor: LevelProgressActor


    protected var balances = arrayOf(0, 0, 0, 0)
    protected var useCount = arrayOf(0, 0, 0, 0)
    protected var cells = arrayOf<ItemCellActor?>(null, null, null, null)
    protected var baseLevel: Int = 0
    protected var baseExp: Int = 0
    protected var boostExp: Int = 0

    protected open fun handleDataLoaded() {

        refreshData()
        recalculate()
    }

    init {
        setSize(r.width, r.height - 190f)

        val shadow = r.image(R.ux_atlas, "background_transparent50").apply {
            setSize(480f, getTableHeight())
        }
        val line = r.image(R.ux_atlas, "background_white").apply {
            setSize(480f, 1f)
            y = shadow.height
            alpha = 0.3f
        }

        characterImage = r.image(R.units_atlas, model.skin).apply {
            width = r.width
            height = r.height - 190f - 340f
            setScaling(Scaling.fillY)
            x = (r.width - this.width) / 2f
            y = 340f
        }
        val characterShadow = r.image(R.units_atlas, model.skin).apply {
            width = characterImage.width
            height = characterImage.height
            setScaling(Scaling.fillY)
            setOrigin(Align.center)
            setPosition(characterImage.x, characterImage.y)
            setScale(1.05f)
            alpha = 0.75f
            color = Color.BLACK
        }
        addActor(characterShadow)
        addActor(characterImage)

        addActor(shadow)
        addActor(contentRoot)

        contentRoot.addActor(tableLeft)
        contentRoot.addActor(tableRight)
        contentRoot.addActor(line)
    }

    protected open fun getTableHeight(): Float {
        return 340f
    }

    protected fun refreshData() {
        val columnWidth = 220f
        tableLeft.clearChildren()
        tableRight.clearChildren()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelHealth.value(r.l)).apply { setAlignment(Align.right); }).size(columnWidth, 35f).colspan(2).row()
        tableLeft.add(r.regular20White(model.health.toString()).apply { setAlignment(Align.right) }).height(20f).padRight(3f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_health").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelLuck.value(r.l)).apply { setAlignment(Align.right); }).size(columnWidth, 35f).colspan(2).row()
        tableLeft.add(r.regular20White("${model.luck}%").apply { setAlignment(Align.right) }).height(20f).padRight(3f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_luck").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelUltProgress.value(r.l)).apply { setAlignment(Align.right); }).size(columnWidth, 35f).colspan(2).row()
        tableLeft.add(r.regular20White("${model.ult}/${model.ultMax}").apply { setAlignment(Align.right) }).height(20f).padRight(3f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_ult").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelBody.value(r.l)).apply { setAlignment(Align.right); }).size(columnWidth, 35f).colspan(2).row()
        tableLeft.add(r.regular20White(model.attributes.body.toString()).apply { setAlignment(Align.right) }).height(20f).padRight(3f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_body").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelSpirit.value(r.l)).apply { setAlignment(Align.right); }).size(columnWidth, 35f).colspan(2).row()
        tableLeft.add(r.regular20White(model.attributes.spirit.toString()).apply { setAlignment(Align.right) }).height(20f).padRight(3f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_spirit").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelMind.value(r.l)).apply { setAlignment(Align.right); }).size(columnWidth, 35f).colspan(2).row()
        tableLeft.add(r.regular20White(model.attributes.mind.toString()).apply { setAlignment(Align.right) }).height(20f).padRight(3f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_mind").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f).row()
//        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f) }).size(columnWidth, 1f).colspan(2).row()

        tableLeft.row()
        tableLeft.add().growY()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelPhysResist.value(r.l)).apply { setAlignment(Align.left); }).size(columnWidth, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_phys").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f)
        tableRight.add(r.regular20White("${"%.2f".format(model.resist.phys * 100f)}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelShockResist.value(r.l)).apply { setAlignment(Align.left); }).size(columnWidth, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_shock").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f)
        tableRight.add(r.regular20White("${"%.1f".format(model.resist.shock * 100f)}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelLightResist.value(r.l)).apply { setAlignment(Align.left); }).size(columnWidth, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_light").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f)
        tableRight.add(r.regular20White("${"%.1f".format(model.resist.light * 100f)}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelDarkResist.value(r.l)).apply { setAlignment(Align.left); }).size(columnWidth, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_dark").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f)
        tableRight.add(r.regular20White("${"%.1f".format(model.resist.dark * 100f)}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelFireResist.value(r.l)).apply { setAlignment(Align.left); }).size(columnWidth, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_fire").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f)
        tableRight.add(r.regular20White("${"%.1f".format(model.resist.fire * 100f)}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f); alpha = 0.3f }).size(columnWidth, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelColdResist.value(r.l)).apply { setAlignment(Align.left); }).size(columnWidth, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_cold").apply { setSize(18f, 18f) }).size(18f, 18f).padLeft(5f).padRight(3f)
        tableRight.add(r.regular20White("${"%.1f".format(model.resist.cold * 100f)}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
//        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(columnWidth, 1f) }).size(columnWidth, 1f).colspan(2).row()

        tableRight.row()
        tableRight.add().growY()

    }

    protected open fun addCurrencyItems() {}

    protected open fun recalculate() {}

}
