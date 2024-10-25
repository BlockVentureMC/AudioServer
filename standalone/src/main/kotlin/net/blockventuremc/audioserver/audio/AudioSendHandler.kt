package net.blockventuremc.audioserver.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import io.ktor.server.application.ApplicationCall
import io.ktor.server.sse.ServerSSESession
import io.ktor.server.websocket.*
import io.ktor.sse.ServerSentEvent
import io.ktor.util.moveToByteArray
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.nio.ByteBuffer


class AudioSendHandler(private var audioPlayer: AudioPlayer) {

    private var lastFrame: AudioFrame? = null

    /**
     * Checks if the audioPlayer can provide an audio frame.
     *
     * @return true if the audioPlayer can provide an audio frame, false otherwise.
     */
    fun canProvide(): Boolean {
        if (audioPlayer.isPaused || audioPlayer.playingTrack == null) return false
        lastFrame = audioPlayer.provide()
        return (lastFrame != null)
    }

    /**
     * Provides a ByteArray containing 20ms of audio data.
     *
     * @return A ByteArray containing 20ms of audio data.
     */
    fun provide20MsAudio(): ByteArray {
        if (lastFrame == null) return ByteArray(0)
        return lastFrame!!.data
    }

    private var job: Job? = null
    fun addConsumer(sseSession: ServerSSESession) {
        job = CoroutineScope(sseSession.coroutineContext).launch {
            while (true) {
                if (canProvide()) {
                    sseSession.send(ServerSentEvent(provide20MsAudio().toString(Charsets.UTF_8), event = "audio"))
                    delay(20)
                }
            }
        }
    }


    fun removeConsumer(sseSession: ServerSSESession) {
        job?.cancel()
    }

}