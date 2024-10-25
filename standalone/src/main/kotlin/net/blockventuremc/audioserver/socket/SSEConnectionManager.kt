package net.blockventuremc.audioserver.socket

import dev.onvoid.webrtc.CreateSessionDescriptionObserver
import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCIceServer
import dev.onvoid.webrtc.RTCSdpType
import dev.onvoid.webrtc.RTCSessionDescription
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.ktor.server.websocket.*
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import net.blockventuremc.audioserver.audio.AudioManager
import net.blockventuremc.audioserver.audio.AudioSendHandler
import net.blockventuremc.audioserver.common.extensions.getLogger
import net.blockventuremc.audioserver.rtc.PeerConnectionRequest
import java.nio.ByteBuffer

object SSEConnectionManager {

    init {
        embeddedServer(Netty, port = 8080, module = Application::startSocket).start()
        getLogger().info("Socket server started on port 8080.")
    }

}

@OptIn(DelicateCoroutinesApi::class)
private fun Application.startSocket() {
    serverConfig {
        developmentMode = true
        getLogger().info("Development mode: $developmentMode")
    }
    install(WebSockets)
    install(SSE)
    install(ContentNegotiation) {
        json()
    }

    val audioSendHandler = AudioSendHandler(AudioManager.getPlayer())
    val factory = PeerConnectionFactory()
    val iceServer = RTCIceServer()
    iceServer.urls = listOf("stun:stun.l.google.com:19302", "stun:stun1.l.google.com:19302")

    val config = RTCConfiguration()
    config.iceServers = listOf(iceServer)

    routing {
        staticResources("/scripts", "scripts")
        staticResources("/", "web")

        put("/offer") {
            getLogger().info("Received offer")
            val offer = call.receive<PeerConnectionRequest>()
            getLogger().info("Opening offer: ${offer.type}")
            val rtcSessionDescription = RTCSessionDescription(RTCSdpType.OFFER, offer.sdp)

            val peerConnection = factory.createPeerConnection(config) {}
            peerConnection.setRemoteDescription(rtcSessionDescription, null)
            getLogger().info("Set remote description - creating answer")


            peerConnection.createAnswer(null, object : CreateSessionDescriptionObserver {
                override fun onSuccess(answer: RTCSessionDescription) {
                    peerConnection.setLocalDescription(answer, null)
                    getLogger().info("Created answer: $answer")

                    CoroutineScope(call.coroutineContext).launch {
                        val answerRequest = PeerConnectionRequest(sdp = answer.sdp, type = "answer")

                        call.respond<PeerConnectionRequest>(answerRequest)
                        getLogger().info("Responded with answer: $answer")
                    }
                }

                override fun onFailure(error: String?) {
                    getLogger().error("Failed to create answer: $error")
                }
            })
            getLogger().info("PUT /offer done")
        }

        sse("/audio") {
            getLogger().info("Client connected to audio sse.")
            audioSendHandler.addConsumer(this)
        }

    }
}

private fun ByteBuffer.display() = array().joinToString(", ") { it.toString() }