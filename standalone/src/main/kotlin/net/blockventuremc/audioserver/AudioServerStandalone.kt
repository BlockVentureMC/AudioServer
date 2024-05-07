package net.blockventuremc.audioserver

import net.blockventuremc.audioserver.cache.AudioSourceCache
import net.blockventuremc.audioserver.cache.PlayerCache
import net.blockventuremc.audioserver.common.data.RabbitConfiguration
import net.blockventuremc.audioserver.common.extensions.getLogger
import net.blockventuremc.audioserver.common.interfaces.AudioSource
import net.blockventuremc.audioserver.common.interfaces.PlayerPositionUpdate
import net.blockventuremc.audioserver.common.rabbit.RabbitClient
import net.blockventuremc.audioserver.utils.Environment
import kotlin.system.exitProcess

class AudioServerStandalone {

    private val rabbitClient: RabbitClient

    init {
        val rabbitConfiguration = RabbitConfiguration(
            host = Environment.getEnv("RABBITMQ_HOST") ?: "localhost",
            port = Environment.getEnv("RABBITMQ_PORT")?.toInt() ?: 5672,
            virtualHost = Environment.getEnv("RABBITMQ_VIRTUAL_HOST") ?: "/",
            username = Environment.getEnv("RABBITMQ_USER") ?: "guest",
            password = Environment.getEnv("RABBITMQ_PASSWORD") ?: "guest"
        )

        getLogger().info("Starting AudioServer standalone at ${rabbitConfiguration.host}:${rabbitConfiguration.port} - (vhost: ${rabbitConfiguration.virtualHost}) with user ${rabbitConfiguration.username}...")

        try {
            rabbitClient = RabbitClient(rabbitConfiguration)

            rabbitClient.withListener { message, delivery ->
                when (val rabbitSendable = rabbitClient.deserializeRabbitMessage(delivery.body)) {
                    is PlayerPositionUpdate -> {
                        PlayerCache.put(rabbitSendable.playerUid, rabbitSendable.position)
                    }
                    is AudioSource -> {
                        AudioSourceCache.put(rabbitSendable)
                    }
                    else -> {
                        getLogger().warn("Received unknown RabbitSendable message: $rabbitSendable")
                    }
                }
            }

            getLogger().info("AudioServer standalone started.")
        } catch (exception: Exception) {
            getLogger().error("Failed to start AudioServer standalone due to the following exception: ", exception)
            exitProcess(1)
        }
    }

}