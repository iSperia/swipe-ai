package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.game7th.items.ItemCategory
import com.game7th.swipe.game.SbMonsterConfiguration
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.reward.CurrencyRewardEntryActor
import com.pl00t.swipe_client.screen.items.ItemBrowserAction
import com.pl00t.swipe_client.screen.items.ItemBrowserActor
import com.pl00t.swipe_client.services.profile.CollectedReward
import com.pl00t.swipe_client.services.profile.SwipeCharacter
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.ScreenTitle
import com.pl00t.swipe_client.ux.hideToBehindAndRemove
import com.pl00t.swipe_client.ux.raiseFromBehind
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.async.KtxAsync

private enum class DisplayMode {
    DESCRIPTION,
    ABILITIES,
}

class MonsterDetailActor(
    private val spaceHeight: Float,
    private val monsterInfo: SbMonsterConfiguration,
    private var character: SwipeCharacter?,
    private val context: SwipeContext,
    private val skin: Skin,
    private val router: MapScreenRouter,
) : Group(), (ItemCategory, String?) -> Unit {

    lateinit var scroll: ScrollPane
    private var attributesActor: AttributeActor? = null
    private var itemBrowser: ItemBrowserActor? = null

    init {
        reloadData()
    }

    private fun reloadData() {
        clearChildren()
        KtxAsync.launch {
            val table = Table()
            val blackBackground = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_dark_blue")).apply {
                width = context.width()
                height = context.height()
            }
            val title = ScreenTitle.createScreenTitle(context, skin, monsterInfo.name).apply {
                y = spaceHeight - 60f
                x = 60f
            }


            if (character != null) {
                val label = Label("Character Info", skin, "wave_caption").apply {
                    width = 480f
                    setAlignment(Align.center)
                }
                table.add(label).padBottom(10f).row()

                val actor = HeroAttributesEquipmentActor(context, skin, character!!, router, this@MonsterDetailActor)
                table.add(actor).width(480f).colspan(2).row()
            } else {
                val monsterImage =
                    Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion(monsterInfo.skin.toString())).apply {
                        name = "monster_image"
                        width = 120f
                        height = 200f
                        setScaling(Scaling.stretch)
                    }


                val paragraphIndex = monsterInfo.lore.indexOf("\n")
                val needReadMore = paragraphIndex > 0
                val text = if (needReadMore) monsterInfo.lore.take(paragraphIndex) else monsterInfo.lore

                val monsterLore = Label(text, skin, "lore_small").apply {
                    name = "monster_lore"
                    width = 340f
                    wrap = true
                    setAlignment(Align.topLeft)
                }

                val loreTable = Table()
                val loreTextTable = Table()
                loreTable.add(monsterImage).width(120f).padLeft(5f).padRight(5f).height(200f)
                loreTextTable.add(monsterLore).padLeft(5f).padRight(5f).align(Align.bottomLeft).width(340f)
                loreTextTable.row()
                if (needReadMore) {
                    val readMore = Label("Read more...", skin, "text_small").apply {
                        width = 340f
                        setAlignment(Align.topRight)
                    }

                    readMore.onClick {
                        readMore.isVisible = false
                        readMore.remove()
                        monsterLore.setText(monsterInfo.lore)
                    }
                    loreTextTable.add(readMore).align(Align.right)
                }
                loreTable.add(loreTextTable)

                val loreLabel = Label("Description", skin, "wave_caption").apply {
                    width = 480f
                    setAlignment(Align.center)
                }
                table.add(loreLabel).padBottom(10f).row()
                table.add(loreTable).row()

                val attributesLabel = Label("Attributes", skin, "wave_caption").apply {
                    width = 480f
                    setAlignment(Align.center)
                }
                table.add(attributesLabel).padBottom(10f).row()
            }

            val char = character
            if (char != null) {
                val experienceBar = ExperienceBar(context, skin).apply {
                    width = 460f
                    height = 30f
                    setProgress(char.level.experience, char.level.maxExperience)
                }
                table.add(experienceBar).pad(10f).row()
                val experienceLabel = Label(
                    "Level: ${char.level.level} (${char.level.experience}/${char.level.maxExperience})",
                    skin,
                    "text_regular"
                ).apply {
                    width = 460f
                    setAlignment(Align.left)
                }
                table.add(experienceLabel).padLeft(10f).padRight(10f).width(460f).minWidth(460f).row()

                scroll = ScrollPane(table).apply {
                    width = 480f
                    height = spaceHeight - 60f
                }

                val profile = context.profileService().getProfile()
                val expBalances = listOf(
                    SwipeCurrency.SCROLL_OF_WISDOM,
                    SwipeCurrency.TOME_OF_ENLIGHTMENT,
                    SwipeCurrency.CODEX_OF_ASCENDANCY,
                    SwipeCurrency.GRIMOIRE_OF_OMNISCENCE,
                )
                expBalances.map { context.profileService().getCurrency(it) }.map {
                    val balance = profile.getBalance(it.currency)
                    CollectedReward.CountedCurrency(it.currency, balance, it.name, it.rarity, it.description)
                }.filter { it.amount > 0 }.forEach { currency ->
                    val currencyActor = CurrencyRewardEntryActor(scroll.width - 30f, currency, context, skin)
                    val useButton = Buttons.createShortActionButton("Use", skin).apply {
                        x = 390f
                        y = 18f
                    }
                    val group = Group().apply {
                        width = 460f
                        height = 84f
                    }
                    group.addActor(currencyActor)
                    group.addActor(useButton)
                    table.add(group).padLeft(10f).padRight(10f).row()

                    useButton.onClick {
                        val result =
                            context.profileService().spendExperienceCurrency(currency.currency, monsterInfo.skin)
                        attributesActor?.updateAttributes(result.character.attributes)

                        val oldCharacter = character!!
                        character = result.character
                        character?.let { character ->
                            val nextProgress = if (oldCharacter.level.level < character.level.level) {
                                oldCharacter.level.maxExperience
                            } else {
                                character.level.experience
                            }
                            experienceBar.setProgress(nextProgress, oldCharacter.level.maxExperience)

                            addAction(Actions.delay(0.3f, Actions.run {
                                experienceLabel.setText("Level: ${character.level.level} (${character.level.experience}/${character.level.maxExperience})")
                                currencyActor.updateAmount(result.balance)
                                if (oldCharacter.level.level < character.level.level) {
                                    experienceLabel.addAction(Actions.sequence(
                                        Actions.scaleTo(2f, 2f, 0.1f),
                                        Actions.scaleTo(1f, 1f, 0.3f),
                                        Actions.run { experienceBar.setProgress(0, character.level.maxExperience) }
                                    ))
                                }
                            }))
                        }
                    }
                }
            }
            if (char == null) {
                attributesActor = AttributeActor(
                    attributes = character?.attributes ?: monsterInfo.attributes,
                    mode = if (character == null) AttributeActor.Mode.PERCENT else AttributeActor.Mode.ABSOLUTE,
                    context = context,
                    skin = skin
                )
                table.add(attributesActor).align(Align.center).row()
            }

            val abilitiesLabel = Label("Abilities", skin, "wave_caption").apply {
                width = 480f
                setAlignment(Align.center)
            }
            table.add(abilitiesLabel).padBottom(10f).row()

            val abilsTable = Table()

            monsterInfo.abilities?.forEach { ability ->
                val tarot =
                    Image(context.commonAtlas(Atlases.COMMON_SKILLS).findRegion(ability.skin.toString())).apply {
                        width = 120f
                        height = 120f
                        setScaling(Scaling.stretch)
                    }
                abilsTable.add(tarot).pad(5f).width(120f).height(120f).align(Align.topLeft)

                val sideTable = Table()
                sideTable.add(Label(ability.title, skin, "wave_caption").apply {
                    width = 330f
                    setAlignment(Align.left)
                }).width(330f).pad(5f).colspan(2)
                sideTable.row()
                sideTable.add(Label(ability.description, skin, "text_regular").apply {
                    setAlignment(Align.left)
                    wrap = true
                }).width(330f).pad(5f).colspan(2)
                sideTable.row()

                ability.descriptionTable.forEach { row ->
                    val titleLabel = Label(row.title, skin, "text_small").apply {
                        wrap = true
                        width = 250f
                        setAlignment(Align.right)
                    }
                    sideTable.add(titleLabel).width(260f).pad(5f)
                    val valueLabel = Label(row.formatDescription(monsterInfo), skin, "text_small_accent").apply {
                        wrap = true
                        width = 50f
                        setAlignment(Align.left)
                    }
                    sideTable.add(valueLabel).width(60f).pad(5f)
                    sideTable.row()
                }

                sideTable.add(Label(ability.lore, skin, "lore_small").apply {
                    wrap = true
                    setAlignment(Align.left)
                }).width(330f).pad(5f).colspan(2)

                abilsTable.add(sideTable).width(340f).maxWidth(340f).minHeight(120f).align(Align.topLeft)
                abilsTable.row()
            }

            table.add(abilsTable).row()



            addActor(blackBackground)

            addActor(title)
            addActor(scroll)
        }
    }

    override fun invoke(category: ItemCategory, itemId: String?) {
        KtxAsync.launch {
            itemBrowser?.let {
                it.hideToBehindAndRemove(640f)
            }
            itemBrowser = ItemBrowserActor(
                categoryFilter = category,
                selectedId = itemId,
                browserWidth = 480f,
                browserHeight = 360f,
                context = context,
                skin = skin,
                actionsProvider = { item ->
                    val equippedItemId = context.profileService().getItems().firstOrNull { it.equippedBy == monsterInfo.skin }?.id
                    val actions = mutableListOf(ItemBrowserAction.CLOSE)
                    if (item.id == equippedItemId) {
                        actions.add(ItemBrowserAction.UNEQUIP)
                    } else {
                        actions.add(ItemBrowserAction.EQUIP)
                    }
                    actions
                },
                actionsHandler = { action, item ->
                    when (action) {
                        ItemBrowserAction.CLOSE -> itemBrowser?.let { it.hideToBehindAndRemove(640f) }
                        ItemBrowserAction.EQUIP -> {
                            context.profileService().equipItem(monsterInfo.skin, item)
                            reloadData()
                        }
                        ItemBrowserAction.UNEQUIP -> {
                            context.profileService().unequipItem(item.id)
                            reloadData()
                        }
                        else -> {}
                    }
                }
            )
            itemBrowser?.raiseFromBehind(640f)
            context.profileService().getItems().firstOrNull { it.id == itemId }?.let { item ->

            }
            addActor(itemBrowser)
        }
    }
}
