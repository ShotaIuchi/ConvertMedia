package jp.si.test.media.converter.video

import android.media.MediaCodecInfo
import android.media.MediaFormat

class VideoEncodeOptionH264(
    override val width: Int? = null,
    override val height: Int? = null,
    override val bitRate: Int? = null,
    override val frameRate: Int? = null,
    override val iFrameInterval: Int? = null,
    override val colorFormat: Int? = null,
) : VideoEncodeOption() {
    override val mime: String
        get() = MediaFormat.MIMETYPE_VIDEO_AVC

    override var dWidth: Int = 128
    override var dHeight: Int = 128
    override var dBitRate: Int = 2000000
    override var dFrameRate: Int = 30
    override var dIFrameInterval: Int = 2
    override var dColorFormat: Int = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
}