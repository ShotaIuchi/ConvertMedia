package jp.si.test.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.core.graphics.createBitmap
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class VideoConverter : AbstractMediaConverter() {
    companion object {
        private const val OUTPUT_MIME_TYPE = "video/avc"
    }

    @Throws(IOException::class)
    override fun setupExtractor(inputFilePath: String) {
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

    @Throws(IOException::class)
    override fun setupEncoder(outputFilePath: String) {
        val format = extractor.getTrackFormat(extractor.sampleTrackIndex)
        val width = format.getInteger(MediaFormat.KEY_WIDTH)
        val height = format.getInteger(MediaFormat.KEY_HEIGHT)

        val frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
//        val iFrameRate = format.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL)
//        val bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE)
        val inputVideoMime = format.getString(MediaFormat.KEY_MIME)!!

        val outputFormat = MediaFormat.createVideoFormat(inputVideoMime, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, 2000000)
            setInteger(MediaFormat.KEY_FRAME_RATE, format.getInteger(MediaFormat.KEY_FRAME_RATE))
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
        }

        encoder = MediaCodec.createEncoderByType(inputVideoMime)//"video/avc")
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        outputStream = FileOutputStream(outputFilePath)
    }

}
