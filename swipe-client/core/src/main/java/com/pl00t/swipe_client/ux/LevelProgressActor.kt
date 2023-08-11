package com.pl00t.swipe_client.ux

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import ktx.actors.alpha

class LevelProgressActor(
    protected val r: Resources
) : Group() {

    protected val progressBackground = r.image(Resources.ux_atlas, "background_accent").apply {
        setSize(440f, 8f)
        setPosition(10f, 0f)
        alpha = 0.5f
    }
    protected val progressActual = r.image(Resources.ux_atlas, "background_white").apply {
        setSize(progressBackground.width - 4f, progressBackground.height - 4f)
        setPosition(progressBackground.x + 2f, progressBackground.y + 2f)
        scaleX = 0f
    }
    protected val progressVirtual = r.image(Resources.ux_atlas, "background_main").apply {
        setSize(progressBackground.width - 4f, progressBackground.height - 4f)
        setPosition(progressBackground.x + 2f, progressBackground.y + 2f)
        scaleX = 0f
    }

    protected val labelLevel = r.regular24White("Lv. 1").apply {
        setSize(85f, 24f)
        setAlignment(Align.left)
        setPosition(progressBackground.x, progressBackground.y + 10f)
    }

    protected val labelLevelBoost = r.regular24Focus("+6").apply {
        setSize(65f, 24f)
        setAlignment(Align.left)
        setPosition(labelLevel.x + 90f, labelLevel.y)
    }

    protected val labelCurrentExp = r.regular20White("0/1500").apply {
        setSize(120f, 24f)
        setAlignment(Align.right)
        setPosition(progressBackground.x + progressBackground.width - 120f, labelLevel.y)
    }

    protected val labelExpBoost = r.regular20Main("+5400").apply {
        setSize(180f, 24f)
        setAlignment(Align.center)
        setPosition(labelLevelBoost.x + 70f, labelLevelBoost.y)
    }

    protected var level: Int = 0
    protected var levelBoost: Int = 0

    init {
        setSize(460f, 32f)
        addActor(progressBackground)
        addActor(progressVirtual)
        addActor(progressActual)

        addActor(labelLevel)
        addActor(labelLevelBoost)
        addActor(labelCurrentExp)
        addActor(labelExpBoost)
    }

    fun setState(level: Int, levelBoost: Int, expBoost: Int, baseExp: Int, exp: Int, maxExp: Int, maxLevel: Int) {
        labelLevel.setText("${UiTexts.LvlShortPrefix.value(r.l)}$level")

        val lExpBoost = if (expBoost > 1000) "${expBoost.toFloat() / 1000f}K" else expBoost.toString()
        labelExpBoost.setText("+$lExpBoost")
        val lExp = if (exp > 1000) "${exp.toFloat() / 1000f}K" else exp.toString()
        val lMaxExp = if (maxExp > 1000) "${maxExp.toFloat() / 1000f}K" else maxExp.toString()

        labelCurrentExp.setText("$lExp/$lMaxExp")

        labelExpBoost.isVisible = expBoost > 0
        if (maxLevel == level) {
            labelLevelBoost.setText("MAX")
        } else {
            labelLevelBoost.setText("")
        }
        if (levelBoost > 0) {
            progressActual.isVisible = false
        } else {
            progressActual.isVisible = true
            progressActual.scaleX = baseExp.toFloat() / maxExp
        }

        if (level != this.level) {
            this.level = level
        } else {
            progressVirtual.addAction(
                Actions.sequence(
                    Actions.repeat(levelBoost - this.levelBoost, Actions.sequence(
                        Actions.scaleTo(1f, 1f, 0.2f),
                        Actions.run { progressVirtual.scaleX = 0f }
                    )),
                    Actions.scaleTo(exp.toFloat() / maxExp, 1f, 0.2f)
                )
            )
        }

        this.levelBoost = levelBoost
    }
}
