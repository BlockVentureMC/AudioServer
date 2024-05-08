package net.blockventuremc.audioserver.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.blockventuremc.audioserver.common.extensions.getLogger

class TrackScheduler : AudioEventAdapter() {

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        getLogger().info("Now playing track: ${track.info.title}")
    }


    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            player.playTrack(track.makeClone())
            getLogger().info("Track ended: ${track.info.title} - Looping track.")
            return
        }
        getLogger().info("Track ended: ${track.info.title} - Reason: $endReason")
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        exception.printStackTrace()
        player.playTrack(track.makeClone())
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        if (thresholdMs > 2500) {
            getLogger().warn("Track stuck ($thresholdMs ms): ${track.info.title} - Restarting track.")
            player.playTrack(track.makeClone())
        }
    }

}