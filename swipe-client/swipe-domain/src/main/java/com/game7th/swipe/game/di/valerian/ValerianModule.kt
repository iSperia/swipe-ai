package com.game7th.swipe.game.di.valerian

import com.game7th.swipe.battle.CharacterAttributes
import com.game7th.swipe.battle.intAttribute
import com.game7th.swipe.game.CommonKeys
import com.game7th.swipe.game.SbBalanceProvider
import com.game7th.swipe.game.SbCharacter
import com.game7th.swipe.game.SbEffect
import com.game7th.swipe.game.di.SbCharacterFactory
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
class ValerianModule {
    @Provides @IntoMap
    @StringKey("CHARACTER_VALERIAN")
    fun provideCharacterBuilder(): SbCharacterFactory {
        return object : SbCharacterFactory {
            override fun createCharacter(balance: SbBalanceProvider): SbCharacter {
                val config = balance.getMonster(SKIN)
                val generators = config.tiles.mapIndexed { index, tileConfig ->
                    SbEffect(
                        id = index,
                        mapOf(CommonKeys.Generator.GENERATOR to tileConfig)
                    )
                }
                return SbCharacter(
                    id = 0,
                    skin = SKIN,
                    human = true,
                    health = config.balance.intAttribute("base_health"),
                    maxHealth = config.balance.intAttribute("base_health"),
                    ultimateProgress = 0,
                    maxUltimateProgress = 1000,
                    team = 0,
                    attributes = CharacterAttributes.ZERO,
                    maxTileId = 0,
                    tiles = emptyList(),
                    maxEffectId = generators.size,
                    effects = generators
                )
            }
        }
    }
}

private val SKIN = "CHARACTER_VALERIAN"
