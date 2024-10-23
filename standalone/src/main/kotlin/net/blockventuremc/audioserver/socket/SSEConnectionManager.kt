package net.blockventuremc.audioserver.socket

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

    routing {
        staticResources("/scripts", "scripts")

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
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Audio Streaming mit Howler.js</title>
        </head>
        <body>
            <!-- Einbinden von Howler.js -->
            <script src="/scripts/howler.core.min.js"></script>
            <script type="text/javascript">
                var sound = new Howl({
                    src: ['ws://localhost:8080/audio'],
                    format: ['opus'],
                    html5: true,
                    autoplay: true,
                    onload: function() {
                        console.log('Audio-Stream geladen');
                    },
                    onloaderror: function(id, error) {
                        console.error('Fehler beim Laden des Audio-Streams:', error);
                    },
                    onplayerror: function(id, error) {
                        console.error('Fehler beim Abspielen des Audio-Streams:', error);
                    },
                    onplay: function(id) {
                        console.log('Audio-Stream wird abgespielt');
                    }
                });
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