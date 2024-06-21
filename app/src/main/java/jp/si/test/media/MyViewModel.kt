package jp.si.test.media

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ErrorInfo(
    val fileName: String,
    val errorMessage: String
)

class MyViewModel : ViewModel() {
    private val _totalFiles = MutableStateFlow(0)
    val totalFiles: StateFlow<Int> = _totalFiles

    private val _bothCount = MutableStateFlow(0)
    val bothCount: StateFlow<Int> = _bothCount

    private val _audioOnlyCount = MutableStateFlow(0)
    val audioOnlyCount: StateFlow<Int> = _audioOnlyCount

    private val _videoOnlyCount = MutableStateFlow(0)
    val videoOnlyCount: StateFlow<Int> = _videoOnlyCount

    private val _errorCount = MutableStateFlow(0)
    val errorCount: StateFlow<Int> = _errorCount

    private val _errorMessages = MutableStateFlow<List<ErrorInfo>>(emptyList())
    val errorMessages: StateFlow<List<ErrorInfo>> = _errorMessages

    fun incrementBothCount() {
        _bothCount.value += 1
        _totalFiles.value += 1
    }

    fun incrementAudioOnlyCount() {
        _audioOnlyCount.value += 1
        _totalFiles.value += 1
    }

    fun incrementVideoOnlyCount() {
        _videoOnlyCount.value += 1
        _totalFiles.value += 1
    }

    fun incrementErrorCount() {
        _errorCount.value += 1
    }

    fun addErrorMessage(errorInfo: ErrorInfo) {
        _errorMessages.value = _errorMessages.value + errorInfo
    }
}
