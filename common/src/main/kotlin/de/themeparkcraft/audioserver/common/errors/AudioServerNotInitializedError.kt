package de.themeparkcraft.audioserver.common.errors

/**
 * This class represents an error that occurs when attempting to use the AudioServer without initializing it first.
 *
 * @constructor Creates a new instance of the AudioServerNotInitializedError class with the default error message.
 * @param message The error message associated with this exception.
 */
class AudioServerNotInitializedError : Error("AudioServer has not been initialized. Please call `AudioServer.connect(rabbitConfiguration)` before using any other methods.")