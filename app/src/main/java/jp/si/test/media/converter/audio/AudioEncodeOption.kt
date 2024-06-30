package jp.si.test.media.converter.audio

import android.media.MediaCodecInfo
import android.media.MediaFormat
import jp.si.test.media.converter.EncodeOption
import jp.si.test.media.converter.MediaType

abstract class AudioEncodeOption : EncodeOption() {
    abstract val bitRate: Int?
    abstract val profile: Int?

    override val type: MediaType
        get() = MediaType.AUDIO

    override fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat {
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        return MediaFormat.createAudioFormat(mime, sampleRate, channelCount).apply {
            applyInteger(this, inputFormat, MediaFormat.KEY_BIT_RATE, bitRate, 128000)
            applyInteger(this, inputFormat, MediaFormat.KEY_AAC_PROFILE, profile, MediaCodecInfo.CodecProfileLevel.AACObjectMain)
        }
    }
}