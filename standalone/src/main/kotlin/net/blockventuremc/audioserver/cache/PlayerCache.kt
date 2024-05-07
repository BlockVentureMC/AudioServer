package net.blockventuremc.audioserver.cache

import net.blockventuremc.audioserver.common.interfaces.PointedWorldPosition
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object PlayerCache {

    /**
     * Represents a cache that stores [PointedWorldPosition] objects associated with player UIDs.
     *
     * @property cache The mutable map that stores player UIDs as keys and corresponding [PointedWorldPosition] objects as values.
     */
    private var cache: MutableMap<String, PointedWorldPosition> = mutableMapOf()
    /**
     * Represents a lock used for controlling access to a cache.
     * The lock allows multiple reads or a single write to the cache at a time.
     * This lock is used to ensure thread-safety when accessing the cache.
     */
    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()

    /**
     * Retrieves the [PointedWorldPosition] associated with the given player UID.
     *
     * @param playerUid The UID of the player.
     * @return The [PointedWorldPosition] object associated with the player UID, or null if not found.
     */
    fun get(playerUid: String): PointedWorldPosition? {
        cacheLock.readLock().lock()
        try {
            return cache[playerUid]
        } finally {
            cacheLock.readLock().unlock()
        }
    }

    /**
     * Updates the cache with the provided player UID and pointed world position.
     *
     * @param playerUid The unique identifier of the player.
     * @param position The pointed world position to be associated with the player.
     */
    fun put(playerUid: String, position: PointedWorldPosition) {
        cacheLock.writeLock().lock()
        try {
            cache[playerUid] = position
        } finally {
            cacheLock.writeLock().unlock()
        }
    }

    /**
     * Removes the player with the specified unique identifier from the cache.
     *
     * @param playerUid the unique identifier of the player to remove from the cache
     */
    fun remove(playerUid: String) {
        cacheLock.writeLock().lock()
        try {
            cache.remove(playerUid)
        } finally {
            cacheLock.writeLock().unlock()
        }
    }
}