package net.blockventuremc.audioserver

import net.blockventuremc.audioserver.audio.AudioManager
import net.blockventuremc.audioserver.cache.AudioSourceCache
import net.blockventuremc.audioserver.cache.PlayerCache
import net.blockventuremc.audioserver.common.data.RabbitConfiguration
import net.blockventuremc.audioserver.common.extensions.getLogger
import net.blockventuremc.audioserver.common.interfaces.AudioSource
import net.blockventuremc.audioserver.common.interfaces.PlayerPositionUpdate
import net.blockventuremc.audioserver.common.rabbit.RabbitClient
import net.blockventuremc.audioserver.utils.Environment
import java.io.File
import kotlin.system.exitProcess

class AudioServerStandalone {

    private lateinit var rabbitClient: RabbitClient

    init {
        getLogger().info("Starting AudioServer standalone...")

        val rabbitConfiguration = RabbitConfiguration(
            host = Environment.getEnv("RABBITMQ_HOST") ?: "localhost",
            port = Environment.getEnv("RABBITMQ_PORT")?.toInt() ?: 5672,
            virtualHost = Environment.getEnv("RABBITMQ_VIRTUAL_HOST") ?: "/",
            username = Environment.getEnv("RABBITMQ_USER") ?: "guest",
            password = Environment.getEnv("RABBITMQ_PASSWORD") ?: "guest"
        )

        getLogger().info("Connecting to RabbitMQ at ${rabbitConfiguration.host}:${rabbitConfiguration.port} - (vhost: ${rabbitConfiguration.virtualHost}) with user ${rabbitConfiguration.username}...")

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

        // Listen for user inputs to stop the server or execute a command
        while (true) {
            val input = readlnOrNull()
            when {
                input == null -> continue
                input == "stop" -> {
                    getLogger().info("Stopping AudioServer standalone...")
                    rabbitClient.disconnect()
                    exitProcess(0)
                }
                input.startsWith("play ") -> {
                    val mediaLocation = input.substring(5)
                    val mediaFile = File(mediaLocation)
                    if (!mediaFile.exists()) {
                        getLogger().info("File not found: $mediaLocation")
                        continue
                    }

                    AudioManager.loadTrackFromFile(mediaLocation)
                }
                else -> getLogger().info("Unknown command: $input")
            }

        }
    }
}