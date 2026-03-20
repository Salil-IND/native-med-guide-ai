```kotlin
package com.medguide.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medguide.ai.services.MedModelService
import com.medguide.ai.ui.theme.EmergencyRed
import com.medguide.ai.ui.theme.MedBlue
import com.medguide.ai.ui.theme.SafeGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSetupScreen(
    onNavigateToHome: () -> Unit,
    modelService: MedModelService
) {

    val allLoaded =
        modelService.isLLMLoaded &&
        modelService.isSTTLoaded &&
        modelService.isTTSLoaded

    // 🔹 Automatically go to Home once models are ready
    LaunchedEffect(allLoaded) {
        if (allLoaded) {
            delay(800)
            onNavigateToHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ AI Model Setup", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Status banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (allLoaded) Color(0xFFE8F5E9)
                        else Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        if (allLoaded)
                            "✅ All models loaded — App is ready!"
                        else
                            "📥 Download required (one-time, ~500MB)",
                        fontWeight = FontWeight.Bold,
                        color =
                            if (allLoaded) SafeGreen
                            else Color(0xFFE65100)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Models are stored on-device. Once downloaded the app works completely offline — even without internet during emergencies.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // LLM
            ModelCard(
                title = "🧠 Medical AI Brain (LLM)",
                description = "Powers symptom checking, voice assistant, drug queries, and first aid guidance.",
                size = "~400 MB",
                model = "SmolLM2 360M Instruct",
                isLoaded = modelService.isLLMLoaded,
                isLoading = modelService.isLLMLoading,
                isDownloading = modelService.isLLMDownloading,
                progress = modelService.llmDownloadProgress,
                onLoad = { modelService.downloadAndLoadLLM() }
            )

            // STT
            ModelCard(
                title = "🎤 Speech Recognition (STT)",
                description = "Converts your voice to text for hands-free emergency use.",
                size = "~75 MB",
                model = "Whisper Tiny (Sherpa-ONNX)",
                isLoaded = modelService.isSTTLoaded,
                isLoading = modelService.isSTTLoading,
                isDownloading = modelService.isSTTDownloading,
                progress = modelService.sttDownloadProgress,
                onLoad = { modelService.downloadAndLoadSTT() }
            )

            // TTS
            ModelCard(
                title = "🔊 Voice Response (TTS)",
                description = "Reads medical guidance aloud — useful when hands are occupied.",
                size = "~20 MB",
                model = "Piper TTS (US English)",
                isLoaded = modelService.isTTSLoaded,
                isLoading = modelService.isTTSLoading,
                isDownloading = modelService.isTTSDownloading,
                progress = modelService.ttsDownloadProgress,
                onLoad = { modelService.downloadAndLoadTTS() }
            )

            // Download all models
            if (!allLoaded) {
                Button(
                    onClick = { modelService.downloadAndLoadAllModels() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled =
                        !modelService.isLLMDownloading &&
                        !modelService.isSTTDownloading &&
                        !modelService.isTTSDownloading &&
                        !modelService.isLLMLoading &&
                        !modelService.isSTTLoading &&
                        !modelService.isTTSLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download All Models (Recommended)", fontSize = 15.sp)
                }
            }

            // Error display
            modelService.errorMessage?.let { message ->

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {

                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            "❌ $message",
                            color = EmergencyRed,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(onClick = { modelService.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            // Info table
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        "What each model enables:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "🧠 LLM → Voice Assistant, Symptom Checker, Drug AI, First Aid AI",
                        "🎤 STT → Hands-free voice input",
                        "🔊 TTS → Spoken responses",
                        "📋 Emergency Protocols → Work offline without models"
                    ).forEach {

                        Text(
                            it,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
```
