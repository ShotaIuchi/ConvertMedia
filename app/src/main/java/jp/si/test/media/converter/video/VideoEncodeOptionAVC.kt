package jp.si.test.media.converter.video

import android.media.MediaCodecInfo
import android.media.MediaFormat

class VideoEncodeOptionAVC(
    private val bitRate: Int? = null,
    private val frameRate: Int? = null,
    private val iFrameInterval: Int? = null,
    private val colorFormat: Int? = null,
) : VideoEncodeOption() {
    override val mime: String
        get() = MediaFormat.MIMETYPE_VIDEO_AVC

    override fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat {
        val width = inputFormat.getInteger(MediaFormat.KEY_WIDTH)
        val height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT)

        return MediaFormat.createVideoFormat(mime, width, height).apply {
            applyInteger(this, inputFormat, MediaFormat.KEY_BIT_RATE, bitRate, 2000000)
            applyInteger(this, inputFormat, MediaFormat.KEY_FRAME_RATE, frameRate, 30)
            applyInteger(this, inputFormat, MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval, 2)
            // optional
            applyInteger(this, inputFormat, MediaFormat.KEY_COLOR_FORMAT, colorFormat, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        }
    }
}