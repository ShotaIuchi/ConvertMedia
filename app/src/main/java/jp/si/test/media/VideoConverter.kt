package jp.si.test.media

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat

abstract class VideoEncodeOption : EncodeOption() {
//    data class Config(
//        var width: Int = 0,
//        var height: Int = 0,
//        var bitRate: Int = 0,
//        var frameRate: Int = 0,
//    )
//
//    fun configFromMediaFormat(srcFormat: MediaFormat): Config {
//        return normalize(Config().apply {
//            width = srcFormat.getInteger(MediaFormat.KEY_WIDTH, 128)
//            height = srcFormat.getInteger(MediaFormat.KEY_HEIGHT, 128)
//            bitRate = srcFormat.getInteger(MediaFormat.KEY_BIT_RATE, 2000000)
//            frameRate = srcFormat.getInteger(MediaFormat.KEY_FRAME_RATE, 30)
//        })
//    }
//
//    private fun normalize(config: Config): Config {
//        val capabilities = capabilities() ?: return config
//        return config.apply {
//            width = clamp(width, capabilities.supportedWidths)
//            height = clamp(height, capabilities.supportedHeights)
//            bitRate = clamp(bitRate, capabilities.bitrateRange)
//            frameRate = clamp(frameRate, capabilities.supportedFrameRates)
//        }
//    }
//
    protected fun clamp(value: Int, range: android.util.Range<Int>): Int {
        return when {
            value < range.lower -> range.lower
            value > range.upper -> range.upper
            else -> value
        }
    }

    protected fun capabilities(): MediaCodecInfo.VideoCapabilities? {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        for (codecInfo in codecList.codecInfos) {
            if (codecInfo.isEncoder) {
                val supportedTypes = codecInfo.supportedTypes
                for (type in supportedTypes) {
                    return codecInfo.getCapabilitiesForType(type)?.videoCapabilities
                }
            }
        }
        return null
    }


    override fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat {
        val capabilities = capabilities() ?: throw RuntimeException("capabilities is null")

        val width = inputFormat.getInteger(MediaFormat.KEY_WIDTH, 128).apply {
            clamp(this, capabilities.supportedWidths)
        }
        val height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT, 128).apply {
            clamp(this, capabilities.supportedHeights)
        }

        return MediaFormat.createVideoFormat(name, width, height).apply {
            inputFormat.getInteger(MediaFormat.KEY_BIT_RATE, 2000000).apply {
                clamp(this, capabilities.bitrateRange)
            }
            inputFormat.getInteger(MediaFormat.KEY_FRAME_RATE, 30).apply {
                clamp(this, capabilities.supportedFrameRates)
            }
        }
    }

}

class VideoEncodeOptionAVC(
    private val bitRate: Int = 2000000,
    private val frameRate: Int = 30,
    private val iFrameInterval: Int = 2,
    private val colorFormat: Int? = null// = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible,
) : VideoEncodeOption() {
    override val name: String
        get() = MediaFormat.MIMETYPE_VIDEO_AVC

    override fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat {
        return super.createEncodeFormat(inputFormat).apply {
            applyInteger(this, inputFormat, MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
            if (colorFormat != null) {
                applyInteger(this, inputFormat, MediaFormat.KEY_COLOR_FORMAT, colorFormat)
            }
        }
    }
}
class VideoConverter(
    inputFilePath: String,
    outputFilePath: String,
    encode: VideoEncodeOption,
) : AbstractMediaConverter(inputFilePath, outputFilePath, encode) {
    override fun selectTrack(extractor: MediaExtractor): Int {
        return selectTrack(extractor, "video/")
    }
}

//class VideoConverter(
//    private val inputFilePath: String,
//    private val outputFilePath: String,
//    private val encode: EncodeOption,
//) : AbstractMediaConverter() {
//
//    @Throws(Exception::class)
//    override fun setupExtractor() {
//        extractor = MediaExtractor()
//        extractor.setDataSource(inputFilePath)
//        val trackIndex = selectTrack(extractor, "video/")
//        if (trackIndex == -1) {
//            throw RuntimeException("ファイルに動画トラックがありません。")
//        }
//        extractor.selectTrack(trackIndex)
//    }
//
//    @Throws(Exception::class)
//    override fun setupDecoder() {
//        val format = extractor.getTrackFormat(extractor.sampleTrackIndex)
//        val codec = format.getString(MediaFormat.KEY_MIME)!!
//        decoder = MediaCodec.createDecoderByType(codec)
//        decoder.configure(format, null, null, 0)
//        decoder.start()
//    }
//
//    @Throws(Exception::class)
//    override fun setupEncoder() {
//        val format = encode.createEncodeFormat(extractor.getTrackFormat(extractor.sampleTrackIndex))
//        val codec = format.getString(MediaFormat.KEY_MIME)!!
//        encoder = MediaCodec.createEncoderByType(codec)
//        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//        encoder.start()
//    }
//
////    @Throws(Exception::class)
////    override fun setupEncoder() {
////        val format = extractor.getTrackFormat(extractor.sampleTrackIndex)
////        val width = format.getInteger(MediaFormat.KEY_WIDTH)
////        val height = format.getInteger(MediaFormat.KEY_HEIGHT)
////
////        val outputFormat = MediaFormat.createVideoFormat("video/${encodeOption.codec}", width, height).apply {
////            val bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE, 2000000)
////            val frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE, 30)
////            val iFrameInterval = format.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
////
////            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
////            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
////            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
////            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
////        }
////
////        encoder = MediaCodec.createEncoderByType("video/${encodeOption.codec}")
////        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
////        encoder.start()
////    }
//
//    @Throws(Exception::class)
//    override fun setupOutStream() {
//        outputStream = FileOutputStream(outputFilePath)
//    }
//
//    // test
//    fun test(): Int {
//        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
//        var supportedColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
//        for (codecInfo in codecList.codecInfos) {
//            if (codecInfo.isEncoder) {
//                for (type in codecInfo.supportedTypes) {
//                    if (type.equals("video/hevc", ignoreCase = true)) {
//                        val capabilities = codecInfo.getCapabilitiesForType(type)
//                        for (colorFormat in capabilities.colorFormats) {
//                            if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible) {
//                                supportedColorFormat = colorFormat
//                                break
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return supportedColorFormat
//    }
//}
