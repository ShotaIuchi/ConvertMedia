package jp.si.test.media.converter

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
