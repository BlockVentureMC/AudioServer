package net.blockventuremc.audioserver.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
     * Provides a ByteBuffer containing 20ms of audio data.
     *
     * @return A ByteBuffer containing 20ms of audio data.
     */
    fun provide20MsAudio(): ByteBuffer {
        if (lastFrame == null) return ByteBuffer.wrap(ByteArray(0))
        return ByteBuffer.wrap(lastFrame!!.data)
    }

    private var job: Job? = null
    fun addConsumer(defaultWebSocketServerSession: DefaultWebSocketServerSession) {
        job = CoroutineScope(defaultWebSocketServerSession.coroutineContext).launch {
            while (true) {
                if (canProvide()) {
                    defaultWebSocketServerSession.send(Frame.Binary(true, provide20MsAudio()))
                    delay(20)
                }
            }
        }
    }

    fun removeConsumer(defaultWebSocketServerSession: DefaultWebSocketServerSession) {
        job?.cancel()
    }

}