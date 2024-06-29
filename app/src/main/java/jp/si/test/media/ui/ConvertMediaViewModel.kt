package jp.si.test.media.ui

import androidx.lifecycle.ViewModel
import jp.si.test.media.converter.MediaConverter
import jp.si.test.media.converter.MediaType
import jp.si.test.media.converter.audio.AudioEncodeOption
import jp.si.test.media.converter.audio.AudioEncodeOptionAAC
import jp.si.test.media.converter.getCodecInfo
import jp.si.test.media.converter.getCodecName
import jp.si.test.media.converter.video.VideoEncodeOption
import jp.si.test.media.converter.video.VideoEncodeOptionAVC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat

import java.util.Date
import java.util.Locale

data class ConvertInfo(
    var id: List<Int> = mutableListOf(),
    val inputFile: File,
    val audioInputCodec: String,
    val videoInputCodec: String,
    val audioOutputFile: File,
    val videoOutputFile: File,
    val audioEncodeOption: AudioEncodeOption,
    val videoEncodeOption: VideoEncodeOption,
    var errorMessage: String = "",
) {
    override fun hashCode(): Int {
        return (inputFile.absolutePath + audioInputCodec + videoInputCodec + audioEncodeOption.mime + videoEncodeOption.mime + errorMessage).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return this.hashCode() == other.hashCode()
    }
}

class ConvertMediaViewModel : ViewModel() {
    private val maxTasks = 5

    private val scope = CoroutineScope(Dispatchers.Default)

    private val mutex = Mutex()

    private val tasks = mutableListOf<Job>()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

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

    private val _activeMessages = MutableStateFlow<List<ConvertInfo>>(emptyList())
    val activeMessages: StateFlow<List<ConvertInfo>> = _activeMessages

    private val _errorMessages = MutableStateFlow<List<ConvertInfo>>(emptyList())
    val errorMessages: StateFlow<List<ConvertInfo>> = _errorMessages

    val errorCount: StateFlow<Int> = errorMessages
        .map { it.size }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    val allCount: StateFlow<Int> = combine(bothCount, audioOnlyCount, videoOnlyCount, noneCount) {
            bothCount, audioOnlyCount, videoOnlyCount, noneCount -> bothCount + audioOnlyCount + videoOnlyCount + noneCount }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    val successCount: StateFlow<Int> = combine(allCount, errorCount, noneCount) {
            allCount, errorCount, noneCount -> allCount - errorCount - noneCount }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    fun toggleRunning() {
        _isRunning.value = !_isRunning.value
    }

    private suspend fun updateTaskCount(count:Int) = mutex.withLock {
        _taskCount.value = count
    }

    private suspend fun addActiveMessage(convertInfo: ConvertInfo) = mutex.withLock {
        _activeMessages.value = _activeMessages.value + convertInfo
    }

    private suspend fun removeActiveMassage(convertInfo: ConvertInfo) = mutex.withLock {
        _activeMessages.value = _activeMessages.value.filter { it.id[0] != convertInfo.id[0] }
    }

    private suspend fun addErrorMessage(convertInfo: ConvertInfo) = mutex.withLock {
        val convertInfoHash = convertInfo.hashCode()
        for (v in _errorMessages.value) {
            if (v.hashCode() == convertInfoHash) {
                v.id = v.id + convertInfo.id
                val tmp = _errorMessages.value.toMutableList()
                _errorMessages.value = mutableListOf()
                _errorMessages.value = tmp
                return@withLock
            }
        }
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
            mutableListOf(convertIndex),
            inputFile,
            getCodecInfo(inputFile, MediaType.AUDIO),
            getCodecInfo(inputFile, MediaType.VIDEO),
            generateOutputFile(outDirectory!!, inputFile, MediaType.AUDIO, convertIndex),
            generateOutputFile(outDirectory!!, inputFile, MediaType.VIDEO, convertIndex),
            audio,
            video,
        )
        convertIndex++
        return convert
    }


    suspend fun processFiles() {
        try {
            withContext(Dispatchers.IO) {
                while (isRunning.value || (tasks.size > 0)) {
                    if (isRunning.value) {
                        if (tasks.size < maxTasks) {
                            val job = launch {
                                convert()
                            }
                            tasks.add(job)
                        }
                    }
                    tasks.removeAll {
                        it.isCompleted
                    }
                    updateTaskCount(tasks.size)
                    delay(100)
                }
            }
        } finally {
            tasks.forEach { it.cancelAndJoin() }
        }
    }

    private suspend fun convert() {
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
            } finally {
                removeActiveMassage(it)
                updateCounts(it)
            }
        }
    }

    private fun generateOutputFile(directory: File, inputFile: File, mediaType: MediaType, index: Int): File {
        val codec = getCodecName(inputFile, mediaType)
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
