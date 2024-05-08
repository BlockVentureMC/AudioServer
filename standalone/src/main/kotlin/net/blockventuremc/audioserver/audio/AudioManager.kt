package net.blockventuremc.audioserver.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.blockventuremc.audioserver.common.extensions.getLogger

object AudioManager {

    private val playerManager = DefaultAudioPlayerManager()
    private val player: AudioPlayer

    init {
        AudioSourceManagers.registerLocalSource(playerManager)

        player = playerManager.createPlayer()
        player.addListener(TrackScheduler())
    }

    /**
     * Loads a track from a file.
     *
     * @param filePath the path to the file
     */
    fun loadTrackFromFile(filePath: String) {
        playerManager.loadItem(filePath, AudioLoadResultListener(player))
        getLogger().info("Loaded track from file: $filePath")
    }
}