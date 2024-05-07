package net.blockventuremc.audioserver.common.interfaces

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@Serializable
sealed interface RabbitSendable {

    @OptIn(ExperimentalSerializationApi::class)
    fun encode(): ByteArray {
        return ProtoBuf.encodeToByteArray(this)
    }

}