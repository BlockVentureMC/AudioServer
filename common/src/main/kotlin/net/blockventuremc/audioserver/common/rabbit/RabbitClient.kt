package net.blockventuremc.audioserver.common.rabbit

import com.rabbitmq.client.*
import net.blockventuremc.audioserver.common.data.RabbitConfiguration
import net.blockventuremc.audioserver.common.extensions.getLogger
import net.blockventuremc.audioserver.common.interfaces.RabbitSendable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.nio.charset.Charset

/**
 * The RabbitClient class represents a client for connecting to a RabbitMQ server.
 *
 * @param rabbitConfiguration The configuration settings for connecting to the RabbitMQ server.
 */
class RabbitClient(rabbitConfiguration: RabbitConfiguration) {

    private val connection: Connection
    private val channel: Channel
    private val queue: String

    init {
        getLogger().info("Connecting to RabbitMQ server at ${rabbitConfiguration.host}:${rabbitConfiguration.port}...")

        // Connect to RabbitMQ server
        val connectionFactory = ConnectionFactory()
        connectionFactory.host = rabbitConfiguration.host
        connectionFactory.port = rabbitConfiguration.port
        connectionFactory.virtualHost = rabbitConfiguration.virtualHost
        connectionFactory.username = rabbitConfiguration.username
        connectionFactory.password = rabbitConfiguration.password
        connectionFactory.isAutomaticRecoveryEnabled = true
        connectionFactory.networkRecoveryInterval = 30_000

        this.connection = connectionFactory.newConnection()
        this.channel = connection.createChannel()

        this.queue = this.channel.queueDeclare(ROUTIING_KEY, false, false, true, null).queue
        this.channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, false, false, null)
        this.channel.queueBind(this.queue, EXCHANGE_NAME, ROUTIING_KEY)

        getLogger().info("Connected to RabbitMQ server.")
    }

    /**
     * Sets a listener for consuming messages from the RabbitMQ queue.
     *
     * @param messageListener The listener for message consumption.
     */
    fun withListener(messageListener: DeliverCallback) {
        this.channel.basicConsume(this.queue, true, messageListener) { consumerTag -> getLogger().error("Consumer $consumerTag cancelled.") }
    }

    /**
     * Sends a message using RabbitMQ.
     *
     * @param rabbitSendable The object to be sent as a message.
     */
    fun sendMessage(rabbitSendable: RabbitSendable) {
        CoroutineScope(Dispatchers.Default).launch {
            val message = rabbitSendable.encode()
            channel.basicPublish(EXCHANGE_NAME, ROUTIING_KEY, null, message)
        }
    }

    /**
     * Decomposes a message represented as a byte array into a RabbitSendable object.
     *
     * @param message The message to decompose represented as a byte array.
     * @return The RabbitSendable object obtained after decomposing the message.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun deserializeRabbitMessage(message: ByteArray): RabbitSendable {
        val messageString = message.toString(Charset.forName("UTF-8"))
        try {
            return ProtoBuf.decodeFromByteArray <RabbitSendable>(message)
        } catch (e: Exception) {
            getLogger().error("Failed to deserialize message: $messageString", e)
            throw e
        }
    }

    /**
     * Disconnects from the RabbitMQ server by closing the channel and the connection.
     */
    fun disconnect() {
        this.channel.close()
        this.connection.close()

        getLogger().info("Disconnecting from RabbitMQ server...")
    }
}