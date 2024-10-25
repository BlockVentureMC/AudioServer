package net.blockventuremc.audioserver.socket

import dev.onvoid.webrtc.CreateSessionDescriptionObserver
import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.RTCAnswerOptions
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCIceServer
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.SetSessionDescriptionObserver
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import net.blockventuremc.audioserver.audio.AudioManager
import net.blockventuremc.audioserver.audio.AudioSendHandler
import net.blockventuremc.audioserver.common.extensions.getLogger
import java.nio.ByteBuffer

object SSEConnectionManager {

    init {
        embeddedServer(Netty, port = 8080, module = Application::startSocket).start()
        getLogger().info("Socket server started on port 8080.")
    }

}

@OptIn(DelicateCoroutinesApi::class)
private fun Application.startSocket() {
    install(WebSockets)
    install(SSE)

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
            val offer = call.receive<RTCSessionDescription>()

            val peerConnection = factory.createPeerConnection(config) {}
            peerConnection.setRemoteDescription(offer, null)


            peerConnection.createAnswer(null, object : CreateSessionDescriptionObserver {
                override fun onSuccess(answer: RTCSessionDescription) {
                    peerConnection.setLocalDescription(answer, null)

                    CoroutineScope(call.coroutineContext).launch {
                        call.respond<RTCSessionDescription>(answer)
                    }
                }

                override fun onFailure(error: String?) {
                    getLogger().error("Failed to create answer: $error")
                }
            })
        }

        sse("/audio") {
            getLogger().info("Client connected to audio sse.")
            audioSendHandler.addConsumer(this)
        }

    }
}

private fun ByteBuffer.display() = array().joinToString(", ") { it.toString() }