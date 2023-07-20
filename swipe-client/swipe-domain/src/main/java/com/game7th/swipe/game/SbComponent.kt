package com.game7th.swipe.game

import com.game7th.swipe.game.characters.ThornstalkerModule
import com.game7th.swipe.game.characters.ValerianModule
import com.google.gson.JsonObject
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import javax.inject.Singleton

interface SbCharacterFactory {
    fun createCharacter(balance: SbBalanceProvider): SbCharacter
}

@Singleton
@Component(modules = [
    SkillsModule::class
])
interface SbComponent {

    fun provideTriggers(): Map<String, SbTrigger>

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun balances(balances: Map<String, JsonObject>): Builder
        fun build(): SbComponent
    }
}

@Module(includes = [
    ValerianModule::class,
    ThornstalkerModule::class
])
class SkillsModule {
    @Provides
    @IntoMap
    @StringKey("common.poison")
    fun providePoison(): SbTrigger = { context, event ->
        context.useOnComplete(event, "COMMON_POISON") { characterId, tileId, lucky ->
            val character = game.character(characterId) ?: return@useOnComplete
            game = game.withUpdatedCharacter(character.removeEffects("COMMON_POISON"))
        }
        context.onEndOfTurn(event) { characterId ->
            val character = game.character(characterId) ?: return@onEndOfTurn
            val damage = character.effects.filter { it.skin == "COMMON_POISON" }.sumOf { it.data[CommonKeys.Poison.POISON] as Int }
            if (damage > 0) {
                dealDamage(null, characterId, SbElemental(dark = damage.toFloat()))
                events.add(SbDisplayEvent.SbShowPopup(
                    characterId = characterId,
                    text = "Poison",
                    icons = emptyList()
                ))
            }
        }
    }
}
