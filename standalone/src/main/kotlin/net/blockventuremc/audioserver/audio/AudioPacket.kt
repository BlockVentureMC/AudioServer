package net.blockventuremc.audioserver.audio

import com.iwebpp.crypto.TweetNaclFast.SecretBox
import net.blockventuremc.audioserver.common.extensions.getLogger
import net.blockventuremc.audioserver.utils.getShortBigEndian

import java.net.DatagramPacket
import java.nio.Buffer
import java.nio.ByteBuffer


/**
 * Represents the contents of a audio packet that will be sent to our webclient.
 *
 * @see [RFC 3350 - RTP: A Transport Protocol for Real-Time Applications](https://tools.ietf.org/html/rfc3550)
 */
open class AudioPacket {
    private val type: Byte
    val sequence: Char
    val timestamp: Int
    val sSRC: Int
    val rawPacket: ByteArray
    val encodedAudio: ByteBuffer

    constructor(packet: DatagramPacket) : this(packet.data.copyOf(packet.length))

    constructor(rawPacket: ByteArray) {
        this.rawPacket = rawPacket

        val buffer = ByteBuffer.wrap(rawPacket)
        this.sequence = buffer.getChar(SEQ_INDEX)
        this.timestamp = buffer.getInt(TIMESTAMP_INDEX)
        this.sSRC = buffer.getInt(SSRC_INDEX)
        this.type = buffer[PT_INDEX]

        val profile = buffer[0]
        val data = buffer.array()
        val hasExtension = (profile.toInt() and 0x10) != 0 // extension bit is at 000X
        val cc = (profile.toInt() and 0x0f).toByte() // CSRC count - we ignore this for now
        val csrcLength = cc * 4 // defines count of 4-byte words
        // it seems as if extensions only exist without a csrc list being present
        val extension: Short =
            if (hasExtension) getShortBigEndian(data, RTP_HEADER_BYTE_LENGTH + csrcLength) else 0

        var offset = RTP_HEADER_BYTE_LENGTH + csrcLength
        if (hasExtension && extension == RTP_DISCORD_EXTENSION) offset = getPayloadOffset(data, csrcLength)

        this.encodedAudio = ByteBuffer.allocate(data.size - offset)
        encodedAudio.put(data, offset, encodedAudio.capacity())
        (encodedAudio as Buffer).flip()
    }

    constructor(buffer: ByteBuffer, seq: Char, timestamp: Int, ssrc: Int, encodedAudio: ByteBuffer) {
        this.sequence = seq
        this.sSRC = ssrc
        this.timestamp = timestamp
        this.encodedAudio = encodedAudio
        this.type = RTP_PAYLOAD_TYPE
        this.rawPacket = generateRawPacket(buffer, seq, timestamp, ssrc, encodedAudio)
    }

    private fun getPayloadOffset(data: ByteArray, csrcLength: Int): Int {
        // headerLength defines number of 4-byte words in the extension
        val headerLength: Short = getShortBigEndian(data, RTP_HEADER_BYTE_LENGTH + 2 + csrcLength)
        var i = (RTP_HEADER_BYTE_LENGTH // RTP header = 12 bytes
                + 4 // header which defines a profile and length each 2-bytes = 4 bytes
                + csrcLength) + headerLength * 4 // number of 4-byte words in extension = len * 4 bytes

        // strip excess 0 bytes
        while (data[i].toInt() == 0) i++
        return i
    }

    @get:Suppress("unused")
    val header: ByteArray
        get() =//The first 12 bytes of the rawPacket are the RTP Discord Nonce.
            rawPacket.copyOf(RTP_HEADER_BYTE_LENGTH)

    val noncePadded: ByteArray
        get() {
            val nonce = ByteArray(SecretBox.nonceLength)
            //The first 12 bytes are the rawPacket are the RTP Discord Nonce.
            System.arraycopy(rawPacket, 0, nonce, 0, RTP_HEADER_BYTE_LENGTH)
            return nonce
        }

    fun asEncryptedPacket(boxer: SecretBox, buffer: ByteBuffer, nonce: ByteArray?, nlen: Int): ByteBuffer {
        //Xsalsa20's Nonce is 24 bytes long, however RTP (and consequently Discord)'s nonce is a different length
        // so we need to create a 24 byte array, and copy the nonce into it.
        // we will leave the extra bytes as nulls. (Java sets non-populated bytes as 0).
        var buffer = buffer
        var extendedNonce = nonce
        if (nlen == 0) // this means the header is the nonce!
            extendedNonce = noncePadded

        //Create our SecretBox encoder with the secretKey provided by Discord.
        val array = encodedAudio.array()
        val offset = encodedAudio.arrayOffset() + encodedAudio.position()
        val length = encodedAudio.remaining()
        val encryptedAudio = boxer.box(array, offset, length, extendedNonce)

        (buffer as Buffer).clear()
        val capacity = RTP_HEADER_BYTE_LENGTH + encryptedAudio.size + nlen
        if (capacity > buffer.remaining()) buffer = ByteBuffer.allocate(capacity)
        populateBuffer(
            sequence, timestamp,
            sSRC, ByteBuffer.wrap(encryptedAudio), buffer
        )
        if (nlen > 0) // this means we append the nonce to the payload
            buffer.put(nonce, 0, nlen)

        (buffer as Buffer).flip()
        return buffer
    }

    companion object {
        const val RTP_HEADER_BYTE_LENGTH: Int = 12

        /**
         * Bit index 0 and 1 represent the RTP Protocol version used. Discord uses the latest RTP protocol version, 2.<br></br>
         * Bit index 2 represents whether or not we pad. Opus uses an internal padding system, so RTP padding is not used.<br></br>
         * Bit index 3 represents if we use extensions.<br></br>
         * Bit index 4 to 7 represent the CC or CSRC count. CSRC is Combined SSRC.
         */
        const val RTP_VERSION_PAD_EXTEND: Byte = 0x80.toByte() //Binary: 1000 0000

        /**
         * This is Discord's RTP Profile Payload type.<br></br>
         * I've yet to find actual documentation on what the bits inside this value represent.
         */
        const val RTP_PAYLOAD_TYPE: Byte = 0x78.toByte() //Binary: 0100 1000

        /**
         * This defines the extension type used by discord for presumably video?
         */
        const val RTP_DISCORD_EXTENSION: Short = 0xBEDE.toShort()

        const val PT_INDEX: Int = 1
        const val SEQ_INDEX: Int = 2
        const val TIMESTAMP_INDEX: Int = 4
        const val SSRC_INDEX: Int = 8

        protected fun decryptAudioPacket(
            encryption: AudioEncryption?,
            packet: DatagramPacket,
            secretKey: ByteArray?
        ): AudioPacket? {
            val boxer = SecretBox(secretKey)
            val encryptedPacket = AudioPacket(packet)
            if (encryptedPacket.type != RTP_PAYLOAD_TYPE) return null

            val extendedNonce: ByteArray
            val rawPacket = encryptedPacket.rawPacket
            when (encryption) {
                AudioEncryption.XSALSA20_POLY1305 -> extendedNonce = encryptedPacket.noncePadded
                AudioEncryption.XSALSA20_POLY1305_SUFFIX -> {
                    extendedNonce = ByteArray(SecretBox.nonceLength)
                    System.arraycopy(
                        rawPacket,
                        rawPacket.size - extendedNonce.size,
                        extendedNonce,
                        0,
                        extendedNonce.size
                    )
                }

                AudioEncryption.XSALSA20_POLY1305_LITE -> {
                    extendedNonce = ByteArray(SecretBox.nonceLength)
                    System.arraycopy(rawPacket, rawPacket.size - 4, extendedNonce, 0, 4)
                }

                else -> {
                    getLogger().info("Failed to decrypt audio packet, unsupported encryption mode!")
                    return null
                }
            }
            val encodedAudio = encryptedPacket.encodedAudio
            var length = encodedAudio.remaining()
            val offset = encodedAudio.arrayOffset() + encodedAudio.position()
            when (encryption) {
                AudioEncryption.XSALSA20_POLY1305 -> {}
                AudioEncryption.XSALSA20_POLY1305_LITE -> length -= 4
                AudioEncryption.XSALSA20_POLY1305_SUFFIX -> length -= SecretBox.nonceLength
            }
            val decryptedAudio = boxer.open(encodedAudio.array(), offset, length, extendedNonce)
            if (decryptedAudio == null) {
                getLogger().info("Failed to decrypt audio packet")
                return null
            }
            val decryptedRawPacket = ByteArray(RTP_HEADER_BYTE_LENGTH + decryptedAudio.size)

            //first 12 bytes of rawPacket are the RTP header
            //the rest is the audio data we just decrypted
            System.arraycopy(encryptedPacket.rawPacket, 0, decryptedRawPacket, 0, RTP_HEADER_BYTE_LENGTH)
            System.arraycopy(decryptedAudio, 0, decryptedRawPacket, RTP_HEADER_BYTE_LENGTH, decryptedAudio.size)

            return AudioPacket(decryptedRawPacket)
        }

        private fun generateRawPacket(
            buffer: ByteBuffer,
            seq: Char,
            timestamp: Int,
            ssrc: Int,
            data: ByteBuffer
        ): ByteArray {
            var buffer: ByteBuffer? = buffer
            if (buffer == null) buffer = ByteBuffer.allocate(RTP_HEADER_BYTE_LENGTH + data.remaining())
            populateBuffer(seq, timestamp, ssrc, data, buffer)
            return buffer!!.array()
        }

        private fun populateBuffer(seq: Char, timestamp: Int, ssrc: Int, data: ByteBuffer, buffer: ByteBuffer?) {
            buffer!!.put(RTP_VERSION_PAD_EXTEND)
            buffer.put(RTP_PAYLOAD_TYPE)
            buffer.putChar(seq)
            buffer.putInt(timestamp)
            buffer.putInt(ssrc)
            buffer.put(data)
            (data as Buffer).flip()
        }
    }
}