package jp.si.test.media

import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat

abstract class AudioEncodeOption : EncodeOption()

class AudioEncodeOptionAAC(
    private val bitRate: Int? = null,
    private val profile: Int? = null,
) : AudioEncodeOption() {
    override fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat {
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        return MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount).apply {
            applyInteger(this, inputFormat, MediaFormat.KEY_BIT_RATE, bitRate, 128000)
            applyInteger(this, inputFormat, MediaFormat.KEY_AAC_PROFILE, profile, MediaCodecInfo.CodecProfileLevel.AACObjectMain)
        }
    }
}

class AudioConverter(
    inputFilePath: String,
    outputFilePath: String,
    encode: AudioEncodeOption,
) : AbstractMediaConverter(inputFilePath, outputFilePath, encode) {
    override fun selectTrack(extractor: MediaExtractor): Int {
        return selectTrack(extractor, "audio/")
    }
}