package de.themeparkcraft.audioserver.common.data.audiosources

import de.themeparkcraft.audioserver.common.interfaces.AudioSource
import java.util.*
import kotlinx.serialization.Serializable

/**
 * Represents a global park audio source.
 *
 * A global park audio source is an audio source that is associated with a park and can be played at a specific height.
 * The height property determines the height at which the audio source should start playing.
 *
 * @property height The height at which the audio source should start playing.
 * @see GlobalParkAudioSource
 */
@Serializable
data class GlobalParkAudioSource(override val uid: String, override val volume: Float, val height: Double) : AudioSource