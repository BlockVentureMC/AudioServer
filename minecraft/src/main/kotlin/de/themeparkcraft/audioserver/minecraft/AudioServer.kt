package de.themeparkcraft.audioserver.minecraft

import de.themeparkcraft.audioserver.common.interfaces.PlayerPositionUpdate
import de.themeparkcraft.audioserver.common.data.RabbitConfiguration
import de.themeparkcraft.audioserver.common.errors.AudioServerNotInitializedError
import de.themeparkcraft.audioserver.common.interfaces.AudioSource
import de.themeparkcraft.audioserver.common.rabbit.RabbitClient
import de.themeparkcraft.audioserver.minecraft.extensions.worldPosition
import org.bukkit.entity.Player
import java.util.UUID

/**
 * The AudioServer class is responsible for sending audio sources and player updates to the audio server.
 */
object AudioServer {

    private lateinit var rabbitClient: RabbitClient
    private var isInitialized = false

    /**
     * Establishes a connection to RabbitMQ server using the provided RabbitConfiguration.
     *
     * @param rabbitConfiguration The configuration settings for connecting to the RabbitMQ server.
     */
    fun connect(rabbitConfiguration: RabbitConfiguration) {
        rabbitClient = RabbitClient(rabbitConfiguration)
        isInitialized = true
    }

    /**
     * Disconnects from the RabbitMQ server by closing the channel and the connection.
     */
    fun disconnect() {
        rabbitClient.disconnect()
        isInitialized = false
    }


    /**
     * Sends the audio source to the audio server.
     *
     * @param audioSource The audio source to be sent.
     */
    fun sendAudioSource(audioSource: AudioSource) {
        if (!isInitialized) {
            throw AudioServerNotInitializedError()
        }

        rabbitClient.sendMessage(audioSource)
    }

    /**
     * Stops the audio source associated with the given unique identifier.
     *
     * @param uid The unique identifier of the audio source to stop.
     */
    fun stopAudioSource(uid: UUID) {
        if (!isInitialized) {
            throw AudioServerNotInitializedError()
        }

        TODO()
    }

    /**
     * Sends a player update to the server.
     *
     * @param player the player object containing the updated information
     */
    fun sendPlayerUpdate(player: Player) {
        if (!isInitialized) {
            throw AudioServerNotInitializedError()
        }

        val worldPosition = player.worldPosition
        val positionUpdate = PlayerPositionUpdate(player.uniqueId.toString(), worldPosition)

        rabbitClient.sendMessage(positionUpdate)
    }

}