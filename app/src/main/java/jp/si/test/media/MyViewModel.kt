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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import java.util.Date

data class ConvertInfo(
    val id: Int,
    val fileName: String,
    val audioCodec: String,
    val videoCodec: String,
    val audioEncodeOption: AudioEncodeOption,
    val videoEncodeOption: VideoEncodeOption,
    var errorMessage: String = "",
) {
    val hash = "$fileName,$audioCodec,${audioEncodeOption.name},$videoCodec,${videoEncodeOption.name},$errorMessage".hashCode()
}

data class MergedConvertInfo(
    var updateTime: Date,
    var count: Int,
    val convertInfo: ConvertInfo,
)



class MyViewModel : ViewModel() {
    private val mutex = Mutex()

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

    val allCount: StateFlow<Int> = combine(
        bothCount,
        audioOnlyCount,
        videoOnlyCount,
        noneCount
    ) { bothCount, audioOnlyCount, videoOnlyCount, noneCount ->
        bothCount + audioOnlyCount + videoOnlyCount + noneCount
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    val successCount: StateFlow<Int> =
        combine(allCount, errorCount, noneCount) { allCount, errorCount, noneCount ->
            allCount - errorCount - noneCount
        }.stateIn(scope, SharingStarted.Eagerly, 0)

    private val _activeMessages = MutableStateFlow<List<ConvertInfo>>(emptyList())
    val activeMessages: StateFlow<List<ConvertInfo>> = _activeMessages

    private val _errorMessages = MutableStateFlow<MutableList<Pair<Int, MergedConvertInfo>>>(mutableListOf())
    val errorMessages: StateFlow<MutableList<Pair<Int, MergedConvertInfo>>> = _errorMessages

    suspend fun updateTaskCount(count: Int) = mutex.lock {
        _taskCount.value = count
    }

    suspend fun incrementTotalTaskCount(): Any = mutex.lock {
        _totalTaskCount.value += 1
        return@lock _totalTaskCount.value
    }

    suspend fun incrementBothCount() = mutex.lock {
        _bothCount.value += 1
    }

    suspend fun incrementAudioOnlyCount() = mutex.lock {
        _audioOnlyCount.value += 1
    }

    suspend fun incrementVideoOnlyCount() = mutex.lock {
        _videoOnlyCount.value += 1
    }

    suspend fun incrementNoneCount() = mutex.lock {
        _noneCount.value += 1
    }

    suspend fun incrementErrorCount() = mutex.lock {
        _errorCount.value += 1
    }

    suspend fun addActiveMessage(activeInfo: ConvertInfo) = mutex.lock {
        _activeMessages.value = _activeMessages.value + activeInfo
    }

    suspend fun removeActiveMassage(activeInfoId: Int) = mutex.lock {
        _activeMessages.value = _activeMessages.value.filter { it.id != activeInfoId }
    }

    suspend fun addErrorMessage(errorInfo: ConvertInfo) = mutex.lock {
        for (msg in _errorMessages.value) {
            if (msg.first == errorInfo.hash) {
                msg.second.let {
                    it.count += 1
                    it.updateTime = Date()
                }
                return@lock
            }
        }
        _errorMessages.value = (_errorMessages.value + Pair(
            errorInfo.hash, MergedConvertInfo(
                count = 1,
                updateTime = Date(),
                convertInfo = errorInfo
            )
        )).toMutableList()
    }
}
