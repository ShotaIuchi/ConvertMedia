package jp.si.test.media

import android.annotation.SuppressLint
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*

class MainActivity : ComponentActivity() {
    private val mediaConverter = MediaConverter()
    private lateinit var videoDir: File
    private lateinit var outputDir: File
    private val maxTasks = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        videoDir = File(filesDir, "")
        outputDir = File(filesDir, "out")

        setContent {
            MyApp()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
    @Composable
    fun MyApp() {
        val viewModel: MyViewModel = viewModel()
        var isRunning by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        var selectedAudioCodec by remember { mutableStateOf("mp4a-latm") }
        var selectedVideoCodec by remember { mutableStateOf("avc") }

        val drawerState = rememberDrawerState(DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Audio Codec")
                        CodecSelection(
                            options = listOf("mp4a-latm", "aac", "opus"),
                            selectedCodec = selectedAudioCodec,
                            onCodecSelected = { selectedAudioCodec = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select Video Codec")
                        CodecSelection(
                            options = listOf("avc", "hevc", "vp9"),
                            selectedCodec = selectedVideoCodec,
                            onCodecSelected = { selectedVideoCodec = it }
                        )
                    }
                }
            },
            drawerState = drawerState
        ) {
            Scaffold {
                Column(modifier = Modifier.padding(2.dp)) {
                    Row {
                        Button(
                            shape = RoundedCornerShape(4.dp),
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Text("Codec")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            shape = RoundedCornerShape(4.dp),
                            onClick = { isRunning = true },
                            enabled = !isRunning
                        ) {
                            Text("Start")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            shape = RoundedCornerShape(4.dp),
                            onClick = { isRunning = false },
                            enabled = isRunning
                        ) {
                            Text("Stop")
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    ProgressSection(viewModel)
                    Spacer(modifier = Modifier.height(2.dp))
                    ActiveSection(viewModel)
                    Spacer(modifier = Modifier.height(2.dp))
                    ErrorSection(viewModel)
                }
            }
            LaunchedEffect(isRunning) {
                if (isRunning) {
                    scope.launch {
                        processFiles(viewModel, selectedAudioCodec, selectedVideoCodec) { isRunning }
                    }
                }
            }
        }
    }

    @Composable
    fun CodecSelection(options: List<String>, selectedCodec: String, onCodecSelected: (String) -> Unit) {
        Column {
            options.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedCodec == option,
                        onClick = { onCodecSelected(option) }
                    )
                    Text(option)
                }
            }
        }
    }

    @Composable
    fun ProgressSection(viewModel: MyViewModel) {
        val taskCount by viewModel.taskCount.collectAsState()
        val totalCount by viewModel.allCount.collectAsState()
        val bothCount by viewModel.bothCount.collectAsState()
        val audioOnlyCount by viewModel.audioOnlyCount.collectAsState()
        val videoOnlyCount by viewModel.videoOnlyCount.collectAsState()
        val errorCount by viewModel.errorCount.collectAsState()

        Card(
            modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth(),
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(1.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("実行: $taskCount", style = MaterialTheme.typography.bodySmall)
                Text("合計: $totalCount", style = MaterialTheme.typography.bodySmall)
                Text("音動: $bothCount", style = MaterialTheme.typography.bodySmall)
                Text("音声: $audioOnlyCount", style = MaterialTheme.typography.bodySmall)
                Text("動画: $videoOnlyCount", style = MaterialTheme.typography.bodySmall)
                Text("失敗: $errorCount", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    @Composable
    fun ActiveSection(viewModel: MyViewModel) {
        val activeMessages by viewModel.activeMessages.collectAsState()

        Box(Modifier.border(border = BorderStroke(1.dp, Color.Blue))) {
            LazyColumn(Modifier.padding(1.dp)) {
                items(activeMessages.size) { index ->
                    Card(
                        modifier = Modifier
                            .padding(1.dp)
                            .fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.Black),
                        shape = RoundedCornerShape(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            val message = activeMessages[index]
                            Text(
                                "(${message.id}):${message.fileName}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Audio: ${activeMessages[index].audioCodec} --> ${activeMessages[index].targetAudioCodec}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Video: ${activeMessages[index].videoCodec} --> ${activeMessages[index].targetVideoCodec}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ErrorSection(viewModel: MyViewModel) {
        val errorMessages by viewModel.errorMessages.collectAsState()

        Box(Modifier.border(border = BorderStroke(1.dp, Color.Red))) {
            LazyColumn(Modifier.padding(1.dp)) {
                items(errorMessages.size) { index ->
                    Card(
                        modifier = Modifier
                            .padding(1.dp)
                            .fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.Black),
                        shape = RoundedCornerShape(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            val message = errorMessages[index]
                            Text(
                                "(${message.id}):${message.fileName}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Audio: ${message.audioCodec} --> ${message.targetAudioCodec}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Video: ${message.videoCodec} --> ${message.targetVideoCodec}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                message.errorMessage,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun processFiles(viewModel: MyViewModel, audioCodec: String, videoCodec: String, isRunning: () -> Boolean) {
        val tasks = mutableListOf<Job>()
        try {
            withContext(Dispatchers.IO) {
                val files = videoDir.listFiles { _, name -> name.endsWith(".mp4") }?.toList() ?: listOf()
                var fileIndex = 0

                while (isRunning()) {
                    if (tasks.size < maxTasks && files.isNotEmpty()) {
                        val file = files[fileIndex % files.size]
                        val job = launch {
                            val index = viewModel.incrementTotalCount()
                            val outputFileName = generateOutputFileName(file.name, index)
                            val convertInfo = ConvertInfo(
                                index,
                                file.name,
                                audioCodec = getCodecInfo(file.absolutePath, "audio/"),
                                videoCodec = getCodecInfo(file.absolutePath, "video/"),
                                targetAudioCodec = audioCodec,
                                targetVideoCodec = videoCodec,
                            )
                            try {
                                viewModel.addActiveMessage(convertInfo)
                                mediaConverter.convert(
                                    file.absolutePath,
                                    outputDir.resolve("$outputFileName.aac").absolutePath,
                                    outputDir.resolve("$outputFileName.avc").absolutePath,
                                    EncodeOption(convertInfo.targetAudioCodec),
                                    EncodeOption(convertInfo.targetVideoCodec),
                                )
                            } catch (e: Exception) {
                                convertInfo.errorMessage = e.message ?: e.javaClass.name
                                viewModel.addErrorMessage(convertInfo)
                                viewModel.incrementErrorCount()
                            } finally {
                                viewModel.removeActiveMassage(index)
                                updateCounts(file, viewModel)
                            }
                        }
                        tasks.add(job)
                        fileIndex++
                    }
                    tasks.removeAll { it.isCompleted }
                    viewModel.updateTaskCount(tasks.size)
                    delay(100)
                }
            }
        } finally {
            tasks.forEach { it.cancelAndJoin() }
        }
    }

    private fun generateOutputFileName(inputFileName: String, index: Int): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${inputFileName}_${timestamp}_${index}"
    }

    private fun updateCounts(file: File, viewModel: MyViewModel) {
        val hasAudio = mediaConverter.hasTrack(file.absolutePath, "audio/")
        val hasVideo = mediaConverter.hasTrack(file.absolutePath, "video/")
        when {
            hasAudio && hasVideo -> viewModel.incrementBothCount()
            hasAudio -> viewModel.incrementAudioOnlyCount()
            hasVideo -> viewModel.incrementVideoOnlyCount()
        }
    }

    private fun getCodecInfo(filePath: String, mimePrefix: String): String {
        val extractor = MediaExtractor()
        extractor.setDataSource(filePath)
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith(mimePrefix)) {
                extractor.release()
                return mime.split("/").getOrNull(1) ?: ""
            }
        }
        extractor.release()
        return ""
    }
}
