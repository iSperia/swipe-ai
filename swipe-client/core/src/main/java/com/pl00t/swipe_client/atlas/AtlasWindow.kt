package com.pl00t.swipe_client.atlas

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.action.*
import com.pl00t.swipe_client.services.profile.SwipeAct
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.actors.onExit
import ktx.actors.onTouchDown
import ktx.async.KtxAsync

class AtlasWindow(
    private val r: Resources,
    private val openAct: (SwipeAct) -> Unit
): Group() {

    private val content: Table = Table().apply {
        width = r.width
    }
    private val scrollPane = ScrollPane(content).apply {
        width = r.width
        height = r.height - 80f
    }
    private lateinit var windowTitle: WindowTitleActor
    private var bottomActionPanel: BottomActionPanel? = null

    private var selectedAct: SwipeAct? = null

    init {
        val texture = r.image(Resources.ux_atlas, "texture_screen").apply {
            setSize(r.width, r.height)
            setScaling(Scaling.fillY)
            color = r.skin().getColor("rarity_4")
        }
        val backgroundShadow = r.image(Resources.ux_atlas, "background_transparent50").apply { setSize(r.width, r.height) }
        addActor(texture)
        addActor(backgroundShadow)

        windowTitle = WindowTitleActor(r, UiTexts.Atlas.Title.value(r.l), null, null, 4).apply {
            setPosition(0f, r.height - 80f)
        }
        addActor(windowTitle)
        KtxAsync.launch {
            addActor(scrollPane)
            loadData()
        }
    }

    private suspend fun loadData() {
        content.clearChildren()

        var details: Group? = null

        val atlas = r.profileService.getAtlas()
        val adventuresAvailable = r.profileService.getProfile().actProgress.map { it.act }.toSet()
        val size = atlas.size
        atlas.forEachIndexed { i, adventure ->
            val g = Group().apply {
                setSize(160f, 200f)
            }
            val background = r.image(Resources.ux_atlas, "background_black").apply {
                setSize(160f, 200f)
                alpha = 0.6f
            }
            val image = r.image(Resources.atlas_atlas, "ACT", adventure.act.ordinal + 1).apply {
                setSize(150f, 150f)
                setPosition(5f, 45f)
                setOrigin(Align.center)
            }
            val label = r.regular20White(adventure.title.value(r.l)).apply {
                setSize(150f, 40f)
                setPosition(5f, 0f)
                setAlignment(Align.center)
                wrap = true
            }

            g.addActor(background)
            g.addActor(image)
            g.addActor(label)

            if (adventuresAvailable.contains(adventure.act)) {
                g.onTouchDown {
                    image.addAction(Actions.scaleTo(0.8f, 0.8f, 0.2f))
                    label.addAction(Actions.moveTo(5f, 10f, 0.2f))
                }
                g.onExit {
                    image.addAction(Actions.scaleTo(1f, 1f, 0.2f))
                    label.addAction(Actions.moveTo(5f, 0f, 0.2f))
                }
                g.onClick {
                    KtxAsync.launch {
                        selectedAct = adventure.act
                        loadData()
                    }
                }
            } else {
                val shadow = r.image(Resources.ux_atlas, "background_black").apply {
                    setSize(150f, 150f)
                    setPosition(5f, 45f)
                    alpha = 0.6f
                }
                val padLock = r.image(Resources.ux_atlas, "icon_padlock").apply {
                    setSize(120f, 120f)
                    setPosition(20f, 55f)
                    setScaling(Scaling.fit)
                    align = Align.center
                }
                g.addActor(shadow)
                g.addActor(padLock)
            }

            content.add(g).size(160f, 200f)

            if (selectedAct == adventure.act) {
                label.color = r.skin().getColor("focus_color")
                details = Group().apply {
                    setSize(r.width, 160f)
                }
                val background = r.image(Resources.ux_atlas, "background_black").apply {
                    alpha = 0.6f
                    setSize(r.width, 160f)
                }
                val lore = r.regularWhite(adventure.lore.value(r.l)).apply {
                    setSize(r.width - 90f, 150f)
                    setPosition(5f, 5f)
                    setAlignment(Align.topLeft)
                    wrap = true
                }
                val action = ActionCompositeButton(r, Action.Atlas, Mode.SingleLine(UiTexts.Atlas.Navigate.value(r.l))).apply {
                    setSize(80f, 110f)
                    setPosition(400f, 25f)
                    onClick {
                        openAct(selectedAct!!)
                    }
                }

                details?.addActor(background)
                details?.addActor(lore)
                details?.addActor(action)

            }

            if (i % 3 == 2 || i == size -1) {
                content.add().growX().row()
                if (details != null) {
                    content.add(details).colspan(3).row()
                    details = null
                }
            }
        }

        content.row()
        content.add().growY().row()
    }


}
