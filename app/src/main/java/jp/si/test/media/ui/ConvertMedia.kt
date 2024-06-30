package jp.si.test.media.ui

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
import kotlinx.coroutines.flow.StateFlow
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertMedia(convertMediaViewModel: ConvertMediaViewModel) {
    val isRunning by convertMediaViewModel.isRunning.collectAsState()

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
        Scaffold { paddingValues ->
            BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
                val isPortrait = maxWidth < maxHeight
                if (isPortrait) {
                    VerticalLayout(
                        viewModel = convertMediaViewModel,
                        drawerState = drawerState,
                        scope = scope,
                        isRunning = isRunning,
                        setRunning = { scope.launch { convertMediaViewModel.toggleRunning() } }
                    )
                } else {
                    HorizontalLayout(
                        viewModel = convertMediaViewModel,
                        drawerState = drawerState,
                        scope = scope,
                        isRunning = isRunning,
                        setRunning = { scope.launch { convertMediaViewModel.toggleRunning() } }
                    )
                }
            }
        }
        LaunchedEffect(isRunning) {
            if (isRunning) {
                scope.launch {
                    convertMediaViewModel.processFiles()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalLayout(
    viewModel: ConvertMediaViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope,
    isRunning: Boolean,
    setRunning: (Boolean) -> Unit
) {
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
                onClick = { setRunning(true) },
                enabled = !isRunning
            ) {
                Text("Start")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                shape = RoundedCornerShape(4.dp),
                onClick = { setRunning(false) },
                enabled = isRunning
            ) {
                Text("Stop")
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        ProgressSection(viewModel)
        Spacer(modifier = Modifier.height(2.dp))
        Message(viewModel.activeMessages, border = BorderStroke(1.dp, Color.Blue))
        Spacer(modifier = Modifier.height(2.dp))
        Message(viewModel.errorMessages, border = BorderStroke(1.dp, Color.Red))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalLayout(
    viewModel: ConvertMediaViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope,
    isRunning: Boolean,
    setRunning: (Boolean) -> Unit
) {
    Row(modifier = Modifier.padding(2.dp)) {
        Column(modifier = Modifier.weight(1f).padding(2.dp)) {
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
                    onClick = { setRunning(true) },
                    enabled = !isRunning
                ) {
                    Text("Start")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    shape = RoundedCornerShape(4.dp),
                    onClick = { setRunning(false) },
                    enabled = isRunning
                ) {
                    Text("Stop")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    shape = RoundedCornerShape(4.dp),
                    onClick = { scope.launch { viewModel.clear() } },
                ) {
                    Text("Clear")
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            ProgressSection(viewModel)
            Spacer(modifier = Modifier.height(2.dp))
            Message(viewModel.activeMessages, border = BorderStroke(1.dp, Color.Blue))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f).padding(2.dp)) {
            Message(viewModel.errorMessages, border = BorderStroke(1.dp, Color.Red))
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
fun ProgressSection(viewModel: ConvertMediaViewModel) {
    val taskCount by viewModel.taskCount.collectAsState()
    val allCount by viewModel.allCount.collectAsState()
    val bothCount by viewModel.bothCount.collectAsState()
    val audioOnlyCount by viewModel.audioOnlyCount.collectAsState()
    val videoOnlyCount by viewModel.videoOnlyCount.collectAsState()
    val noneCount by viewModel.noneCount.collectAsState()
    val successCount by viewModel.successCount.collectAsState()
    val errorCount by viewModel.errorCount.collectAsState()

    Card(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxWidth(),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(1.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("OK: $successCount, NG: $errorCount, none: $noneCount", style = MaterialTheme.typography.bodySmall)
            Text("active: $taskCount, all: $allCount, both: $bothCount, audio: $audioOnlyCount, video: $videoOnlyCount, none: $noneCount", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun Message(stateMessages: StateFlow<List<ConvertInfo>>, border: BorderStroke) {
    val messages by stateMessages.collectAsState()

    Box(Modifier.border(border)) {
        LazyColumn(Modifier.padding(1.dp)) {
            items(messages.size) { index ->
                Card(
                    modifier = Modifier
                        .padding(1.dp)
                        .fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(1.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        val message = messages[index]
                        Text(
                            "${message.id}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "${message.inputFile}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Audio: ${message.audioInputCodec} --> ${message.audioEncodeOption.mime}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Video: ${message.videoInputCodec} --> ${message.videoEncodeOption.mime}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (message.errorMessage.isNotEmpty()) {
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
}