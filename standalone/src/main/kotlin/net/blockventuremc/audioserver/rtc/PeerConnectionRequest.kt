package net.blockventuremc.audioserver.rtc


import kotlinx.serialization.Serializable

@Serializable
data class PeerConnectionRequest(
    val sdp: String? = null,
    val type: String? = null
)