package jp.si.test.media.converter.video

import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Range
import jp.si.test.media.converter.EncodeOption
import jp.si.test.media.converter.MediaType


abstract class VideoEncodeOption : EncodeOption() {
    abstract val width: Int?
    abstract val height: Int?
    abstract val bitRate: Int?
    abstract val frameRate: Int?
    abstract val iFrameInterval: Int?
    abstract val colorFormat: Int?

    abstract var dWidth: Int
    abstract var dHeight: Int
    abstract var dBitRate: Int
    abstract var dFrameRate: Int
    abstract var dIFrameInterval: Int
    abstract var dColorFormat: Int

    override val type: MediaType
        get() = MediaType.VIDEO

//    protected fun clamp(range: Range<Int>, value: Int): Int = range.clamp(value)

    override fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat {
        val capabilities = capabilities()
//        capabilities?.let {
//            dWidth = it.videoCapabilities.supportedWidths.lower
//            dHeight = it.videoCapabilities.supportedHeights.lower
//            dBitRate = it.defaultFormat.getInteger(MediaFormat.KEY_BIT_RATE, dBitRate)
//            dFrameRate = it.defaultFormat.getInteger(MediaFormat.KEY_FRAME_RATE, dFrameRate)
//            dIFrameInterval = it.defaultFormat.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL, dIFrameInterval)
//            dColorFormat = it.colorFormats[0]
//        }
        val width = getInteger(inputFormat, MediaFormat.KEY_WIDTH, width, dWidth)
        val height = getInteger(inputFormat, MediaFormat.KEY_HEIGHT, height, dHeight)
        return MediaFormat.createVideoFormat(mime, width, height).apply {
            applyInteger(this, inputFormat, MediaFormat.KEY_BIT_RATE, bitRate, dBitRate)
            applyInteger(this, inputFormat, MediaFormat.KEY_FRAME_RATE, frameRate, dFrameRate)
            applyInteger(this, inputFormat, MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval, dIFrameInterval)
            applyInteger(this, inputFormat, MediaFormat.KEY_COLOR_FORMAT, colorFormat, dColorFormat)
        }
    }
}
