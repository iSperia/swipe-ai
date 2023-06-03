package com.pl00t.swipe_client.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.log.debug
import kotlin.math.max
import kotlin.math.min


class MapScreen(
    amCore: AssetManager
) : StageScreen(amCore), GestureDetector.GestureListener {

    lateinit var amMap: AssetManager
    lateinit var taMap: TextureAtlas
    lateinit var taCharacter: TextureAtlas

    lateinit var mapActor: Group
    lateinit var mapImage: Image

    private val gestureDetector = GestureDetector(this)

    override fun show() {
        debug("MapScreen") { "map screen is shown" }
        amMap = AssetManager().apply {
            load("atlases/map.atlas", TextureAtlas::class.java)
            load("atlases/charValerion.atlas", TextureAtlas::class.java)
        }
        loadAm(amMap, this::mapLoaded)
        Gdx.input.inputProcessor = gestureDetector
    }

    override fun render(delta: Float) {
        super.render(delta)
    }

    private fun mapLoaded() {
        debug("MapScreen") { "Map screen is loaded" }
        taMap = amMap.get("atlases/map.atlas", TextureAtlas::class.java)
        taCharacter = amMap.get("atlases/charValerion.atlas", TextureAtlas::class.java)
        initMapImage()
    }

    private fun initMapImage() {
        mapActor = Group()
        mapImage = Image(taMap.findRegion("map_act1")).apply {
            height = root.height
            width = root.height
            originX = 0.5f
            originY = 0.5f
        }
        mapActor.addActor(mapImage)

        root.addActor(mapActor)
    }

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        return true
    }

    override fun longPress(x: Float, y: Float): Boolean {
        return true
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        return true
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        mapActor.x = max(root.width - mapImage.imageWidth, min(0f, mapActor.x + deltaX))
        return true
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        val percentage = distance / initialDistance

        return true
    }

    override fun pinch(
        initialPointer1: Vector2?,
        initialPointer2: Vector2?,
        pointer1: Vector2?,
        pointer2: Vector2?
    ): Boolean {
        return true
    }

    override fun pinchStop() {
    }
}
