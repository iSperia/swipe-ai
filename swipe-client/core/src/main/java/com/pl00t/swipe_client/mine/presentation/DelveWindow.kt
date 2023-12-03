package com.pl00t.swipe_client.mine.presentation

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.swipe.SbText
import com.pl00t.swipe_client.Resources
import com.pl00t.swipe_client.UiTexts
import com.pl00t.swipe_client.battle.EncounterResultDialog
import com.pl00t.swipe_client.home.StackDelegate
import com.pl00t.swipe_client.mine.data.MineItem
import com.pl00t.swipe_client.services.battle.EncounterResultModel
import com.pl00t.swipe_client.services.profile.FrontItemEntryModel
import com.pl00t.swipe_client.services.profile.SwipeAct
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync
import kotlin.random.Random

class DelveWindow(
    private val r: Resources,
    private val stack: StackDelegate,
    private val onClose: () -> Unit
) : Group() {

    val background = r.image(Resources.actAtlas(SwipeAct.ACT_2), "crystal_mines").apply {
        width = r.width
        height = r.height
        setScaling(Scaling.fillY)
    }
    val backgroundShadow = r.image(Resources.ux_atlas, "background_black").apply {
        width = r.width
        height = r.height
        alpha = 0.75f
    }

    private val field = mutableMapOf<Int, MineItem>()

    private val rewards = mutableListOf<MineItem>()

    private var openedPosition: Int = -1
    private var triesLeft = 0

    val label = r.regular24Focus(UiTexts.Mine.CaptionTriesLeft.value(r.l)).apply {
        setSize(r.width - 20f, 30f)
        setPosition(10f, r.height - 30f)
    }
    val captionSubtitle = r.regular20White(UiTexts.Mine.CaptionResults.value(r.l)).apply {
        setSize(r.width - 20f, 30f)
        setPosition(10f, label.y - 30f)
    }

    val gemLayer = Group().apply {
        y = 20f
    }
    val backLayer = Group().apply {
        y = 20f
    }
    val rewardLayer = Group().apply {
        y = captionSubtitle.y
    }


    init {
        addActor(background)
        addActor(backgroundShadow)
        addActor(label)
        addActor(captionSubtitle)
        addActor(rewardLayer)

        val cellSize = r.width * 0.25f

        r.loadAtlas(ATLAS) {
            KtxAsync.launch {
                onAtlasLoaded(cellSize)
            }
        }
    }

    private suspend fun onAtlasLoaded(cellSize: Float) {
        addActor(gemLayer)
        addActor(backLayer)

        val skins = listOf(
            "MINE_ENCHANTED_TOPAZ",
            "MINE_GLIMMERING_SAPPHIRE",
            "MINE_LUMINOUS_EMERALD",
            "MINE_PRISMATIC_DIAMOND",
            "MINE_RADIANT_RUBY",
            "MINE_SHIMMERING_OPAL")

        val positions = (0 until 20).shuffled()
        triesLeft = r.mineService.getAttemptsPerTry()
        label.setText(UiTexts.Mine.CaptionTriesLeft.value(r.l) + triesLeft.toString())

        (0 until 10).forEach { gemIndex ->
            val skin = skins.random()
            val tier = Random.nextInt(r.mineService.getMaxTier())
            val mineItem = MineItem(skin, tier)

            field[positions[gemIndex * 2]] = mineItem
            field[positions[gemIndex * 2 + 1]] = mineItem
        }

        field.forEach { (position, mineItem) ->
            val col = position % 4
            val row = position / 4

            val x = col * cellSize
            val y = row * cellSize
            val name = position.toString()

            val actor = MineItemActor(r, mineItem).apply {
                setSize(cellSize, cellSize)
                setPosition(x, y)
                this.name = name
                this.isVisible = false
            }
            gemLayer.addActor(actor)
        }

        (0 until 20).forEach { index ->
            val col = index % 4
            val row = index / 4

            val x = col * cellSize
            val y = row * cellSize
            val name = index.toString()

            val back = r.image(ATLAS, "card_back").apply {
                this.name = name
                setPosition(x, y)
                setSize(cellSize, cellSize)
            }
            back.onClick { onItemClicked(index)}
            backLayer.addActor(back)
        }
    }

    private fun onItemClicked(p: Int) {
        if (triesLeft <= 0) return

        val back = backLayer.findActor<Actor>(p.toString())
        val gem = gemLayer.findActor<MineItemActor>(p.toString())

        back.clearActions()
        gem.clearActions()

        back.setOrigin(Align.center)
        gem.setOrigin(Align.center)
        back.addAction(
            Actions.sequence(
                Actions.scaleTo(1.2f, 0.9f, 0.15f),
                Actions.parallel(
                    Actions.scaleTo(0.6f, 1.8f, 0.2f),
                    Actions.alpha(0f, 0.2f)
                ),
                Actions.visible(false)
            )
        )
        gem.addAction(
            Actions.sequence(
                Actions.alpha(0f),
                Actions.scaleTo(1f, 1f),
                Actions.visible(true),
                Actions.alpha(1f, 0.35f),
            )
        )

        if (openedPosition == -1) {
            openedPosition = p
        } else {
            triesLeft--
            label.setText(UiTexts.Mine.CaptionTriesLeft.value(r.l) + triesLeft.toString())

            val rsize = r.width / 5f

            val oldBack = backLayer.findActor<Actor>(openedPosition.toString())
            val oldGem = gemLayer.findActor<MineItemActor>(openedPosition.toString())

            oldBack.clearActions()
            oldGem.clearActions()

            oldBack.isVisible = true
            oldBack.alpha = 0f
            oldBack.addAction(
                Actions.sequence(
                    Actions.delay(2f),
                    Actions.parallel(
                        Actions.scaleTo(1.2f, 0.9f, 0.2f),
                        Actions.alpha(1f, 0.2f)
                    ),
                    Actions.scaleTo(1f, 1f, 0.15f)
                )
            )
            oldGem.addAction(Actions.sequence(
                Actions.delay(2f),
                Actions.scaleTo(1.2f, 0.9f, 0.15f),
                Actions.parallel(
                    Actions.scaleTo(0.6f, 1.8f, 0.2f),
                    Actions.alpha(0f, 0.2f)
                ),
                Actions.visible(false)
            ))
            back.addAction(
                Actions.sequence(
                    Actions.delay(2f),
                    Actions.alpha(0f),
                    Actions.visible(true),
                    Actions.parallel(
                        Actions.scaleTo(1.2f, 0.9f, 0.2f),
                        Actions.alpha(1f, 0.2f)
                    ),
                    Actions.scaleTo(1f, 1f, 0.15f)
                )
            )
            gem.addAction(Actions.sequence(
                Actions.delay(2f),
                Actions.scaleTo(1.2f, 0.9f, 0.15f),
                Actions.parallel(
                    Actions.scaleTo(0.6f, 1.8f, 0.2f),
                    Actions.alpha(0f, 0.2f)
                ),
                Actions.visible(false)
            ))

            val oldItem = field[openedPosition]!!
            val nowItem = field[p]!!
            if (oldItem == nowItem) {
                val rp = rewardLayer.children.size
                val col = rp % 5
                val row = rp / 5
                val rx = col * rsize
                val ry = -(row + 1) * rsize

                rewards.add(oldItem)

                val ractor = MineItemActor(r, oldItem).apply {
                    setPosition(rx, ry)
                    setSize(rsize, rsize)
                }
                rewardLayer.addActor(ractor)

                oldBack.addAction(
                    Actions.sequence(
                        Actions.delay(2.5f),
                        Actions.alpha(0f, 0.2f),
                        Actions.removeActor()
                    )
                )
                oldGem.addAction(
                    Actions.sequence(
                        Actions.delay(2.5f),
                        Actions.alpha(0f, 0.2f),
                        Actions.removeActor()
                    )
                )
                back.addAction(
                    Actions.sequence(
                        Actions.delay(2.5f),
                        Actions.alpha(0f, 0.2f),
                        Actions.removeActor()
                    )
                )
                gem.addAction(
                    Actions.sequence(
                        Actions.delay(2.5f),
                        Actions.alpha(0f, 0.2f),
                        Actions.removeActor()
                    )
                )
            } else {

            }

            if (triesLeft <= 0) {
                addAction(Actions.sequence(
                    Actions.delay(4f),
                    Actions.run {
                        KtxAsync.launch {
                            val rewardEntities = rewards.map { gem ->
                                val template = r.mineService.getGemTemplate(gem.skin)
                                FrontItemEntryModel.GemItemEntryModel(
                                    skin = gem.skin,
                                    amount = 0,
                                    level = 0,
                                    rarity = gem.tier,
                                    name = template.name,
                                    gem = gem
                                )
                            }
                            r.mineService.addGems(rewards)

                            val resultActor = EncounterResultDialog(
                                r,
                                result = EncounterResultModel.MineResult(
                                    gems = rewardEntities,
                                    level = r.mineService.level()
                                ),
                                onClose = { stack.moveBack(); onClose() },
                                onItemClick = { Unit },
                                onStartLevel = { _ -> Unit }
                            )

                            stack.showScreen(resultActor)
                        }
                    }
                ))
            }

            openedPosition = -1
        }
    }
}

private const val ATLAS = "atlases/mine.atlas"
