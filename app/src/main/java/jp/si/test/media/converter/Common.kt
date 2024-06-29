package jp.si.test.media.converter

import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File

fun hasTrack(file: File, type: MediaType): Boolean {
    return getCodecInfo(file, type).isNotEmpty()
}

fun getCodecName(file: File, type: MediaType): String {
    return getCodecInfo(file, type).split("/").getOrNull(1) ?: ""
}

fun getCodecInfo(file: File, type: MediaType): String {
    val extractor = MediaExtractor()
    extractor.setDataSource(file.absolutePath)
    try {
        return getCodecInfo(extractor, type)?.second ?: ""
    } finally {
        extractor.release()
    }
}

fun getCodecInfo(extractor: MediaExtractor, type: MediaType): Pair<Int, String>? {
    for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)
        if (mime != null && mime.startsWith(type.mimePrefix)) {
            return Pair(i, mime)
        }
    }
    return null
}
