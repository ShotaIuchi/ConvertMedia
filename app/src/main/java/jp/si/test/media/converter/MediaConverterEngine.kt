package jp.si.test.media.converter

import android.media.MediaExtractor

abstract class MediaConverterEngine {
    abstract val encode: EncodeOption

    abstract fun convert()

    protected fun selectTrack(extractor: MediaExtractor): Int =
        getCodecInfo(extractor, encode.type)?.first ?: -1
}
