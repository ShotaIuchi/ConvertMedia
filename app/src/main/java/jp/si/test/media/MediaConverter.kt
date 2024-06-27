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

    fun hasTrack(filePath: String, mimePrefix: String): Boolean {
        return getCodecInfo(filePath, mimePrefix).isNotEmpty()
    }

    fun getCodecInfo(filePath: String, mimePrefix: String): String {
        val extractor = MediaExtractor()
        extractor.setDataSource(filePath)
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith(mimePrefix)) {
                extractor.release()
                return mime//mime.split("/").getOrNull(1) ?: ""
            }
        }
        extractor.release()
        return ""
    }
}
