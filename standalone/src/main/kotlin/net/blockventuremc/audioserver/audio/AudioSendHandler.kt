package net.blockventuremc.audioserver.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import java.nio.ByteBuffer


class AudioSendHandler(private var audioPlayer: AudioPlayer) {

    private var lastFrame: AudioFrame? = null

    /**
     * Checks if the audioPlayer can provide an audio frame.
     *
     * @return true if the audioPlayer can provide an audio frame, false otherwise.
     */
    fun canProvide(): Boolean {
        lastFrame = audioPlayer.provide()
        return lastFrame != null
    }

    /**
     * Provides a ByteBuffer containing 20ms of audio data.
     *
     * @return A ByteBuffer containing 20ms of audio data.
     */
    fun provide20MsAudio(): ByteBuffer {
        return ByteBuffer.wrap(lastFrame!!.data)
    }

}