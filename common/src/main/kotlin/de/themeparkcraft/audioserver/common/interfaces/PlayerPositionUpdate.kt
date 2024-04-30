package de.themeparkcraft.audioserver.common.interfaces

import kotlinx.serialization.Serializable

/**
 * Represents an update of a player's position in the game.
 *
 * This class is used to send player position updates over RabbitMQ.
 *
 * @param playerUid The unique identifier of the player.
 * @param position The world position of the player.
 *
 * @see WorldPosition
 * @see RabbitSendable
 */
@Serializable
data class PlayerPositionUpdate(
    val playerUid: String,
    val position: WorldPosition
) : RabbitSendable
