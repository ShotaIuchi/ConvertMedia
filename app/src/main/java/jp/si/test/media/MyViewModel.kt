package jp.si.test.media

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ConvertInfo(
    val id: Int,
    val fileName: String,
    val audioCodec: String,
    val videoCodec: String,
    val targetAudioCodec: String,
    val targetVideoCodec: String,
    var errorMessage: String = "",
)

class MyViewModel : ViewModel() {
    private val _taskCount = MutableStateFlow(0)
    val taskCount: StateFlow<Int> = _taskCount

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    private val _allCount = MutableStateFlow(0)
    val allCount: StateFlow<Int> = _allCount

    private val _bothCount = MutableStateFlow(0)
    val bothCount: StateFlow<Int> = _bothCount

    private val _audioOnlyCount = MutableStateFlow(0)
    val audioOnlyCount: StateFlow<Int> = _audioOnlyCount

    private val _videoOnlyCount = MutableStateFlow(0)
    val videoOnlyCount: StateFlow<Int> = _videoOnlyCount

    private val _errorCount = MutableStateFlow(0)
    val errorCount: StateFlow<Int> = _errorCount

    private val _activeMessages = MutableStateFlow<List<ConvertInfo>>(emptyList())
    val activeMessages: StateFlow<List<ConvertInfo>> = _activeMessages

    private val _errorMessages = MutableStateFlow<List<ConvertInfo>>(emptyList())
    val errorMessages: StateFlow<List<ConvertInfo>> = _errorMessages

    fun updateTaskCount(count:Int) {
        _taskCount.value = count
    }

    fun incrementTotalCount(): Int {
        _totalCount.value += 1
        return _totalCount.value
    }

    fun incrementBothCount() {
        _bothCount.value += 1
        _allCount.value += 1
    }

    fun incrementAudioOnlyCount() {
        _audioOnlyCount.value += 1
        _allCount.value += 1
    }

    fun incrementVideoOnlyCount() {
        _videoOnlyCount.value += 1
        _allCount.value += 1
    }

    fun incrementErrorCount() {
        _errorCount.value += 1
    }

    fun addActiveMessage(activeInfo: ConvertInfo) {
        _activeMessages.value = _activeMessages.value + activeInfo
    }

    fun removeActiveMassage(activeInfoId: Int) {
        _activeMessages.value = _activeMessages.value.filter { it.id != activeInfoId }
    }

    fun addErrorMessage(errorInfo: ConvertInfo) {
        _errorMessages.value = _errorMessages.value + errorInfo
    }
}
