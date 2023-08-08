package com.game7th.swipe.game.characters

import com.game7th.swipe.game.*

fun provideDefaultTriggers(): Map<String, SbTrigger> = mapOf(

    /**Poison*/
    "common.poison" to { context, event ->
        context.useOnComplete(event, "COMMON_POISON") { characterId, tileId, _ ->
            val character = game.character(characterId) ?: return@useOnComplete
            game = game.withUpdatedCharacter(character.removeEffects("COMMON_POISON"))
        }
        context.onEndOfTurn(event) { characterId ->
            val character = game.character(characterId) ?: return@onEndOfTurn
            val damage = character.effects.filter { it.skin == "COMMON_POISON" }
                .sumOf { it.data[CommonKeys.Poison.POISON] as Int }
            if (damage > 0) {
                dealDamage(null, characterId, SbElemental(dark = damage.toFloat()))
                events.add(
                    SbDisplayEvent.SbShowPopup(
                        characterId = characterId,
                        text = "Poison",
                        icons = emptyList(),
                        sound = SbSoundType.HISS_POISON
                    )
                )
            }
        }
    },

    "common.stun" to { context, event ->
        context.useOnComplete(event, "COMMON_STUN") { characterId, tileId, _ -> }
    },

    /**Weakness*/
    "common.weakness" to { context, event ->
        context.triggerBackgroundLayerOnComplete(event, "COMMON_WEAKNESS") { characterId, tileId ->
            val character = game.character(characterId) ?: return@triggerBackgroundLayerOnComplete
            character.effects.firstOrNull { it.skin == "COMMON_WEAKNESS" }?.let { effect ->
                game = game.withUpdatedCharacter(character.withRemovedEffect(effect.id))
            }
        }
    }
)
