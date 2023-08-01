package com.pl00t.swipe_client.monster

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.swipe.FrontAttributesModel
import com.pl00t.swipe_client.R
import com.pl00t.swipe_client.UiTexts

class AttributesActor(
    private val r: R,
    private var model: FrontAttributesModel,
    private val skin: String
) : Group() {

    lateinit var characterImage: Image
    private val tableLeft = Table().apply {
        width = 180f
        height = 390f
        x = 5f
    }
    private val tableRight = Table().apply {
        width = 180f
        height = 390f
        x = r.width - 185f
    }

    init {
        setSize(r.width, r.height - 190f)

        characterImage = r.image(R.units_atlas, skin).apply {
            width = r.width
            height = r.height - 190f
            setScaling(Scaling.fill)
            x = (r.width - this.width) / 2f
        }

        val shadow = r.image(R.ux_atlas, "background_transparent50").apply {
            setSize(480f, 390f)
        }

        addActor(characterImage)
        addActor(shadow)
        addActor(tableLeft)
        addActor(tableRight)

        refreshData()
    }

    private fun refreshData() {
        tableLeft.clearChildren()
        tableRight.clearChildren()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelHealth.value(r.l)).apply { setAlignment(Align.right); }).size(180f, 35f).colspan(2).row()
        tableLeft.add(r.regular20White(model.health).apply { setAlignment(Align.right) }).height(20f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_health").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelLuck.value(r.l)).apply { setAlignment(Align.right); }).size(180f, 35f).colspan(2).row()
        tableLeft.add(r.regular20White("${model.luck}%").apply { setAlignment(Align.right) }).height(20f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_luck").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelUltProgress.value(r.l)).apply { setAlignment(Align.right); }).size(180f, 35f).colspan(2).row()
        tableLeft.add(r.regular20White("${model.ultProgress}/${model.ultMax}").apply { setAlignment(Align.right) }).height(20f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_ult").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelBody.value(r.l)).apply { setAlignment(Align.right); }).size(180f, 35f).colspan(2).row()
        tableLeft.add(r.regular20White(model.body).apply { setAlignment(Align.right) }).height(20f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_body").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelSpirit.value(r.l)).apply { setAlignment(Align.right); }).size(180f, 35f).colspan(2).row()
        tableLeft.add(r.regular20White(model.spirit).apply { setAlignment(Align.right) }).height(20f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_spirit").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableLeft.add(r.regular20Focus(UiTexts.AttributeLabelMind.value(r.l)).apply { setAlignment(Align.right); }).size(180f, 35f).colspan(2).row()
        tableLeft.add(r.regular20White(model.mind).apply { setAlignment(Align.right) }).height(20f).growX()
        tableLeft.add(r.image(R.ux_atlas, "icon_attr_mind").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f).row()
        tableLeft.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableLeft.row()
        tableLeft.add().growY()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelPhysResist.value(r.l)).apply { setAlignment(Align.left); }).size(180f, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_phys").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f)
        tableRight.add(r.regular20White("${model.resists.phys * 100f}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelShockResist.value(r.l)).apply { setAlignment(Align.left); }).size(180f, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_shock").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f)
        tableRight.add(r.regular20White("${model.resists.shock * 100f}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelLightResist.value(r.l)).apply { setAlignment(Align.left); }).size(180f, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_light").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f)
        tableRight.add(r.regular20White("${model.resists.light * 100f}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelDarkResist.value(r.l)).apply { setAlignment(Align.left); }).size(180f, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_dark").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f)
        tableRight.add(r.regular20White("${model.resists.dark * 100f}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelFireResist.value(r.l)).apply { setAlignment(Align.left); }).size(180f, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_fire").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f)
        tableRight.add(r.regular20White("${model.resists.fire * 100f}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableRight.add(r.regular20Focus(UiTexts.AttributeLabelColdResist.value(r.l)).apply { setAlignment(Align.left); }).size(180f, 35f).colspan(2).row()
        tableRight.add(r.image(R.ux_atlas, "icon_resist_cold").apply { setSize(20f, 20f) }).size(20f, 20f).padLeft(3f).padRight(3f)
        tableRight.add(r.regular20White("${model.resists.cold * 100f}%").apply { setAlignment(Align.left) }).height(20f).growX().row()
        tableRight.add(r.image(R.ux_atlas, "background_white").apply { setSize(180f, 1f) }).size(180f, 1f).colspan(2).row()

        tableRight.row()
        tableRight.add().growY()

    }

    fun updateModel(model: FrontAttributesModel) {
        this.model = model
        refreshData()
    }
}
