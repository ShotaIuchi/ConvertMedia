package jp.si.test.media.converter.audio

import android.media.MediaCodecInfo
import android.media.MediaFormat

class AudioEncodeOptionAAC(
    private val bitRate: Int? = null,
    private val profile: Int? = null,
) : AudioEncodeOption() {
    override val mime: String
        get() = MediaFormat.MIMETYPE_AUDIO_AAC

    override fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat {
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        return MediaFormat.createAudioFormat(mime, sampleRate, channelCount).apply {
            applyInteger(this, inputFormat, MediaFormat.KEY_BIT_RATE, bitRate, 128000)
            applyInteger(this, inputFormat, MediaFormat.KEY_AAC_PROFILE, profile, MediaCodecInfo.CodecProfileLevel.AACObjectMain)
        }
    }
}