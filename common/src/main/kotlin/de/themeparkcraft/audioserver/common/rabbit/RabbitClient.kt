package de.themeparkcraft.audioserver.common.rabbit

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import de.themeparkcraft.audioserver.common.data.RabbitConfiguration
import de.themeparkcraft.audioserver.common.extensions.getLogger
import de.themeparkcraft.audioserver.common.interfaces.RabbitSendable
import kotlinx.serialization.protobuf.ProtoBuf

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

        this.queue = this.channel.queueDeclare(ROUTIING_KEY, false, true, true, null).queue
        this.channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, false, true, null)
        this.channel.queueBind(this.queue, EXCHANGE_NAME, ROUTIING_KEY)

        getLogger().info("Connected to RabbitMQ server.")
    }

    /**
     * Sets a listener for consuming messages from the RabbitMQ queue.
     *
     * @param messageListener The listener for message consumption.
     */
    fun withListener(messageListener: RabbitMQListener) {
        this.channel.basicConsume(this.queue, true, messageListener) { consumerTag -> getLogger().error("Consumer $consumerTag cancelled.") }
    }

    /**
     * Sends a message using RabbitMQ.
     *
     * @param rabbitSendable The object to be sent as a message.
     */
    fun sendMessage(rabbitSendable: RabbitSendable) {
        val message = rabbitSendable.encode()
        this.channel.basicPublish(EXCHANGE_NAME, ROUTIING_KEY, null, message)
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