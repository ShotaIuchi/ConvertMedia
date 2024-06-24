package jp.si.test.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConverterViewModel {
    private var _audioEncodeOptions = MutableStateFlow<MutableMap<String, AudioEncodeOption>>(mutableMapOf())
    val audioEncodeOptions: StateFlow<Map<String, AudioEncodeOption>> = _audioEncodeOptions

    private val _videoEncodeOptions = MutableStateFlow<MutableMap<String, VideoEncodeOption>>(mutableMapOf())
    val videoEncodeOptions: StateFlow<Map<String, VideoEncodeOption>> = _videoEncodeOptions

    init {
        _audioEncodeOptions.value[AudioEncodeOptionAAC().name] = AudioEncodeOptionAAC()
    }
}