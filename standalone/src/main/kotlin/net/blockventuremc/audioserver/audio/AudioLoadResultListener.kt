package net.blockventuremc.audioserver.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class AudioLoadResultListener(private val player: AudioPlayer) : AudioLoadResultHandler {
    override fun trackLoaded(audioTrack: AudioTrack) {
        player.playTrack(audioTrack)
    }

    override fun playlistLoaded(audioTrack: AudioPlaylist) {
        player.playTrack(audioTrack.tracks.first())
    }

    override fun noMatches() {
        // Do nothing
    }

    override fun loadFailed(exception: FriendlyException) {
        exception.printStackTrace()
    }
}