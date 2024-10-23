package net.blockventuremc.audioserver.audio


import java.util.*

enum class AudioEncryption {
    // these are ordered by priority, lite > suffix > normal
    // we prefer lite because it uses only 4 bytes for its nonce while the others use 24 bytes
    XSALSA20_POLY1305_LITE,
    XSALSA20_POLY1305_SUFFIX,
    XSALSA20_POLY1305;

    val key: String = name.lowercase(Locale.getDefault())
}