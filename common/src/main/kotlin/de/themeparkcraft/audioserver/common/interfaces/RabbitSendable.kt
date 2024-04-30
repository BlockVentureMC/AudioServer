package de.themeparkcraft.audioserver.common.interfaces

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

interface RabbitSendable {

    @OptIn(ExperimentalSerializationApi::class)
    fun encode(): ByteArray {
        return ProtoBuf.encodeToByteArray(this)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun decode(data: ByteArray): RabbitSendable {
        return ProtoBuf.decodeFromByteArray(data)
    }

}