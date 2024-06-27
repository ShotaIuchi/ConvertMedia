package jp.si.test.media

import android.provider.ContactsContract.Directory
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConvertInfo(
    val id: Int,
    val inputFile: File,
    val audioInputCodec: String,
    val videoInputCodec: String,
    val audioOutputFile: File,
    val videoOutputFile: File,
    val audioEncodeOption: AudioEncodeOption,
    val videoEncodeOption: VideoEncodeOption,
    var errorMessage: String = "",
)

class MyViewModel : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val mutex = Mutex()

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

    private fun addActiveMessage(convertInfo: ConvertInfo) {
        _activeMessages.value = _activeMessages.value + convertInfo
    }

    private fun removeActiveMassage(convertInfo: ConvertInfo) {
        _activeMessages.value = _activeMessages.value.filter { it.id != convertInfo.id }
    }

    private fun addErrorMessage(convertInfo: ConvertInfo) {
        _errorMessages.value = _errorMessages.value + convertInfo
    }

    private val mediaConverter = MediaConverter()

    private var srcFiles: List<File> = listOf()
    private var outDirectory: File? = null
    private var convertIndex = 0

    fun initialize(cwd: File) {
        val fCwd = File(cwd, "")
        this.srcFiles = fCwd.listFiles { _, name -> name.endsWith(".mp4") }?.toList() ?: listOf()
        this.outDirectory = File(cwd, "out")
    }

    private fun nextFile(): ConvertInfo? {
        if (srcFiles.isEmpty()) {
            return null
        }
        if (outDirectory == null) {
            return null
        }
        val fileIndex = convertIndex % srcFiles.size
        if (fileIndex >= srcFiles.size) {
            return null
        }

        val inputFile = srcFiles[fileIndex]
        val audio = AudioEncodeOptionAAC()
        val video = VideoEncodeOptionAVC()
        val convert = ConvertInfo(
            convertIndex,
            inputFile,
            mediaConverter.getCodecInfo(inputFile, "audio/"),
            mediaConverter.getCodecInfo(inputFile, "video/"),
            generateOutputFile(outDirectory!!, inputFile, "audio/", convertIndex),
            generateOutputFile(outDirectory!!, inputFile, "video/", convertIndex),
            audio,
            video,
        )
        convertIndex++
        return convert
    }

    fun convert() {
        nextFile()?.let {
            try {
                addActiveMessage(it)
                mediaConverter.convert(
                    it.inputFile,
                    it.audioOutputFile,
                    it.videoOutputFile,
                    it.audioEncodeOption,
                    it.videoEncodeOption,
                )
            } catch (e: Exception) {
                it.errorMessage = e.toString()
                addErrorMessage(it)
                _errorCount.value += 1
            } finally {
                removeActiveMassage(it)
                updateCounts(it)
            }
        }
    }


    private fun generateOutputFile(directory: File, inputFile: File, mimePrefix: String, index: Int): File {
        val codec = mediaConverter.getCodecName(inputFile, mimePrefix)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm_ss", Locale.getDefault()).format(Date())
        return File(directory, "[$timestamp]_($index)_${inputFile.name}_($codec)")
    }

    private fun updateCounts(convertInfo: ConvertInfo) {
        val hasAudio = convertInfo.audioInputCodec.isNotEmpty()
        val hasVideo = convertInfo.videoInputCodec.isNotEmpty()
        when {
            hasAudio && hasVideo -> _bothCount.value++
            hasAudio -> _audioOnlyCount.value++
            hasVideo -> _videoOnlyCount.value++
            else -> _videoOnlyCount.value++
        }
    }
}
