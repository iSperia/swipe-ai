package com.pl00t.swipe_client.screen.reward

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.services.profile.CollectedReward


class ItemRewardEntryActor(
    private val reward: CollectedReward.CollectedItem,
    private val context: SwipeContext,
    private val skin: Skin,
): Group() {

    val image: Image
    val title: Label
    val levelLabel: Label

    init {
        width = 360f
        height = 84f

        val leftGroup = Group().apply {
            height = 84f
            width = 72f
        }

        image = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion(reward.skin)).apply {
            width = 72f
            height = 72f
            y = 12f
        }
        leftGroup.addActor(image)

        val starGroup = Group().apply {
            width = 24f + (reward.rarity - 1) * 12f
            height = 24f
            x = (72f - this.width) / 2f
        }
        (0 until reward.rarity).forEach { index ->
            val star = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("icon_star")).apply {
                x = 12f * index
                width = 24f
                height = 24f
                setScaling(Scaling.stretch)
            }
            starGroup.addActor(star)
        }
        leftGroup.addActor(starGroup)


        val rightGroup = Group().apply {
            x = 90f
            height = 84f
            width = 270f
        }

        title = Label(reward.title, skin, "wave_caption").apply {
            setAlignment(Align.topLeft)
            y = 60f
        }
        rightGroup.addActor(title)

        levelLabel = Label("Lvl. ${reward.level}", skin, "text_regular").apply {
            setAlignment(Align.topRight)
            width = 270f
            y = 60f
        }
        rightGroup.addActor(levelLabel)

        val descriptionLabel = Label(reward.lore, skin, "text_small").apply {
            setAlignment(Align.topLeft)
            wrap = true
            width = 270f
            height = 58f
        }
        rightGroup.addActor(descriptionLabel)

        addActor(leftGroup)
        addActor(rightGroup)
    }

}
