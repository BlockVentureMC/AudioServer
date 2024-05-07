package net.blockventuremc.audioserver.common.interfaces

import kotlinx.serialization.Serializable

/**
 * The AudioSource interface represents an audio source. It provides access to the unique identifier (uid)
 * and volume of the audio source.
 */
@Serializable
sealed interface AudioSource : RabbitSendable {
    val uid: String
    val server: String
    val volume: Float
}