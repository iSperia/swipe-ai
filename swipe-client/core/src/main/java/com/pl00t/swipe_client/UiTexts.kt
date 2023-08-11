package com.pl00t.swipe_client

import com.game7th.swipe.SbText

object UiTexts {

    val NavItems = SbText("Items", "Предметы")
    val MakeActive = SbText("Make Active", "Сделать Активным")
    val NavShop = SbText("Shop", "Лавка")
    val FilterCurrency = SbText("Currency", "Валюта")
    val FilterAmulet = SbText("Amulets", "Подвески")
    val FilterBelt = SbText("Belts", "Пояса")
    val FilterBoots = SbText("Boots", "Ботинки")
    val FilterGloves = SbText("Gloves", "Перчатки")
    val FilterHelm = SbText("Helmets", "Шлемы")
    val FilterRing = SbText("Rings", "Кольца")
    val NavParty = SbText("Heroes", "Герои")
    val Tarot = SbText("Fate Deck", "Колода Судьбы")

    val BattleVictory = SbText("Victory", "Победа")
    val BattleDefeat = SbText("Defeat", "Поражение")
    val ExpBoost = SbText("+$ experience", "+$ очков опыта")
    val RetryLevel = SbText("Try again", "Попробовать заново")

    val RaidPossibleMonsters = SbText("Possible enemies:", "Возможные противники")
    val RaidPossibleRewards = SbText("Possible rewards:", "Возможные награды")
    val RaidFreeRewards = SbText("Free rewards collected:", "Полученные бесплатные награды:")
    val RaidRichRewards = SbText("Rewards collected:", "Полученные награды:")
    val RaidCollectRewards = SbText("Collect Rewards", "Собрать Награды")
    val RaidLittleCoins = SbText("You have no enough coins to collect the rewards!", "У вас недостаточно эфирных монет, чтобы собрать награду!")

    val LevelUpApply = SbText("Apply", "Применить")
    val Details = SbText("Details", "Детали")
    val PutOff = SbText("Unequip Item", "Снять предмет")
    val PutOn = SbText("Equip Item", "Надеть предмет")
    val UseItem = SbText("Use item", "Применить предмет")
    val Dust = SbText("Dust item", "Распылить предмет")
    val DustWarning = SbText("WARNING: if you dust the item, it is destroyed and cannot be undone\n\nYou will receive:",
        "ПРЕДУПРЕЖДЕНИЕ: Если вы распылите предмет, он будет уничтожен и это нельзя отменить\n\nВы получите:")

    val Settings = SbText("Settings", "Настройки")
    val LvlPrefix = SbText("Level: ", "Уровень: ")
    val LvlShortPrefix = SbText("Lv.", "Ур.")
    val WaveTemplate = SbText("Wave $", "Волна $")
    val ButtonAttack = SbText("Attack", "Атаковать")
    val ButtonSkillset = SbText("Skills", "Навыки")
    val ButtonStats = SbText("Attributes", "Атрибуты")
    val ButtonInfo = SbText("Info", "Инфо")
    val ButtonParameters = SbText("Stats", "Параметры")
    val ButtonResistances = SbText("Resistances", "Сопротивления")
    val ButtonStory = SbText("Story", "История")

    val AttributeLabelBody = SbText("Body", "Тело")
    val AttributeLabelSpirit = SbText("Spirit", "Дух")
    val AttributeLabelMind = SbText("Mind", "Разум")
    val AttributeLabelHealth = SbText("Health", "Здоровье")
    val AttributeLabelLuck = SbText("Luck", "Удача")
    val AttributeLabelUltProgress = SbText("Ult. progress", "Наполнение ульт. способности")
    val AttributeLabelPhysResist = SbText("Resist to phys.", "Физ. сопротивление")
    val AttributeLabelColdResist = SbText("Resist to cold", "Сопротивление холоду")
    val AttributeLabelFireResist = SbText("Resist to fire", "Сопротивление огню")
    val AttributeLabelDarkResist = SbText("Resist to dark", "Сопротивление тьме")
    val AttributeLabelLightResist = SbText("Resist to light", "Сопротивление свету")
    val AttributeLabelShockResist = SbText("Resist to shock", "Сопротивление шоку")

    object Tutorials {
        val Act1Intro = SbText("Press the level icon to see the encounter details", "Нажмите значок уровня, чтобы увидеть подробности")
        val C1Details = SbText("This is the details of level. To start the battle, press the Attack button", "Это детали уровня. Чтобы начать битву, нажмите кнопку Атаковать")

        object A1C1 {
            val T1 = SbText("This is where opposing sides stand.", "Поле битвы — здесь стоят противоборствующие стороны.")
            val T2 = SbText("Your character, Valerian, stands on the left side.", "Ваш персонаж, Валериан, стоит слева.")
            val T3 = SbText("Character Valerian HP Bar - Watch your hero's HP closely. If it reaches zero, Valerian will fall, and you'll lose the battle.", "шкала HP персонажа Валериана — внимательно следите за HP вашего героя. Если он достигнет нуля, Валериан падет, и вы проиграете битву.")
            val T4 = SbText("Thornstalker Character - This is your enemy. You must strategically use your skills to defeat Thornstalker.", "Терновый охотник — это ваш враг. Вы должны стратегически использовать свои навыки, чтобы победить Тернового Охотника.")
            val T5 = SbText("Tilefield - This is the grid where skill symbols appear. Each symbol represents a skill you can use.", "Игровое поле — это сетка, в которой отображаются символы навыков. Каждый символ представляет собой навык, который вы можете использовать.")
            val T6 = SbText("Skill Symbol - Radiant Strike - Use this skill to perform a powerful physical attack.", "Символ навыка — Сияющий Удар — используйте этот навык для выполнения мощной физической атаки.")
            val T7 = SbText("Skill Symbol - Sigils of Renewal - This skill heals Valerian when used on the corresponding tiles.", "Символ навыка — Печать обновления — этот навык исцеляет Валериана при использовании на соответствующих плитках.")
            val T8 = SbText("Skill Symbol - Luminous Beam - This skill deals massive damage with light. Useful when there are several enemies.", "Символ навыка — Луч Света — этот навык наносит массовый урон светом. Полезно, когда врагов несколько.")
            val T9 = SbText("To use a skill, you need to merge enough same symbols into one place. Swipe up on the screen to move the skill symbols. Arrange them to create powerful combos.", "Чтобы использовать навык, вам нужно объединить достаточное количество одинаковых символов в одно место. Проведите вверх по экрану, чтобы переместить символы навыков. Располагайте их, чтобы создать мощные комбинации")
            val T10 = SbText("Whenever you swipe, all your skill symbols move in that direction until they reach the edge of the field or are blocked by another symbol. Similar symbols will merge into one for powerful effects.", "Механика смахивания. Всякий раз, когда вы проводите пальцем, все ваши символы навыков перемещаются в этом направлении, пока не достигнут края поля или не будут заблокированы другим символом. Похожие символы объединятся в один для мощных эффектов.")
            val T11 = SbText("After each turn, a random skill symbol will appear on the field. See that new Radiant Strike symbol? Plan your moves accordingly.", "После каждого хода на поле будет появляться случайный символ навыка. Видите этот новый символ Сияющего Удара? Мудро планируйте свои действия.")
            val T12 = SbText("Three Radiant Strike symbols are placed in a row. You may swipe left to merge them all. Three symbols are enough to unleash this skill!", "Три символа Сияющий Удар располагаются в ряд. Вы можете провести пальцем влево, чтобы объединить их все. Трех символов достаточно, чтобы раскрыть этот навык!")
            val T13 = SbText("Swipe left to merge Radiant Strike!", "Проведите влево, чтобы объединить символы!")
            val T14 = SbText("Great! Now that you've merged the symbols, Radiant Strike is used. Enemy Health - Thornstalker's health has been reduced after your powerful attack. Keep attacking to defeat him!!", "Отлично! Теперь, когда вы объединили символы, активировался Сияющий Удар. Здоровье Тернистого охотника уменьшилось после вашей мощной атаки. Продолжайте атаковать, чтобы победить его!")
            }

        object Battle {
            val SigilOfRenewal = SbText("Take a look at the newly generated Sigil of Renewal tile on your field. If you use a skill on this tile, it will heal you! Keep an eye out for healing opportunities during battles.", "Взгляните на сотворенную плитку Печати Обновления на вашем поле. Если вы используете навык на этой плитке, он исцелит вас! Следите за возможностями исцеления во время сражений.")
            val Weakness = SbText(" Look out! You've been hit with weakness. Those tiles reduce your damage by 2.5% each. To get back your full strength, use a skill on the weakness tile and break free from this debuff!", "Осторожно! Вас поразила слабость. Эти плитки уменьшают ваш урон на 2,5% каждая. Чтобы восстановить свою полную силу, используйте навык на плитке слабости и освободитесь от этого дебаффа!")
            val Poison = SbText("Oh no, it seems you've been poisoned! Poison symbols have appeared on your field. If you don't do something about it, you'll take dark damage every turn. Try to merge the poison symbols to remove this harmful effect and keep yourself safe!", "О нет, кажется, вас отравили! На вашем поле появились символы яда. Если вы ничего с этим не сделаете, вы будете получать урон тьмой каждый ход. Попробуйте объединить символы яда, чтобы удалить этот вредный эффект и обезопасить себя!")
        }
    }
}
