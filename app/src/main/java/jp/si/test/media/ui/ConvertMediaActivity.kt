package jp.si.test.media.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel

class ConvertMediaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val convertMediaViewModel: ConvertMediaViewModel = viewModel()
            convertMediaViewModel.initialize(filesDir)
            ConvertMedia(convertMediaViewModel)
        }
    }
}
