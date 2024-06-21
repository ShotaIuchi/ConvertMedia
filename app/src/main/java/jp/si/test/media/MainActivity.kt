package jp.si.test.media

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
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

        Scaffold() {
            Column(modifier = Modifier.padding(4.dp)) {
                Row {
                    Button(
                        onClick = { isRunning = true },
                        enabled = !isRunning
                    ) {
                        Text("Start")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { isRunning = false },
                        enabled = isRunning
                    ) {
                        Text("Stop")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ProgressSection(viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                ErrorSection(viewModel)
            }

            if (isRunning) {
                scope.launch {
                    processFiles(viewModel)
                }
            }
        }
    }

    @Composable
    fun ProgressSection(viewModel: MyViewModel) {
        val totalFiles by viewModel.totalFiles.collectAsState()
        val bothCount by viewModel.bothCount.collectAsState()
        val audioOnlyCount by viewModel.audioOnlyCount.collectAsState()
        val videoOnlyCount by viewModel.videoOnlyCount.collectAsState()
        val errorCount by viewModel.errorCount.collectAsState()

        Column {
            Text("Total Files: $totalFiles")
            Text("Both (Audio & Video): $bothCount")
            Text("Audio Only: $audioOnlyCount")
            Text("Video Only: $videoOnlyCount")
            Text("Errors: $errorCount")
        }
    }

    @Composable
    fun ErrorSection(viewModel: MyViewModel) {
        val errorMessages by viewModel.errorMessages.collectAsState()

        LazyColumn {
            items(errorMessages.size) { index ->
                Card(
                    modifier = Modifier
                        .padding(1.dp)
                        .fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(1.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp),) {
                        Text(errorMessages[index], style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }


    private suspend fun processFiles(viewModel: MyViewModel) {
        withContext(Dispatchers.IO) {
            val files = videoDir.listFiles { _, name -> name.endsWith(".mp4") }?.toList() ?: listOf()
            viewModel.updateTotalFiles(files.size)
            var fileIndex = 0
            val tasks = mutableListOf<Job>()

            while (true) {
                if (!isActive) break

                if (tasks.size < maxTasks && files.isNotEmpty()) {
                    val file = files[fileIndex % files.size]
                    val outputFileName = generateOutputFileName(file.name)
                    val job = launch {
                        try {
                            mediaConverter.convert(
                                file.absolutePath,
                                outputDir.resolve("$outputFileName.aac").absolutePath,
                                outputDir.resolve("$outputFileName.mp4").absolutePath
                            )
                            updateCounts(file, viewModel)
                        } catch (e: Exception) {
                            viewModel.addErrorMessage("${file.name}: ${e.message}")
                            viewModel.incrementErrorCount()
                        }
                    }
                    tasks.add(job)
                    fileIndex++
                }

                tasks.removeAll { it.isCompleted }
                delay(1000) // Adjust the delay as needed
            }
        }
    }

    private fun generateOutputFileName(inputFileName: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${inputFileName}_$timestamp"
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
}