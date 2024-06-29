package jp.si.test.media.converter

import android.media.MediaExtractor
import android.media.MediaFormat
import jp.si.test.media.converter.audio.AudioEncodeOption
import jp.si.test.media.converter.video.VideoEncodeOption
import java.io.File

class MediaConverter {
    fun convert(
        inputFilePath: File,
        audioOutputFilePath: File,
        videoOutputFilePath: File,
        audioEncodeOption: AudioEncodeOption,
        videoEncodeOption: VideoEncodeOption,
        createEngine: (inputFilePath: File, outputFilePath: File, encode: EncodeOption) -> MediaConverterEngine? =
            { i, o, e -> MediaConverterEngineBuffer.create(i, o, e) },
    ) {
        createEngine(inputFilePath, audioOutputFilePath, audioEncodeOption)?.convert()
        createEngine(inputFilePath, videoOutputFilePath, videoEncodeOption)?.convert()
    }
}

fun hasTrack(file: File, type: MediaType): Boolean {
    return getCodecInfo(file, type).isNotEmpty()
}

fun getCodecName(file: File, type: MediaType): String {
    return getCodecInfo(file, type).split("/").getOrNull(1) ?: ""
}

fun getCodecInfo(file: File, type: MediaType): String {
    val extractor = MediaExtractor()
    extractor.setDataSource(file.absolutePath)
    for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)
        if (mime != null && mime.startsWith(type.mimePrefix)) {
            extractor.release()
            return mime
        }
    }
    extractor.release()
    return ""
}
