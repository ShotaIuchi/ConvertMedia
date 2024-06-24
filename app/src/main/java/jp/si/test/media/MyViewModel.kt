package jp.si.test.media

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ConvertInfo(
    val id: Int,
    val fileName: String,
    val audioCodec: String,
    val videoCodec: String,
    val audioEncodeOption: AudioEncodeOption,
    val videoEncodeOption: VideoEncodeOption,
    var errorMessage: String = "",
)

class MyViewModel : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _taskCount = MutableStateFlow(0)
    val taskCount: StateFlow<Int> = _taskCount

    private val _totalTaskCount = MutableStateFlow(0)
    val totalTaskCount: StateFlow<Int> = _totalTaskCount

    private val _bothCount = MutableStateFlow(0)
    val bothCount: StateFlow<Int> = _bothCount

    private val _audioOnlyCount = MutableStateFlow(0)
    val audioOnlyCount: StateFlow<Int> = _audioOnlyCount

    private val _videoOnlyCount = MutableStateFlow(0)
    val videoOnlyCount: StateFlow<Int> = _videoOnlyCount

    private val _noneCount = MutableStateFlow(0)
    val noneCount: StateFlow<Int> = _noneCount

    private val _errorCount = MutableStateFlow(0)
    val errorCount: StateFlow<Int> = _errorCount

    val allCount: StateFlow<Int> = combine(bothCount, audioOnlyCount, videoOnlyCount, noneCount) { bothCount, audioOnlyCount, videoOnlyCount, noneCount ->
        bothCount + audioOnlyCount + videoOnlyCount + noneCount
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    val successCount: StateFlow<Int> = combine(allCount, errorCount, noneCount) { allCount, errorCount, noneCount ->
        allCount - errorCount - noneCount
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    private val _activeMessages = MutableStateFlow<List<ConvertInfo>>(emptyList())
    val activeMessages: StateFlow<List<ConvertInfo>> = _activeMessages

    private val _errorMessages = MutableStateFlow<List<ConvertInfo>>(emptyList())
    val errorMessages: StateFlow<List<ConvertInfo>> = _errorMessages

    fun updateTaskCount(count:Int) {
        _taskCount.value = count
    }

    fun incrementTotalTaskCount(): Int {
        _totalTaskCount.value += 1
        return _totalTaskCount.value
    }

    fun incrementBothCount() {
        _bothCount.value += 1
    }

    fun incrementAudioOnlyCount() {
        _audioOnlyCount.value += 1
    }

    fun incrementVideoOnlyCount() {
        _videoOnlyCount.value += 1
    }

    fun incrementNoneCount() {
        _noneCount.value += 1
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
