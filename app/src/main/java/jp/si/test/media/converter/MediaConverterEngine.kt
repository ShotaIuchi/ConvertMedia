package jp.si.test.media.converter

import android.media.MediaExtractor
import android.media.MediaFormat

abstract class MediaConverterEngine {
    abstract val encode: EncodeOption

    abstract fun convert()

    protected fun selectTrack(extractor: MediaExtractor): Int =
        selectTrack(extractor, encode.type.mimePrefix)

    private fun selectTrack(extractor: MediaExtractor, mimePrefix: String): Int {
        val trackCount = extractor.trackCount
        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith(mimePrefix)) {
                return i
            }
        }
        return -1
    }
}
