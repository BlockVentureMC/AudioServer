package net.blockventuremc.audioserver.common.interfaces

import kotlinx.serialization.Serializable

/**
 * Represents an area audio source in a world.
 *
 * An area audio source is defined by two positions in a world, represented by instances of the [WorldPosition] class.
 * The [pos1] position represents the starting point of the area audio source, and the [pos2] position represents the ending point.
 * The area audio source is essentially a rectangular area in the world.
 */
@Serializable
data class AreaAudioSource(override val uid: String, override val volume: Float, val pos1: WorldPosition, val pos2: WorldPosition,
                           override val server: String
) :
    AudioSource