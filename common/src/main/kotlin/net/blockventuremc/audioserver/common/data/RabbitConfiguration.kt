package net.blockventuremc.audioserver.common.data

/**
 * Represents the configuration settings for connecting to a RabbitMQ server.
 *
 * @property host The hostname of the RabbitMQ server. Default value is "localhost".
 * @property port The port number to connect to. Default value is 5672.
 * @property virtualHost The virtual host to connect to. Default value is "/".
 * @property username The username to use for authentication. Default value is "guest".
 * @property password The password to use for authentication. Default value is "guest".
 */
data class RabbitConfiguration(
    val host: String = "localhost", // Default value
    val port: Int = 5672, // Default value
    val virtualHost: String = "/", // Default value
    val username: String = "guest", // Default value
    val password: String = "guest" // Default value
)
