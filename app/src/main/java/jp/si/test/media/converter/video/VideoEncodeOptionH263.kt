package jp.si.test.media.converter.video

import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log

class VideoEncodeOptionH263(
    override val width: Int? = null,
    override val height: Int? = null,
    override val bitRate: Int? = null,
    override val frameRate: Int? = null,
    override val iFrameInterval: Int? = null,
    override val colorFormat: Int? = null,
) : VideoEncodeOption() {
    override val mime: String
        get() = MediaFormat.MIMETYPE_VIDEO_H263

    override var dWidth: Int = 176
    override var dHeight: Int = 144
    override var dBitRate: Int = 64000
    override var dFrameRate: Int = 15
    override var dIFrameInterval: Int = 10
    override var dColorFormat: Int = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
}