package jp.si.test.media.converter.audio

import android.media.MediaExtractor
import jp.si.test.media.converter.EncodeOption
import jp.si.test.media.converter.MediaConverter
import java.io.File

class AudioConverter(
    override val inputFilePath: File,
    override val outputFilePath: File,
    override val encode: EncodeOption
) : MediaConverter() {
    override fun selectTrack(extractor: MediaExtractor): Int = selectTrack(extractor, "audio/")
}
