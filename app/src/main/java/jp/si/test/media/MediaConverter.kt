package jp.si.test.media

import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.IOException

class MediaConverter {
    fun convert(
        inputFilePath: String,
        audioOutputFilePath: String,
        videoOutputFilePath: String,
        audioEncodeOption: AudioEncodeOption,
        videoEncodeOption: VideoEncodeOption
    ) {
        val hasAudio = hasTrack(inputFilePath, "audio/")
        val hasVideo = hasTrack(inputFilePath, "video/")

        if (hasAudio && hasVideo) {
            AudioConverter(inputFilePath, audioOutputFilePath, audioEncodeOption).convert()
            VideoConverter(inputFilePath, videoOutputFilePath, videoEncodeOption).convert()
        } else if (hasAudio) {
            AudioConverter(inputFilePath, audioOutputFilePath, audioEncodeOption).convert()
        } else if (hasVideo) {
            VideoConverter(inputFilePath, videoOutputFilePath, videoEncodeOption).convert()
        } else {
            // データなし
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
