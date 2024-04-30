package de.themeparkcraft.audioserver

import de.themeparkcraft.audioserver.common.data.RabbitConfiguration
import de.themeparkcraft.audioserver.common.extensions.getLogger
import de.themeparkcraft.audioserver.common.rabbit.RabbitClient
import de.themeparkcraft.audioserver.utils.Environment
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
                getLogger().info("Received message: $message")
            }

            getLogger().info("AudioServer standalone started.")
        } catch (exception: Exception) {
            getLogger().error("Failed to start AudioServer standalone due to the following exception: ", exception)
            exitProcess(1)
        }
    }

}