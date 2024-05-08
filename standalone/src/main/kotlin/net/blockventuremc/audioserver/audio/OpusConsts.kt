package net.blockventuremc.audioserver.audio

/** (Hz) We want to use the highest of qualities! All the bandwidth!  */
const val OPUS_SAMPLE_RATE = 48000
/** An opus frame size of 960 at 48000hz represents 20 milliseconds of audio.  */
const val OPUS_FRAME_SIZE = 960
/** This is 20 milliseconds. We are only dealing with 20ms opus packets.  */
const val OPUS_FRAME_TIME_AMOUNT = 20
/** We want to use stereo. If the audio given is mono, the encoder promotes it to Left and Right mono (stereo that is the same on both sides)  */
const val OPUS_CHANNEL_COUNT = 2
/** Maximum value of an unsigned 32-bit integer. */
const val MAX_UINT_32 = 4294967295L