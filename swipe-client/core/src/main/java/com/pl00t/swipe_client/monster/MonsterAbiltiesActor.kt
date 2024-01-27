package com.pl00t.swipe_client.monster

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.game.FrontMonsterConfiguration
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.action.Action
import com.pl00t.swipe_client.action.ActionCompositeButton
import com.pl00t.swipe_client.action.BottomActionPanel
import com.pl00t.swipe_client.action.Mode
import ktx.actors.alpha
import ktx.actors.onClick

class MonsterAbiltiesActor(
    private val r: Resources,
    private val model: FrontMonsterConfiguration
) : Group() {

    private var focusIndex = 0

    private val contentTable = Table().apply {
        width = 480f
    }
    private val scrollPane = ScrollPane(contentTable).apply {
        setPosition(0f, 110f)
        setSize(480f, r.height - 300f)
    }

    init {
        val actions = model.frontAbilities.mapIndexed { index, ability ->
            ActionCompositeButton(
                r = r,
                action = Action.SkillDetails(ability.skin),
                mode = Mode.SingleLine(ability.title.value(r.l))
            ).apply {
                this.width = 120f
                onClick { selectIndex(index) }
            }
        }
        val bottomPanel = BottomActionPanel(
            r = r,
            actions = actions,
            backgroundRarity = 1
        )

        addActor(bottomPanel)
        addActor(scrollPane)

        selectIndex(0)
    }

    fun selectIndex(index: Int) {
        if (index >= model.frontAbilities.size) return
        focusIndex = index
        contentTable.clearChildren()

        model.frontAbilities[index].let { ability ->
            contentTable.add(r.labelFocusedCaption(ability.title.value(r.l))).height(60f).colspan(2).row()
            contentTable.add(r.regular20White(ability.description.value(r.l)).apply { wrap = true }).colspan(2).width(460f).pad(10f).row()
            contentTable.add(r.image(Resources.ux_atlas, "background_white").apply { setSize(460f, 1f); alpha = 0.3f }).width(460f).colspan(2).row()

            ability.fields.forEach { field ->
                contentTable.add(r.regular20White(field.title.value(r.l)).apply { setAlignment(Align.right) }).growX().colspan(1)
                contentTable.add(r.regular20Focus(field.value).apply { setAlignment(Align.center) }).padLeft(15f).width(90f).row()
            }
        }

        contentTable.row()
        contentTable.add().growY()
    }
}
