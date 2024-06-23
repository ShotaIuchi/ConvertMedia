package jp.si.test.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import androidx.core.graphics.createBitmap
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class VideoConverter(
        inputFilePath: String,
        outputFilePath: String,
        encodeOption: EncodeOption,
) : AbstractMediaConverter(inputFilePath, outputFilePath, encodeOption) {

    @Throws(IOException::class)
    override fun setupExtractor() {
        extractor = MediaExtractor()
        extractor.setDataSource(inputFilePath)
        val trackIndex = selectTrack(extractor, "video/")
        if (trackIndex == -1) {
            throw RuntimeException("ファイルに動画トラックがありません。")
        }
        extractor.selectTrack(trackIndex)
    }

    @Throws(IOException::class)
    override fun setupDecoder() {
        val format = extractor.getTrackFormat(extractor.sampleTrackIndex)
        decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
        decoder.configure(format, null, null, 0)
        decoder.start()
    }

    // test
    fun test(): Int {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        var supportedColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        for (codecInfo in codecList.codecInfos) {
            if (codecInfo.isEncoder) {
                for (type in codecInfo.supportedTypes) {
                    if (type.equals("video/hevc", ignoreCase = true)) {
                        val capabilities = codecInfo.getCapabilitiesForType(type)
                        for (colorFormat in capabilities.colorFormats) {
                            if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible) {
                                supportedColorFormat = colorFormat
                                break
                            }
                        }
                    }
                }
            }
        }
        return supportedColorFormat
    }

    @Throws(IOException::class)
    override fun setupEncoder() {
        val format = extractor.getTrackFormat(extractor.sampleTrackIndex)
        val width = format.getInteger(MediaFormat.KEY_WIDTH)
        val height = format.getInteger(MediaFormat.KEY_HEIGHT)

        val outputFormat = MediaFormat.createVideoFormat("video/${encodeOption.codec}", width, height).apply {
            val bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE, 2000000)
            val frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE, 30)
            val iFrameInterval = format.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)

            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
        }

        encoder = MediaCodec.createEncoderByType("video/${encodeOption.codec}")
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        outputStream = FileOutputStream(outputFilePath)
    }
}
