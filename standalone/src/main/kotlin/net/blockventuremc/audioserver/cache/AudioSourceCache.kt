package net.blockventuremc.audioserver.cache

import net.blockventuremc.audioserver.common.interfaces.AudioSource
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object AudioSourceCache {

    /**
     * Mutable variable that represents a cache of audio sources.
     *
     * The cache is implemented as a mutable map where the keys are strings representing the unique identifiers (uid) of the
     * audio sources and the values are instances of the `AudioSource` interface. This allows for fast retrieval of audio sources
     * using their unique identifiers.
     *
     * The cache is represented by a mutable map, meaning that its contents can be modified. New audio sources can be added to
     * the cache or existing ones can be updated or removed.
     *
     * It is important to note that the `AudioSource` interface represents an audio source and provides access to its unique
     * identifier (uid), the server it is associated with, and its volume. The `AudioSource` interface implements the `RabbitSendable`
     * interface, which provides the ability to encode the audio source into a byte array representation using the ProtoBuf encoding.
     *
     * @see AudioSource
     * @see RabbitSendable
     *
     * @property cache The mutable map used to store the audio sources in the cache.
     */
    private var cache: MutableMap<String, AudioSource> = mutableMapOf()

    /**
     * A variable representing the lock used for thread synchronization in a cache.
     * It uses a ReadWriteLock implementation to allow multiple threads to read data concurrently,
     * while ensuring that only one thread can write to the cache at a time.
     */
    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()


    /**
     * Retrieves the audio source associated with the given unique identifier (uid).
     *
     * This function retrieves the audio source from the cache using the provided unique identifier (uid).
     * It acquires a read lock on the cache to allow multiple threads to read the data concurrently.
     * If the audio source is found in the cache, it is returned. Otherwise, the function returns null.
     *
     * @param uid The unique identifier of the audio source to retrieve.
     * @return The audio source associated with the provided unique identifier, or null if not found.
     */
    fun get(uid: String): AudioSource? {
        cacheLock.readLock().lock()
        try {
            return cache[uid]
        } finally {
            cacheLock.readLock().unlock()
        }
    }

    /**
     * Updates the cache with the provided audio source.
     *
     * This function updates the cache with the provided audio source. It acquires a write lock on the cache
     * to ensure that only one thread can write to the cache at a time. The audio source is added to the cache
     * using its unique identifier as the key.
     *
     * @param audioSource The audio source to be added or updated in the cache.
     */
    fun put(audioSource: AudioSource) {
        cacheLock.writeLock().lock()
        try {
            cache[audioSource.uid] = audioSource
        } finally {
            cacheLock.writeLock().unlock()
        }
    }

    /**
     * Removes the audio source with the specified unique identifier from the cache.
     *
     * This function removes the audio source with the specified unique identifier from the cache.
     * It acquires a write lock on the cache to ensure that only one thread can write to the cache at a time.
     * The audio source is removed from the cache using its unique identifier as the key.
     *
     * @param uid The unique identifier of the audio source to remove from the cache.
     */
    fun remove(uid: String) {
        cacheLock.writeLock().lock()
        try {
            cache.remove(uid)
        } finally {
            cacheLock.writeLock().unlock()
        }
    }
}