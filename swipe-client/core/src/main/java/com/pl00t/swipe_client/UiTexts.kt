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
        val Act1Hero1 = SbText("Now that you've collected loot, it's time to enhance your character. Tap on the \"Party\" button to explore character details and equip new items. Strengthen Valerian and be prepared for the challenges ahead!", "Теперь, когда вы собрали добычу, пришло время улучшить своего персонажа. Нажмите на кнопку «Герои», чтобы изучить детали персонажа и экипировать новые предметы. Усильте Валериана и будьте готовы к грядущим испытаниям!")
        val Act1Hero2 = SbText("Valerian is currently your only companion on this journey, but as your adventure progresses, more heroes may join your party. To get started, let's take a closer look at Valerian's details. Tap on his profile to access his attributes, skills, and equipment.", "В настоящее время Валериан — ваш единственный компаньон в этом путешествии, но по мере развития вашего приключения к вашей группе могут присоединиться новые герои. Для начала давайте подробнее рассмотрим детали Валериана. Нажмите на его профиль, чтобы получить доступ к его атрибутам, навыкам и снаряжению.")
        val A1C1R1 = SbText("As you gain experience through battles, your abilities will grow stronger. Keep adventuring to become more powerful and face even greater challenges!", "По мере того, как вы набираете опыт в битвах, ваши способности становятся сильнее. Продолжайте приключения, чтобы стать сильнее и столкнуться с еще большими вызовами!")
        val A1C1R2 = SbText("Remember, the farther you go, the more loot you'll gather. This loot can be used to enhance your equipment and strengthen your characters. Onward to greater rewards!", "Помните, чем дальше вы пойдете, тем больше добычи соберете. Эта добыча может быть использована для улучшения вашего снаряжения и усиления ваших персонажей. Вперед к большим наградам!")
        val C1Details = SbText("This is the details of level. To start the battle, press the Attack button", "Это детали уровня. Чтобы начать битву, нажмите кнопку Атаковать")

        object CharacterScreen {
            val S1 = SbText("Welcome to the Character Screen! By default, the \"Info\" tab is displayed, providing you with valuable details about your hero's level, attributes, and statistics.", "Добро пожаловать на экран персонажа! По умолчанию отображается вкладка «Информация», содержащая ценную информацию об уровне, атрибутах и статистике вашего героя.")
            val S2 = SbText("Now, let's take a look at the \"Parameters\" button. Here you can see Valerian's current level. You can use experience items to help him level up and become even stronger.", "Теперь давайте посмотрим на кнопку «Параметры». Здесь вы можете увидеть текущий уровень Валериана. Вы можете использовать предметы опыта, чтобы помочь ему повысить уровень и стать еще сильнее.")
            val S3 = SbText("See the experience bar below? Valerian has gained 10 experience points from his first battle, but he needs 1000 to level up. Plenty more battles await to make him more formidable!", "Видите шкалу опыта внизу? Валериан получил 10 очков опыта в своей первой битве, но для повышения уровня ему нужно 1000. Впереди еще много сражений, которые сделают его еще более грозным!")
            val S4 = SbText("Notice the health value? Health is a crucial attribute, as the higher it is, the better the chances of your character surviving tough battles.", "Обратите внимание на значение для здоровья? Здоровье является важным атрибутом, так как чем оно выше, тем выше шансы вашего персонажа выжить в тяжелых битвах.")
            val S5 = SbText("Moving on to \"Luck\". This attribute directly impacts the effectiveness of your character's skills. The more luck Valerian has, the more damage he can deal with his abilities.", "Переходим к «Удаче». Этот атрибут напрямую влияет на эффективность навыков вашего персонажа. Чем больше удачи у Валериана, тем больше урона он может нанести своими способностями.")
            val S6 = SbText("Lastly, there's the \"Ultimate Progress\" attribute. As Valerian merges symbols during his turns, this gauge fills up, bringing him closer to unleashing his powerful ultimate ability.", "Наконец, есть атрибут «Наполнение ульт. способности». По мере того, как Валериан объединяет символы во время своих ходов, эта шкала заполняется, приближая его к раскрытию своей мощной ультимативной способности.")
            val S7 = SbText("Take a look at the two Scrolls of Wisdom in your inventory. Lucky you, these scrolls add valuable experience to your character! Let's use one of them.", "Взгляните на два Свитка Мудрости в вашем инвентаре. К счастью, эти свитки добавят ценный опыт вашему персонажу! Воспользуемся одним из них.")
            val S8 = SbText("Now, press the \"Use\" button on a Scroll of Wisdom to grant Valerian 750 experience points.", "Теперь нажмите кнопку «Использовать» на свитке мудрости, чтобы дать Валериану 750 очков опыта.")
            val S9 = SbText("Marvelous! As you can see, his experience bar has grown significantly.", "Замечательно! Как видите, его полоса опыта значительно выросла.")
            val S10 = SbText("Go ahead and use the second Scroll of Wisdom to further boost his experience.", "Используй второй свиток мудрости, чтобы еще больше повысить его опыт.")
            val S11 = SbText("Amazing! Valerian has leveled up! Notice that his attributes have increased as well. Keep battling to make him even mightier!", "Удивительно! Валериан повысил уровень! Обратите внимание, что его атрибуты также увеличились. Продолжайте сражаться, чтобы сделать его еще сильнее!")
            val S12 = SbText(" Let's move on to the \"Skills\" section. Here, you'll find detailed information about Valerian's abilities.", "Давайте перейдем к разделу «Навыки». Здесь вы найдете подробную информацию о способностях Валериана.")
            val S13 = SbText("Take a look at \"Radiant Strike\". This skill deals potent physical damage, just like you've witnessed during your fight with Thornstalker.", "Взгляните на \"Cияющий Удар\". Этот навык наносит мощный физический урон, как вы видели во время боя с Терновым Охотником.")
            val S14 = SbText("Now, focus on \"Luminous Beam\". This remarkable ability inflicts massive light damage to all enemies on the field, a true force to be reckoned with.", "Теперь сосредоточьтесь на «Светящемся луче». Эта замечательная способность наносит огромный легкий урон всем врагам на поле боя, настоящая сила, с которой нужно считаться.")
            val S15 = SbText("\"Sigils of Renewal\" is next. By swiping your tiles strategically, you can use this skill to heal Valerian during battle. A clever choice when facing tough opponents.", "Следующим будет «Печать обновления». Стратегически проводя по своим плиткам, вы можете использовать этот навык, чтобы лечить Валериана во время битвы. Умный выбор, когда вы сталкиваетесь с сильным противником.")
            val S16 = SbText("And behold, \"Divine Convergence\". Valerian's ultimate skill. It engulfs the battlefield in a dazzling display of power. This skill consumes all sigil tiles, healing Valerian and unleashing a devastating light damage wave upon your foes. It's a game-changer!", "И вот, «Божественная Конвергенция». Высшее умение Валериана. Он поглощает поле битвы ослепительной демонстрацией мощи. Этот навык поглощает все тайлы печати, исцеляя Валериана и обрушивая на ваших противников разрушительную волну светового урона. Это меняет правила игры!")
            val S17 = SbText("Don't forget to explore the detailed description of each ability. This way, you can harness their full potential and witness their incredible growth as Valerian becomes more powerful.", "Рассказчик: Не забудьте изучить подробное описание каждой способности. Таким образом, вы сможете использовать весь их потенциал и стать свидетелем их невероятного роста по мере того, как Валериан становится все более могущественным.")
            val S18 = SbText("Now, let's take a look at the \"Items\" section. Here, you can manage the equipment that Valerian carries into battle.", "Теперь давайте посмотрим на раздел «Предметы». Здесь вы можете управлять снаряжением, которое Валериан несет в бой.")
            val S19 = SbText("As you can see, Valerian currently has some equipment slots available. These slots are where you can equip items to enhance his abilities and attributes.", "Как видите, сейчас у Валериана есть несколько свободных слотов для снаряжения. В эти слоты вы можете экипировать предметы, чтобы улучшить его способности и атрибуты.")
            val S20 = SbText("In the world of The Shattered Kingdoms, there are six types of equipment: Rings, Amulets, Belts, Gloves, Helmets, and Boots.", "В мире Расколотых Королевств есть шесть типов снаряжения: кольца, амулеты, ремни, перчатки, шлемы и сапоги.")
            val S21 = SbText(" Equipping the right combination of items can greatly improve Valerian's performance in battle. Be sure to choose items that suit your preferred playstyle and strategy.", "Экипировка правильной комбинации предметов может значительно повысить эффективность Валериана в бою. Обязательно выбирайте предметы, которые соответствуют вашему предпочтительному стилю игры и стратегии.")
            val S22 = SbText("Let's take a closer look at that new ring you found after defeating the Thornstalker. Simply select the \"Ring\" slot to examine its details.", "Давайте внимательнее посмотрим на то новое кольцо, которое вы нашли после победы над Тернистым охотником. Просто выберите слот «Кольцо», чтобы изучить его детали.")
            val S23 = SbText("This particular ring is a 1-star item. The main property of this type of ring provides resistance to dark damage. The more stars an item has, the stronger its effects will be.", "Это конкретное кольцо — предмет с 1 звездой. Основным свойством этого типа колец является устойчивость к темным повреждениям. Чем больше звезд у предмета, тем сильнее будет его эффект.")
            val S24 = SbText("Additionally, 1-star items come with one random additional property.", "Кроме того, предметы с 1 звездой имеют одно случайное дополнительное свойство.")
            val S25 = SbText("As you use and upgrade the ring, its properties will become more potent. Each level increases the power of one random additional property. You can continue leveling up the ring until it reaches its maximum level of 5.", "Когда вы используете и улучшаете кольцо, его свойства становятся более мощными. Каждый уровень увеличивает силу одного случайного дополнительного свойства. Вы можете продолжать повышать уровень кольца, пока оно не достигнет максимального уровня 5.")
            val S26 = SbText("If you're satisfied with the ring's attributes, go ahead and equip it by selecting the \"Equip\" button. Now, notice how the ring has been placed in the equipped slot.", "Если вас устраивают характеристики кольца, наденьте его, нажав кнопку «Надеть». Теперь обратите внимание, как кольцо было помещено в экипированную ячейку.")
            val S27 = SbText("Equipping items like this can significantly enhance Valerian's abilities. Keep an eye out for other powerful items and customize your equipment to best suit your playstyle.", "Оснащение подобными предметами может значительно усилить способности Валериана. Следите за другими мощными предметами и настраивайте свое снаряжение в соответствии с вашим стилем игры.")
            val S28 = SbText("You've learned quite a bit about Valerian's abilities and equipment. For now, you can close this window by selecting the \"Close\" button. But remember, there's always more to discover and improve upon in the future. Keep coming back to this window to explore other details and continue enhancing your character.", "Вы довольно много узнали о способностях и снаряжении Валериана. Пока вы можете закрыть это окно, нажав кнопку «Закрыть». Но помните, в будущем всегда есть что открыть и улучшить. Продолжайте возвращаться к этому окну, чтобы изучить другие детали и продолжить улучшать вашего персонажа.")
        }

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
