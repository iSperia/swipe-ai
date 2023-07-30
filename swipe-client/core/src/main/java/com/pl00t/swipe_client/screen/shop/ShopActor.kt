package com.pl00t.swipe_client.screen.shop

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.game7th.items.InventoryItem
import com.pl00t.swipe_client.Atlases
import com.pl00t.swipe_client.SwipeContext
import com.pl00t.swipe_client.screen.battle.BattleDialogActor
import com.pl00t.swipe_client.screen.items.CurrencyCellActor
import com.pl00t.swipe_client.screen.items.InventoryCellActor
import com.pl00t.swipe_client.screen.map.MapScreenRouter
import com.pl00t.swipe_client.screen.navpanel.NavigationPanel
import com.pl00t.swipe_client.screen.reward.RewardDialog
import com.pl00t.swipe_client.services.levels.DialogEntryModel
import com.pl00t.swipe_client.services.levels.DialogOrientation
import com.pl00t.swipe_client.services.profile.CurrencyMetadata
import com.pl00t.swipe_client.services.profile.SwipeCurrency
import com.pl00t.swipe_client.ux.Buttons
import com.pl00t.swipe_client.ux.ScreenTitle
import com.pl00t.swipe_client.ux.hideToBehindAndRemove
import kotlinx.coroutines.launch
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.async.KtxAsync
import kotlin.random.Random

private val phrases = listOf(
    "Step into my realm of wonders, where stars whisper secrets of forgotten times and dreams intermingle with reality.",
    "In Aetheria, the skies hold tales of lost loves and celestial tragedies, just as these artifacts reveal their own stories to those who dare to listen.",
    "Long ago, a comet danced across the heavens, bringing joy and sorrow in equal measure to my homeland of Aetheria.",
    "Aetheria's floating gardens blossom with mythical flora, while in its shadows, cosmic beings weave threads of fate.",
    "Amidst the shimmering clouds, Aetheria's noble winged guardians soar, forever watching over their sacred realm.",
    "I've seen brave souls venture through these doors, seeking treasures that would rewrite their destinies.",
    "An adventurer once arrived with a relic from the Kingdom of Necromancy, seeking redemption for a past steeped in darkness.",
    "A young prince from the Kingdom of Light once visited, his heart burdened with a longing for lost harmony.",
    "A fierce barbarian warrior once traded tales of her dragon-slaying feats, while searching for a mystical blade said to rival her own strength.",
    "Legend has it that a forest dweller from the Kingdom of Forests sought the legendary amulet, hoping to commune with the spirits of the ancient woods.",
    "A tech-savvy mage from the Kingdom of Technology once wandered in, sharing her inventions while seeking an ancient tome to unlock new knowledge.",
    "A wise sage from the Kingdom of Wisdom embarked on a quest to uncover ancient secrets, drawn by the allure of mysterious artifacts.",
    "An ambitious alchemist from the Kingdom of Intelligence once visited, eager to distill the essence of celestial wisdom into his concoctions.",
    "A master necromancer once sought an alliance with me, trading secrets of life and death to unleash newfound power.",
    "In the luminous skies of Aetheria, a tale of star-crossed lovers forever etched in the constellations.",
    "Within the gates of Aetheria, whispers of a long-lost treasure guarding the secrets of forgotten kingdoms echo through the ages.",
    "My heart aches, for I too once lost a loved one, guiding my quest to weave hope into the very fabric of the cosmos.",
    "From the depths of sorrow, I emerged stronger, choosing to shine brighter than the stars that witnessed my grief.",
    "In the darkest of nights, Aetheria's radiant light offers solace to those seeking a glimmer of hope in the abyss.",
    "As time flows like stardust in Aetheria, the echoes of each tale shared in this mystical shop resonate through the celestial realms, forever immortalized among the heavens.",
    "Harness the power of infused orbs and shards to unlock the hidden potential of your equipped gear.",
    "Dust unused items to forge precious equipment upgrade currency, paving the way for greater strength.",
    "Remember, 2-star items may possess a single additional property, so choose wisely when seeking perfection.",
    "3-star items hold the potential for two additional properties, opening the path to diverse abilities.",
    "Breathe life into 4-star items with up to three additional properties, granting you unparalleled versatility.",
    "5-star items stand as masterpieces, embracing four additional properties, a canvas for true greatness.",
    "Through upgrades, the main property of an item ascends, and a random additional property joins the ranks.",
    "An item's potential may not be fully unlocked; a level up could reveal a new additional property.",
    "Embrace the power of scrolls of wisdom and other experience items to empower your hero.",
    "Body attribute fortifies your health, preparing you for the battles that lie ahead.",
    "Spirit attribute enhances your luck, ensuring favorable outcomes when fate intertwines with your journey.",
    "Nurture your mind attribute, accelerating the preparation time of your ultimate ability, a beacon of immense power."
)

class ShopActor(
    private val context: SwipeContext,
    private val skin: Skin,
    private val router: MapScreenRouter
) : Group() {

    lateinit var balanceLabel: Label
    val items = Group().apply {
        height = 300f
        width = 480f
        y = 300f
    }

    init {
        KtxAsync.launch {
            val bg = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_dark_blue")).apply {
                width = 480f
                height = 660f
            }
            addActor(bg)

            val line = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("panel_line")).apply {
                width = 480f
                y = 658f
            }
            addActor(line)

            val title = ScreenTitle.createScreenTitle(context, skin, "Mystery Shop").apply {
                y = bg.height - 28f
                x = 60f
            }

            val characterImage = Image(context.commonAtlas(Atlases.COMMON_UNITS).findRegion("CHARACTER_ZEPHYR")).apply {
                width = 300f
                height = 450f
                touchable = Touchable.disabled
                x = -100f
                y = -40f
            }
            addActor(characterImage)

            val panel = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion("bg_blue")).apply {
                width = 480f
                height = 50f
            }
            addActor(panel)

            drawPhrase()

            val closeButton = Buttons.createActionButton("Close", skin).apply {
                x = 300f
                y = 7f
            }
            closeButton.onClick {
                router.activeCharacterChanged()
                this@ShopActor.hideToBehindAndRemove(context.height())
            }

            val rerollButton = Buttons.createActionButton("Reroll", skin).apply {
                x = 10f
                y = 7f
            }
            rerollButton.onClick {
                KtxAsync.launch {
                    context.profileService().rerollMysteryShop()
                    refreshBalance()
                    drawShop()
                }
            }
            val icon = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion(SwipeCurrency.ETHERIUM_COIN.toString())).apply {
                width = 28f
                height = 28f
                x = rerollButton.x + rerollButton.width - 32f
                y = rerollButton.y + 4f
                touchable = Touchable.disabled
            }
            val amountLabel = Label("10", skin, "affix_text").apply {
                x = icon.x - 42f
                y = icon.y + 1f
                setAlignment(Align.right)
                touchable = Touchable.disabled
                width = 40f
                height = icon.height
            }
            addActor(rerollButton)
            addActor(icon)
            addActor(amountLabel)

            balanceLabel = Label("", skin, "affix_text").apply {
                x = 10f
                height = 20f
                width = 150f
                y = 610f
                setAlignment(Align.right)
            }
            val balanceIcon = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion(SwipeCurrency.ETHERIUM_COIN.toString())).apply {
                x = balanceLabel.x + balanceLabel.width + 2f
                y = balanceLabel.y - 2f
                width = 24f
                height = 24f
            }
            addActor(balanceLabel)
            addActor(balanceIcon)
            drawBalance()

            addActor(closeButton)

            addActor(title)

            addActor(items)
            drawShop()
        }
    }

    private suspend fun refreshBalance() {
        balanceLabel.setText("Your balance: ${context.profileService().getProfile().getBalance(SwipeCurrency.ETHERIUM_COIN)}")
    }

    private suspend fun drawBalance() {
        balanceLabel.setText("Your balance: ${context.profileService().getProfile().getBalance(SwipeCurrency.ETHERIUM_COIN)}")
    }

    private fun drawPhrase() {
        val sentenceText = phrases.random()

        val dialogBackground = Image(context.commonAtlas(Atlases.COMMON_UX).createPatch("dialog_bubble")).apply {
            width = 360f
            height = 240f
            x = 100f
            y = 70f
        }
        val titleLabel = Label("Zephyr, the Enigmatic Trader", skin, "dialog_title").apply {
            x = dialogBackground.x + 10f
            y = dialogBackground.y + 150f
            wrap = true
            width = dialogBackground.width
            height = 40f
            setAlignment(Align.center)
            touchable = Touchable.disabled
        }
        val dialogLabel = Label(sentenceText, skin, "text_regular").apply {
            x = titleLabel.x + 20f
            width = titleLabel.width - 50f
            y = dialogBackground.y + 10f
            height = 140f
            setAlignment(Align.topLeft)
            wrap = true
            touchable = Touchable.disabled
        }
        addActor(dialogBackground)
        addActor(titleLabel)
        addActor(dialogLabel)
    }

    private suspend fun drawShop() {
        items.clearChildren()
        context.profileService().getMysteryShop().forEachIndexed { index, shopItem ->
            val group = Group().apply {
                width = 80f
                height = 150f
                x = (index % 6) * 80f
                y = 150f - (index / 6) * 150f
            }
            val imgActor = if (shopItem.currency != null) {
                val meta = context.profileService().getCurrency(shopItem.currency)
                CurrencyCellActor(context, skin, 80f, CurrencyMetadata(meta.currency, meta.lore, meta.name, meta.rarity, meta.description))
            } else {
                val meta = context.itemService().getItemTemplate(shopItem.item!!)!!
                InventoryCellActor(context, skin, 80f, InventoryItem(meta.skin, meta.skin, emptyList(), emptyList(), 0, 0, shopItem.rarity, meta.category, null, 0))
            }
            imgActor.apply {
                y = 70f
            }
            items.addActor(group)
            val title = Label(shopItem.title, skin, "lore_small").apply {
                width = 76f
                y = 40f
                x = 2f
                height = 28f
                wrap = true
                setAlignment(Align.left)
            }
            group.addActor(title)

            val priceLabel = Label("${shopItem.price}", skin, "affix_text").apply {
                width = 55f
                height = 40f
                x = 32f
                setAlignment(Align.left)
            }
            group.addActor(priceLabel)

            group.onClick {
                KtxAsync.launch {
                    val reward = context.profileService().buyMysteryItem(shopItem.id)
                    val dialogActor = RewardDialog(reward, context, skin, "Close") {}.apply {
                        x = 40f
                        y = 200f
                    }
                    this@ShopActor.addActor(dialogActor)
                    drawShop()
                    drawBalance()
                }
            }

            val priceIcon = Image(context.commonAtlas(Atlases.COMMON_UX).findRegion(SwipeCurrency.ETHERIUM_COIN.toString())).apply {
                width = 28f
                height = 28f
                x = 2f
                y = 8f
            }
            group.addActor(priceIcon)

            group.addActor(imgActor)
            group.alpha = 0f
            group.setScale(2f)

            val dx = 300f * (Random.nextFloat() - 0.5f)
            val dy = 300f * (Random.nextFloat() - 0.5f)
            val dr = Random.nextFloat() * 360f - 180f
            group.moveBy(dx, dy)
            group.setOrigin(Align.center)
            group.rotateBy(dr)

            group.addAction(Actions.parallel(
                Actions.alpha(1f, 0.8f),
                Actions.scaleTo(1f, 1f, 0.8f),
                Actions.moveBy(-dx, -dy, 0.8f),
                Actions.rotateTo(0f, 0.8f)
            ))
        }
    }
}
