package net.blockventuremc.audioserver.audio

import com.iwebpp.crypto.TweetNaclFast
import com.iwebpp.crypto.TweetNaclFast.SecretBox
import net.blockventuremc.audioserver.common.extensions.getLogger
import net.blockventuremc.audioserver.utils.setIntBigEndian
import java.net.DatagramPacket
import java.nio.Buffer
import java.nio.ByteBuffer
import java.util.concurrent.ThreadLocalRandom


class AudioConnection(private val sendHandler: AudioSendHandler, private val boxer: SecretBox) {

    private val nonceBuffer = ByteArray(SecretBox.nonceLength)
    private var seq = 0.toChar() //Sequence of audio packets. Used to determine the order of the packets.
    private var timestamp = 0 //Used to sync up our packets within the same timeframe of other people talking.
    private var nonce: Long = 0
    private var buffer: ByteBuffer = ByteBuffer.allocate(512)
    private var encryptionBuffer: ByteBuffer = ByteBuffer.allocate(512)

    fun getNextPacket(unused: Boolean): DatagramPacket? {
        val buffer = getNextPacketRaw(unused)
        return if (buffer == null) null else getDatagramPacket(buffer)
    }

    fun getNextPacketRaw(unused: Boolean): ByteBuffer? {
        var nextPacket: ByteBuffer? = null
        try {
            if (sendHandler.canProvide()) {
                val rawAudio: ByteBuffer = sendHandler.provide20MsAudio()
                if (!rawAudio.hasArray()) {
                    // we can't use the boxer without an array so encryption would not work
                    getLogger().error("AudioSendHandler provided ByteBuffer without a backing array! This is unsupported.")
                }

                if (rawAudio.hasRemaining() && rawAudio.hasArray()) {
                    nextPacket = getPacketData(rawAudio)

                    if (seq + 1 > Character.MAX_VALUE) seq = 0
                    else seq++
                }
            }
        } catch (e: Exception) {
            getLogger().error("There was an error while getting next audio packet", e)
        }

        if (nextPacket != null) timestamp += OPUS_FRAME_SIZE

        return nextPacket
    }

    private fun getDatagramPacket(b: ByteBuffer): DatagramPacket {
        val data = b.array()
        val offset = b.arrayOffset() + b.position()
        val length = b.remaining()
        return DatagramPacket(data, offset, length, webSocket.getAddress())
    }

    private fun getPacketData(rawAudio: ByteBuffer): ByteBuffer {
        ensureEncryptionBuffer(rawAudio)
        val packet = AudioPacket(encryptionBuffer, seq, timestamp, webSocket.getSSRC(), rawAudio)
        val nlen: Int
        when (webSocket.encryption) {
            AudioEncryption.XSALSA20_POLY1305 -> nlen = 0
            AudioEncryption.XSALSA20_POLY1305_LITE -> {
                if (nonce >= MAX_UINT_32) loadNextNonce(0L.also { nonce = it })
                else loadNextNonce(++nonce)
                nlen = 4
            }

            AudioEncryption.XSALSA20_POLY1305_SUFFIX -> {
                ThreadLocalRandom.current().nextBytes(nonceBuffer)
                nlen = TweetNaclFast.SecretBox.nonceLength
            }

            else -> throw IllegalStateException(("Encryption mode [" + webSocket.encryption).toString() + "] is not supported!")
        }
        return packet.asEncryptedPacket(boxer, buffer, nonceBuffer, nlen).also { buffer = it }
    }

    private fun loadNextNonce(nonce: Long) {
        setIntBigEndian(nonceBuffer, 0, nonce.toInt())
    }

    private fun ensureEncryptionBuffer(data: ByteBuffer) {
        (encryptionBuffer as Buffer).clear()
        val currentCapacity: Int = encryptionBuffer.remaining()
        val requiredCapacity = AudioPacket.RTP_HEADER_BYTE_LENGTH + data.remaining()
        if (currentCapacity < requiredCapacity) encryptionBuffer = ByteBuffer.allocate(requiredCapacity)
    }


}