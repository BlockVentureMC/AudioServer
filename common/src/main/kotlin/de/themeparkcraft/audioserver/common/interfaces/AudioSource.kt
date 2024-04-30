package de.themeparkcraft.audioserver.common.interfaces

/**
 * The AudioSource interface represents an audio source. It provides access to the unique identifier (uid)
 * and volume of the audio source.
 */
interface AudioSource : RabbitSendable {
    val uid: String
    val volume: Float
}