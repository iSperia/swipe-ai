package com.game7th.items

import com.game7th.swipe.SbText

enum class ItemCategory(val label: SbText) {
    HELMET(SbText("Helmet", "Шлем")),
    GLOVES(SbText("Gloves", "Перчатки")),
    BOOTS(SbText("Boots", "Ботинки")),
    AMULET(SbText("Necklace", "Подвеска")),
    RING(SbText("Ring", "Кольцо")),
    BELT(SbText("Belt", "Пояс"))
}
