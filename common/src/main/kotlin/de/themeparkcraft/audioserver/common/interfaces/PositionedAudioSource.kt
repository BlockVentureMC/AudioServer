package de.themeparkcraft.audioserver.common.interfaces

import kotlinx.serialization.Serializable

/**
 * An interface representing a positioned audio source.
 *
 * This interface defines the properties of a positioned audio source, including its coordinates and the world it belongs to.
 */
@Serializable
data class PositionedAudioSource(override val uid: String, override val volume: Float, val position: WorldPosition) :
    AudioSource