package jp.si.test.media.converter

import android.media.MediaExtractor
import android.media.MediaFormat
import jp.si.test.media.converter.audio.AudioConverter
import jp.si.test.media.converter.audio.AudioEncodeOption
import jp.si.test.media.converter.video.VideoConverter
import jp.si.test.media.converter.video.VideoEncodeOption
import java.io.File

class MediaConverterManager {
    fun convert(
        inputFilePath: File,
        audioOutputFilePath: File,
        videoOutputFilePath: File,
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

    private fun hasTrack(file: File, mimePrefix: String): Boolean {
        return getCodecInfo(file, mimePrefix).isNotEmpty()
    }

    fun getCodecName(file: File, mimePrefix: String): String {
        return getCodecInfo(file, mimePrefix).split("/").getOrNull(1) ?: ""
    }

    fun getCodecInfo(file: File, mimePrefix: String): String {
        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith(mimePrefix)) {
                extractor.release()
                return mime
            }
        }
        extractor.release()
        return ""
    }
}
