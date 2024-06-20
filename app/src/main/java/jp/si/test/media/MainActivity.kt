package jp.si.test.media

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val mediaConverter = MediaConverter()
    private lateinit var videoDir: File
    private lateinit var outputDir: File
    private val maxTasks = 1

    private var totalFiles = 0
    private var bothCount = 0
    private var audioOnlyCount = 0
    private var videoOnlyCount = 0
    private var errorCount = 0

    private val errorMessages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        videoDir = File(filesDir, "")
        outputDir = File(filesDir, "out")

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        setContent {
            MyApp()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("CoroutineCreationDuringComposition", "UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun MyApp() {
        var isRunning by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        Scaffold {
            Column(modifier = Modifier.padding(16.dp)) {
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
                ProgressSection()
                ErrorSection()
            }

            if (isRunning) {
                scope.launch {
                    processFiles()
                }
            }
        }
    }

    @Composable
    fun ProgressSection() {
        Text("Total Files: $totalFiles")
        Text("Both (Audio & Video): $bothCount")
        Text("Audio Only: $audioOnlyCount")
        Text("Video Only: $videoOnlyCount")
        Text("Errors: $errorCount")
    }

    @Composable
    fun ErrorSection() {
        LazyColumn {
            items(errorMessages.size) { index ->
                Text(errorMessages[index])
            }
        }
    }

    private suspend fun processFiles() {
        withContext(Dispatchers.IO) {
            val files = videoDir.listFiles { _, name -> name.endsWith(".mp4") }?.toList() ?: listOf()
            totalFiles = files.size
            var fileIndex = 0
            val tasks = mutableListOf<Job>()

            while (true) {
                if (!isActive) break

                if (tasks.size < maxTasks && files.isNotEmpty()) {
                    val file = files[fileIndex % totalFiles]
                    val outputFileName = generateOutputFileName(file.name)
                    val job = launch {
                        try {
                            mediaConverter.convert(
                                file.absolutePath,
                                outputDir.resolve("$outputFileName.aac").absolutePath,
                                outputDir.resolve("$outputFileName.mp4").absolutePath
                            )
                            updateCounts(file)
                        } catch (e: Exception) {
                            errorMessages.add("Error processing ${file.name}: ${e.message}")
                            errorCount++
                            delay(100) // Adjust the delay as neede
                        }
                    }
                    tasks.add(job)
                    fileIndex++
                } else {
                    delay(1000) // Adjust the delay as needed
                }

                Log.d("XXXXX", "$fileIndex")

                tasks.removeAll { it.isCompleted }
            }
        }
    }

    private fun generateOutputFileName(inputFileName: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${inputFileName}_$timestamp"
    }

    private fun updateCounts(file: File) {
        val hasAudio = mediaConverter.hasTrack(file.absolutePath, "audio/")
        val hasVideo = mediaConverter.hasTrack(file.absolutePath, "video/")
        when {
            hasAudio && hasVideo -> bothCount++
            hasAudio -> audioOnlyCount++
            hasVideo -> videoOnlyCount++
        }
    }
}
