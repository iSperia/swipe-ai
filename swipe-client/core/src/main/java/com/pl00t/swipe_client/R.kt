package com.pl00t.swipe_client

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.game7th.swipe.Language
import com.game7th.swipe.monsters.MonsterService
import com.pl00t.swipe_client.screen.ScreenRouter
import com.pl00t.swipe_client.services.battle.BattleService
import com.pl00t.swipe_client.services.files.FileService
import com.pl00t.swipe_client.services.items.ItemService
import com.pl00t.swipe_client.services.levels.LevelService
import com.pl00t.swipe_client.services.profile.ProfileService
import com.pl00t.swipe_client.services.profile.SwipeAct

typealias SbAssetLoadedCallback = (R) -> Unit

class R {
    var width: Float = 0f
    var height: Float = 0f
    var l: Language = Language.EN
    lateinit var inputMultiplexer: InputMultiplexer

    lateinit var router: ScreenRouter
    lateinit var profileService: ProfileService
    lateinit var levelService: LevelService
    lateinit var battleService: BattleService
    lateinit var monsterService: MonsterService
    lateinit var fileService: FileService
    lateinit var itemService: ItemService

    val assetManager = AssetManager()
    private val queue = mutableListOf<SbAssetLoadedCallback>()

    fun loadAtlas(name: String) {
        assetManager.load(AssetDescriptor(name, TextureAtlas::class.java))
    }

    fun loadSound(name: String) {
        assetManager.load(AssetDescriptor(name, TextureAtlas::class.java))
    }

    fun loadMusic(name: String) {
        assetManager.load(AssetDescriptor(name, TextureAtlas::class.java))
    }

    fun loadSkin(name: String) {
        assetManager.load(AssetDescriptor(name, Skin::class.java))
    }

    fun onLoad(action: SbAssetLoadedCallback) {
        queue.add(action)
    }

    fun unload(name: String) {
        assetManager.unload(name)
    }

    fun update() {
        if (assetManager.update()) {
            val queueSize = queue.size
            queue.take(queueSize).forEach {
                it(this@R)
                queue.removeFirst()
            }
        }
    }

    fun atlas(name: String) = assetManager.get<TextureAtlas>(name)

    fun region(atlas: String, name: String) = atlas(atlas).findRegion(name).let { it ?: throw IllegalStateException("No texture $name") }
    fun region(atlas: String, name: String, index: Int) = atlas(atlas).findRegion(name, index).let { it ?: throw IllegalStateException("No texture $name") }

    fun image(atlas: String, name: String) = Image(region(atlas, name))
    fun image(atlas: String, name: String, index: Int) = Image(region(atlas, name, index))

    fun skin() = assetManager.get(SKIN, Skin::class.java)

    fun labelWindowTitle(text: String) = Label(text, skin(), "window_title")
    fun labelFocusedCaption(text: String) = Label(text, skin(), "caption_focus")
    fun labelAction(text: String) = Label(text, skin(), "action_label")
    fun labelLore(text: String) = Label(text, skin(), "lore_text").apply { wrap = true }
    fun regular24Focus(text: String) = Label(text, skin(), "regular24_focus").apply { wrap = true }
    fun regular24White(text: String) = Label(text, skin(), "regular24_white").apply { wrap = true }
    fun regular20Focus(text: String) = Label(text, skin(), "regular20_focus").apply { wrap = true }
    fun regular20White(text: String) = Label(text, skin(), "regular20_white").apply { wrap = true }

    companion object {
        const val ui_atlas = "styles/ui.atlas"
        const val ux_atlas = "styles/ux.atlas"
        const val battle_atlas = "atlases/battle.atlas"
        const val core_atlas = "atlases/core.atlas"
        const val tarot_atlas = "atlases/tarot.atlas"
        const val skills_atlas = "atlases/skills.atlas"
        const val units_atlas = "atlases/units.atlas"
        const val map_atlas = "atlases/map.atlas"

        fun actAtlas(act: SwipeAct) = "atlases/$act.atlas"
        const val SKIN = "styles/ux.json"
    }
}
