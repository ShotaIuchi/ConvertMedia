package jp.si.test.media

import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat

abstract class VideoEncodeOption : EncodeOption()

class VideoEncodeOptionAVC(
    private val bitRate: Int? = null,
    private val frameRate: Int? = null,
    private val iFrameInterval: Int? = null,
    private val colorFormat: Int? = null,
) : VideoEncodeOption() {
    override fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat {
        val width = inputFormat.getInteger(MediaFormat.KEY_WIDTH)
        val height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT)

        return MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            applyInteger(this, inputFormat, MediaFormat.KEY_BIT_RATE, bitRate, 2000000)
            applyInteger(this, inputFormat, MediaFormat.KEY_FRAME_RATE, frameRate, 30)
            applyInteger(this, inputFormat, MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval, 2)
            // optional
            applyInteger(this, inputFormat, MediaFormat.KEY_COLOR_FORMAT, colorFormat, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
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
