package net.blockventuremc.audioserver.socket

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
        embeddedServer(Netty, port = 8080, module = Application::startSSE).start()
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
private fun Application.startSSE() {
    val audioSendHandler = AudioSendHandler(AudioManager.getPlayer())

    val sseFlow = flow {
        while (true) {
            if (audioSendHandler.canProvide()) {
                emit(audioSendHandler.provide20MsAudio())
            }
        }
    }.shareIn(GlobalScope, SharingStarted.Eagerly)

    routing {
        /**
         * Route to be executed when the client perform a GET `/sse` request.
         * It will respond using the [respondSse] extension method defined in this same file
         * that uses the [SharedFlow] to collect sse events.
         */
        get("/sse") {
            call.respondSse(sseFlow)
        }
    }
}