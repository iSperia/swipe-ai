package com.pl00t.swipe_client.ux

import com.badlogic.gdx.math.Interpolation.ExpIn
import com.badlogic.gdx.math.Interpolation.PowIn
import com.badlogic.gdx.math.Interpolation.SwingOut
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.game7th.swipe.SbText
import com.game7th.swipe.gestures.SimpleDirectionGestureDetector
import com.pl00t.swipe_client.Resources
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.actors.repeatForever
import ktx.async.KtxAsync

sealed interface HoverAction {
    data class HoverClick(val onClick: suspend () -> Unit): HoverAction
    data class HoverSwipe(val dx: Int, val dy: Int, val onSwipe: () -> Unit): HoverAction
}

class TutorialHover(
    private val r: Resources,
    private val area: Rectangle,
    private val text: SbText,
    private val action: HoverAction
) : Group() {

    init {
        val hover = createTutorial(r, area.x, area.y, area.width, area.height).apply {
            this.touchable = Touchable.enabled
            onClick {  }
        }
        addActor(hover)
        val spaceTop = r.height - area.y - area.height
        val spaceBottom = area.y

        when (action) {
            is HoverAction.HoverClick -> {
                val fake = r.image(Resources.ux_atlas, "background_black").apply {
                    onClick {
                        KtxAsync.launch {
                            action.onClick()
                            this@TutorialHover.remove()
                        }
                    }
                    alpha = 0f
                    setPosition(area.x, area.y)
                    setSize(area.width, area.height)
                }
                addActor(fake)
            }
            is HoverAction.HoverSwipe -> {
                val fake = r.image(Resources.ux_atlas, "background_black").apply {
                    alpha = 0f
                    setPosition(area.x, area.y)
                    setSize(area.width, area.height)
                }
                addActor(fake)

                val gestureDetector = SimpleDirectionGestureDetector(object: SimpleDirectionGestureDetector.DirectionListener {
                    override fun onLeft() = checkSwipe(-1, 0)
                    override fun onRight() = checkSwipe(1, 0)
                    override fun onUp() = checkSwipe(0, 1)
                    override fun onDown() = checkSwipe(0, -1)

                    private fun checkSwipe(dx: Int, dy: Int) {
                        if (dx == action.dx && dy == action.dy) {
                            r.inputMultiplexer.removeProcessor(r.inputMultiplexer.processors.size - 1)
                            action.onSwipe()
                            this@TutorialHover.remove()
                        }
                    }
                })
                r.inputMultiplexer.addProcessor(gestureDetector)
                val cursorActor = r.image(Resources.ux_atlas, "cursor").apply {
                    setSize(96f, 96f)
                    setPosition(area.x + area.width / 2f - 48f - 72f * action.dx, area.y + area.height / 2f - 48f - 72f * action.dy)
                    setOrigin(Align.center)
                    this.touchable = Touchable.disabled
                    addAction(Actions.sequence(
                        Actions.alpha(0f),
                        Actions.alpha(1f, 0.3f),
                        Actions.moveBy(144f * action.dx, 144f * action.dy, 0.4f, PowIn(2)),
                        Actions.alpha(0f, 0.3f),
                        Actions.moveBy(-144f * action.dx, -144f * action.dy)
                    ).repeatForever())
                }
                addActor(cursorActor)
            }
        }

        val text = r.regular24White(text.value(r.l)).apply {
            x = 10f
            y = if (spaceBottom > spaceTop) 0f else area.y + area.height + 10f
            width = 460f
            height = if (spaceBottom > spaceTop) spaceBottom - 10f else spaceTop - 10f
            setAlignment(if (spaceBottom > spaceTop) Align.topLeft else Align.bottomLeft)
            wrap = true
            this.touchable = Touchable.disabled
        }
        addActor(text)
    }
}
