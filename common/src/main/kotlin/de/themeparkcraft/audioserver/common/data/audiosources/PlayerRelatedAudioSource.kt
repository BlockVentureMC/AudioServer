package de.themeparkcraft.audioserver.common.data.audiosources

import de.themeparkcraft.audioserver.common.interfaces.AudioSource
import kotlinx.serialization.Serializable

/**
 * Represents an audio source associated with a player.
 *
 * This interface extends the [AudioSource] interface and adds a property [playerUUID] to uniquely identify the player.
 *
 * This is a global sound source not positioned in the world.
 * Use this for player-related sound effects.
 */
@Serializable
data class PlayerRelatedAudioSource(override val uid: String, override val volume: Float, val playerUUID: String) :
    AudioSource