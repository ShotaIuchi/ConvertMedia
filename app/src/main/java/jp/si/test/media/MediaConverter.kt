package jp.si.test.media

import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.IOException

class MediaConverter {
    fun convert(inputFilePath: String, audioOutputFilePath: String, videoOutputFilePath: String) {
        try {
            val hasAudio = hasTrack(inputFilePath, "audio/")
            val hasVideo = hasTrack(inputFilePath, "video/")

            if (hasAudio && hasVideo) {
                AudioConverter().convert(inputFilePath, audioOutputFilePath)
//                VideoConverter().convert(inputFilePath, videoOutputFilePath)
            } else if (hasAudio) {
                AudioConverter().convert(inputFilePath, audioOutputFilePath)
            } else if (hasVideo) {
                VideoConverter().convert(inputFilePath, videoOutputFilePath)
            } else {
                throw RuntimeException("ファイルに音声トラックも動画トラックもありません。")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun hasTrack(inputFilePath: String, mimePrefix: String): Boolean {
        val extractor = MediaExtractor()
        extractor.setDataSource(inputFilePath)
        val trackCount = extractor.trackCount
        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith(mimePrefix)) {
                extractor.release()
                return true
            }
        }
        extractor.release()
        return false
    }
}
