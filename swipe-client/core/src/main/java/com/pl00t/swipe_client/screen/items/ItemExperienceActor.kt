package com.pl00t.swipe_client.screen.items

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import kotlinx.coroutines.launch
import ktx.async.KtxAsync

class ItemExperienceActor(
    private val itemId: String,
    private val context: SwipeContext,
    private val skin: Skin,
) : Group() {

    val bg: Image
    val fg: Image
    val progressLabel: Label

    init {
        bg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_dark_blue")).apply {
            width = 120f
            height = 20f
        }
        fg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_blue")).apply {
            width = 120f
            height = 20f
            scaleX = 0f
        }
        progressLabel = Label("", skin, "text_small").apply {
            width = 120f
            height = 20f
            setAlignment(Align.center)
        }

        addActor(bg)
        addActor(fg)
        addActor(progressLabel)
        loadData()
    }

    fun loadData() {
        KtxAsync.launch {
            context.profileService().getItems().firstOrNull { it.id == itemId }?.let { item ->
                val maxProgress = item.level * item.level
                val progressPercent = item.experience.toFloat() / maxProgress
                fg.addAction(Actions.scaleTo(progressPercent, 1f, 0.3f))
                if (item.level >= item.maxLevel) {
                    progressLabel.setText("MAX EXP")
                } else {
                    progressLabel.setText("EXP: ${item.experience}/$maxProgress")
                }
            }
        }
    }

}
