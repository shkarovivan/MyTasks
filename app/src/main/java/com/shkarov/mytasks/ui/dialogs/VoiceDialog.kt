package com.shkarov.mytasks.ui.dialogs

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.VoiceRequestType
import com.shkarov.mytasks.viewmodels.SpeechRecognitionViewModel

@Composable
fun VoiceDialog(
    showDialog: Boolean,
    requestType: VoiceRequestType,
    onDismiss: (String) -> Unit
) {
    val viewModel: SpeechRecognitionViewModel = hiltViewModel()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val status by viewModel.loadProgress.collectAsState()
    if (showDialog) {
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.startRecord()
            }
        }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        Dialog(
            onDismissRequest = {
                viewModel.stopRecognition()
                onDismiss("")
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = recognizedText,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            enabled = status,
                            onClick = {
                            viewModel.startRecord()
                        }) {
                            Text(stringResource(id = R.string.record_button_text))
                        }
                        Button(
                            enabled = status,
                            onClick = {
                            viewModel.stopRecognition()
                            onDismiss(recognizedText)
                        }) {
                            Text(
                                text = stringResource(
                                    id = when (requestType) {
                                        VoiceRequestType.ADD_TASK -> R.string.save_button_text
                                        VoiceRequestType.SEARCH,
                                        VoiceRequestType.UNKNOWN -> R.string.send_button_text
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
