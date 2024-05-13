package net.blockventuremc.audioserver.socket

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import net.blockventuremc.audioserver.audio.AudioManager
import net.blockventuremc.audioserver.audio.AudioSendHandler
import net.blockventuremc.audioserver.common.extensions.getLogger
import java.nio.ByteBuffer

object SSEConnectionManager {

    init {
        embeddedServer(Netty, port = 8080, module = Application::startSocket).start()
        getLogger().info("SSE server started on port 8080.")
    }

}

suspend fun ApplicationCall.respondSse(eventFlow: Flow<ByteBuffer>) {
    response.cacheControl(CacheControl.NoCache(null))
    respondBytesWriter(contentType = ContentType.Text.EventStream) {
        eventFlow.collect { event ->
            writeAvailable(event)
            flush()
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun Application.startSocket() {
    install(WebSockets)
    val audioSendHandler = AudioSendHandler(AudioManager.getPlayer())

    val sseFlow = flow {
        while (true) {
            if (audioSendHandler.canProvide()) {
                kotlinx.coroutines.delay(20)
                val audioBytes = audioSendHandler.provide20MsAudio()
                emit(audioBytes)
                continue
            }
            emit(ByteBuffer.wrap("data: Track ended\n\n".toByteArray(Charsets.UTF_8)))
            kotlinx.coroutines.delay(5000)
        }
    }.shareIn(GlobalScope, SharingStarted.Eagerly)

    routing {
        /**
         * Route to be executed when the client perform a GET `/sse` request.
         * It will respond using the [respondSse] extension method defined in this same file
         * that uses the [SharedFlow] to collect sse events.
         */
        webSocket("/audio") {
            getLogger().info("Client connected to audio websocket.")
            audioSendHandler.addConsumer(this)
            try {
                for (frame in incoming) {
                    getLogger().info("Received frame: $frame")
                }
            } catch (e: Exception) {
                getLogger().error("Error while receiving frames: ", e)
            } finally {
                getLogger().info("Client disconnected from audio websocket.")
                audioSendHandler.removeConsumer(this)
            }
        }

        get("/") {
            call.respondText(
                """
                        <html>
                            <head></head>
                            <body>
                                <ul id="events">
                                </ul>
                                <script type="text/javascript">
                                    const audioContext = new AudioContext();
                                    const sampleRate = audioContext.sampleRate;
                                    
                                    const socket = new WebSocket('ws://localhost:8080/audio');
                                    
                                    socket.binaryType = 'arraybuffer';
                                    socket.onopen = (event) => {
                                        console.log('Connected to audio websocket');
                                    };
                                    
                                    socket.onmessage = (event) => {
                                        const data = event.data;
                                        console.log('Received data: ' + data);
                                        // Dekodieren und abspielen der empfangenen Opus-Daten
//                                        const audioBuffer = audioContext.createBuffer(1, 960, sampleRate);
//                                        const audioData = audioBuffer.getChannelData(0);
//                                        const view = new DataView(data);
//                                        for (let i = 0; i < 960; i++) {
//                                            audioData[i] = view.getFloat32(i * 4, true);
//                                        }
//                                        const source = audioContext.createBufferSource();
//                                        source.buffer = audioBuffer;
//                                        source.connect(audioContext.destination);
//                                        source.start();
                                    };
                                    
                                    socket.onclose = (event) => {
                                        console.log('Disconnected from audio websocket');
                                    };
                                </script>
                            </body>
                        </html>
                    """.trimIndent(),
                contentType = ContentType.Text.Html
            )
        }
    }
}

private fun ByteBuffer.display() = array().joinToString(", ") { it.toString() }