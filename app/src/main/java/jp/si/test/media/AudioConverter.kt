package jp.si.test.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException

class AudioConverter : AbstractMediaConverter() {
    @Throws(IOException::class)
    override fun setupExtractor(inputFilePath: String) {
        extractor = MediaExtractor()
        extractor.setDataSource(inputFilePath)
        val trackIndex = selectTrack(extractor, "audio/")
        if (trackIndex == -1) {
            throw RuntimeException("ファイルに音声トラックがありません。")
        }
        extractor.selectTrack(trackIndex)
    }

    @Throws(IOException::class)
    override fun setupDecoder() {
        val format = extractor.getTrackFormat(extractor.sampleTrackIndex)
        val codec = format.getString(MediaFormat.KEY_MIME)!!
        decoder = MediaCodec.createDecoderByType(codec)
        decoder.configure(format, null, null, 0)
        decoder.start()
    }

    @Throws(IOException::class)
    override fun setupEncoder(outputFilePath: String) {
        val inputFormat = extractor.getTrackFormat(extractor.sampleTrackIndex)
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val bitRate = inputFormat.getInteger(MediaFormat.KEY_BIT_RATE)

        val format =
            MediaFormat.createAudioFormat("audio/mp4a-latm", sampleRate, channelCount).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(
                    MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC
                )
            }

        encoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        outputStream = FileOutputStream(outputFilePath)
    }
}