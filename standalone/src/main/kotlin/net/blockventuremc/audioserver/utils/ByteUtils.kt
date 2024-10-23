package net.blockventuremc.audioserver.utils

import java.nio.ByteBuffer


/**
 * Returns a 16-bit signed integer in big-endian byte order from the specified byte array
 * starting at the specified offset.
 *
 * @param arr the byte array from which the short value is to be retrieved
 * @param offset the index in the byte array from which the short value starts
 * @return the 16-bit signed integer in big-endian byte order
 */
fun getShortBigEndian(arr: ByteArray, offset: Int): Short {
    return ((arr[offset].toInt() and 0xff) shl 8 or (arr[offset + 1].toInt() and 0xff)).toShort()
}

/**
 * Convert a two-byte little endian value from a byte array to a short.
 *
 * @param arr the byte array containing the little endian value.
 * @param offset the starting index of the little endian value in the byte array.
 * @return the short value converted from the little endian byte array.
 */
fun getShortLittleEndian(arr: ByteArray, offset: Int): Short {
    // Same as big endian but reversed order of bytes (java uses big endian)
    return ((arr[offset].toInt() and 0xff)
            or ((arr[offset + 1].toInt() and 0xff) shl 8)).toShort()
}

/**
 * Retrieves a big-endian integer value from the given byte array.
 *
 * @param arr    the byte array from which to retrieve the integer value
 * @param offset the offset in the byte array at which the integer starts
 * @return the big-endian integer value
 */
fun getIntBigEndian(arr: ByteArray, offset: Int): Int {
    return arr[offset + 3].toInt() and 0xFF or ((arr[offset + 2].toInt() and 0xFF) shl 8
            ) or ((arr[offset + 1].toInt() and 0xFF) shl 16
            ) or ((arr[offset].toInt() and 0xFF) shl 24)
}

/**
 * Sets the specified integer in big-endian format to the given byte array at the specified offset.
 *
 * @param arr the byte array to modify
 * @param offset the offset at which to set the integer
 * @param it the integer value to set
 */
fun setIntBigEndian(arr: ByteArray, offset: Int, it: Int) {
    arr[offset] = ((it ushr 24) and 0xFF).toByte()
    arr[offset + 1] = ((it ushr 16) and 0xFF).toByte()
    arr[offset + 2] = ((it ushr 8) and 0xFF).toByte()
    arr[offset + 3] = (it and 0xFF).toByte()
}

/**
 * Reallocates a ByteBuffer with specified length.
 *
 * @param original  the original ByteBuffer to be reallocated
 * @param length  the new length for the reallocated ByteBuffer
 * @return  the reallocated ByteBuffer
 */
fun reallocate(original: ByteBuffer?, length: Int): ByteBuffer {
    val buffer: ByteBuffer = ByteBuffer.allocate(length)
    buffer.put(original)
    return buffer
}