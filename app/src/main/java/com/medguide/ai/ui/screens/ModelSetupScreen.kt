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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSetupScreen(
    onNavigateBack: () -> Unit,
    modelService: MedModelService
) {
    val allLoaded = modelService.isLLMLoaded && modelService.isSTTLoaded && modelService.isTTSLoaded

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ AI Model Setup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    containerColor = if (allLoaded) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (allLoaded) "✅ All models loaded — App is ready!"
                        else "📥 Download required (one-time, ~500MB)",
                        fontWeight = FontWeight.Bold,
                        color = if (allLoaded) SafeGreen else Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Models are stored on-device. Once downloaded, the entire app works completely offline — even without internet during emergencies.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // LLM Card
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

            // STT Card
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

            // TTS Card
            ModelCard(
                title = "🔊 Voice Response (TTS)",
                description = "Reads medical guidance aloud — essential when hands are occupied in emergencies.",
                size = "~20 MB",
                model = "Piper TTS (US English)",
                isLoaded = modelService.isTTSLoaded,
                isLoading = modelService.isTTSLoading,
                isDownloading = modelService.isTTSDownloading,
                progress = modelService.ttsDownloadProgress,
                onLoad = { modelService.downloadAndLoadTTS() }
            )

            // Download all button
            if (!allLoaded) {
                Button(
                    onClick = { modelService.downloadAndLoadAllModels() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !modelService.isLLMDownloading && !modelService.isSTTDownloading && !modelService.isTTSDownloading,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download All Models (Recommended)", fontSize = 15.sp)
                }
            }

            // Error display
            if (modelService.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "❌ ${modelService.errorMessage}",
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

            // Feature table
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("What each model enables:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "🧠 LLM → Voice Assistant, Symptom Checker, Drug AI, First Aid AI",
                        "🎤 STT → Hands-free voice input (speak instead of type)",
                        "🔊 TTS → Spoken responses (hear guidance without reading)",
                        "📋 Emergency Protocols → Work offline without ANY models"
                    ).forEach { item ->
                        Text(item, fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ModelCard(
    title: String,
    description: String,
    size: String,
    model: String,
    isLoaded: Boolean,
    isLoading: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onLoad: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLoaded) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(model, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isLoaded) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Loaded", tint = SafeGreen, modifier = Modifier.size(28.dp))
                } else {
                    Text(size, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 19.sp)

            when {
                isDownloading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Downloading: ${(progress * 100).toInt()}%", fontSize = 12.sp, color = MedBlue, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth(), color = MedBlue)
                }
                isLoading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MedBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading model into memory...", fontSize = 12.sp, color = MedBlue)
                    }
                }
                !isLoaded -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(onClick = onLoad, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download & Load ($size)")
                    }
                }
                else -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("✅ Ready to use", fontSize = 12.sp, color = SafeGreen, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}