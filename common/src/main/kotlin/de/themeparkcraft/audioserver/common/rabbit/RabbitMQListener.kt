package de.themeparkcraft.audioserver.common.rabbit

import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery

class RabbitMQListener: DeliverCallback {


    override fun handle(message: String, delivery: Delivery) {
        TODO()
    }


}