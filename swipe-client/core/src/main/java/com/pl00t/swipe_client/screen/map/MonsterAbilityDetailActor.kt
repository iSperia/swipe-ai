package com.pl00t.swipe_client.screen.map

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.game7th.swipe.game.SbMonsterAbilityConfiguration

class MonsterAbilityDetailActor(
    private val configuration: SbMonsterAbilityConfiguration,
    private val w: Float,
    private val coreAtlas: TextureAtlas,
    private val tarotAtlas: TextureAtlas,
): Group() {

    private val tarotImage: Image
//    private val title: Label
//    private val description: Label
//    private val lore: Label
    private val table: Group

    private val _tarotWidth = w * 0.2f
    private val _tarotHeight = _tarotWidth * 1.66f
    private val _titleHeight = _tarotHeight * 0.3f
    private val _descHeight = _tarotHeight * 0.35f
    private val _loreHeight = _tarotHeight * 0.35f
    private val _lineHeight = _tarotHeight * 0.25f
    private val _rowHeight = _lineHeight * 1.1f
    private val _padding = w * 0.025f
    private val _totalHeight = _tarotHeight + 2 * _padding + configuration.descriptionTable.size * _rowHeight
    private val _firstColumnWid = (w - _padding * 2f) * 0.84f

    init {

        tarotImage = Image(tarotAtlas.findRegion(configuration.skin.toString())).apply {
            width = _tarotWidth
            height = _tarotHeight
            x = _padding
            y = _totalHeight - _padding - _tarotHeight
        }

//        title = Fonts.createWhiteTitle(configuration.title, _titleHeight).apply {
//            width = w - _tarotWidth - 3 * _padding
//            height = _titleHeight
//            x = tarotImage.x + _tarotWidth + _padding
//            y = _totalHeight - _padding - _titleHeight
//        }

//        description = Fonts.createWhiteCaption(configuration.description, _lineHeight).apply {
//            width = w - _tarotWidth - 3 * _padding
//            height = _descHeight
//            setAlignment(Align.topLeft)
//            x = title.x
//            y = title.y - _descHeight
//            wrap = true
//        }

//        lore = Fonts.createCaptionAccent(configuration.lore, _lineHeight).apply {
//            width = description.width
//            height = _loreHeight
//            setAlignment(Align.topLeft)
//            x = description.x
//            y = tarotImage.y
//            wrap = true
//        }

        addActor(tarotImage)
//        addActor(title)
//        addActor(description)
//        addActor(lore)

        table = Group()
        table.height = _rowHeight * configuration.descriptionTable.size
        table.width = w
        var h = table.height
        configuration.descriptionTable.forEach { desc ->
            val columnTitleBackground = Image(coreAtlas.createPatch("white_border")).apply {
                x = _padding
                y = h - _rowHeight
                width = _firstColumnWid
                height = _rowHeight
            }
//            val columnTitleLabel = Fonts.createCaptionAccent(desc.title, _rowHeight).apply {
//                x = columnTitleBackground.x - _padding
//                y = columnTitleBackground.y
//                width = columnTitleBackground.width
//                height = columnTitleBackground.height
//                setAlignment(Align.right)
//
//            }
            val textBackground = Image(coreAtlas.createPatch("white_border")).apply {
                x = columnTitleBackground.x + _firstColumnWid
                y = columnTitleBackground.y
                width = w - 2 * _padding - _firstColumnWid
                height = _rowHeight
            }
//            val textLabel = Fonts.createWhiteCaption(desc.formatDescription(configuration.attributes), _rowHeight).apply {
//                x = textBackground.x + _padding
//                y = textBackground.y
//                width = textBackground.width
//                height = textBackground.height
//                setAlignment(Align.left)
//            }
            table.addActor(columnTitleBackground)
//            table.addActor(columnTitleLabel)
            table.addActor(textBackground)
//            table.addActor(textLabel)
            h -= _rowHeight
        }

        table.y = _padding
        addActor(table)

        height = _tarotHeight + 2 * _padding + table.height
    }
}
