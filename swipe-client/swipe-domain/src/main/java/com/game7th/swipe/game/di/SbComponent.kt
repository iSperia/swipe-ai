package com.game7th.swipe.game.di

import com.game7th.swipe.game.SbBalanceProvider
import com.game7th.swipe.game.SbCharacter
import com.game7th.swipe.game.di.valerian.ValerianModule
import dagger.Component
import dagger.Module
import javax.inject.Singleton

interface SbCharacterFactory {
    fun createCharacter(balance: SbBalanceProvider): SbCharacter
}

@Singleton
@Component(modules = [
    CharactersModule::class
])
interface SbComponent {
    fun characterFactories(): Map<String, SbCharacterFactory>
}

@Module(includes = [
    ValerianModule::class
])
class CharactersModule {

}
