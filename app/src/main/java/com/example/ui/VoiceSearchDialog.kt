package com.example.ui

import android.speech.SpeechRecognizer
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

@Composable
fun VoiceSearchDialog(
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data?.getStringArrayListExtra(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!data.isNullOrEmpty()) {
            onResult(data[0])
        }
        onDismiss()
    }

    LaunchedEffect(Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        launcher.launch(intent)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Listening...") },
        text = { Text("Speak product name...") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
