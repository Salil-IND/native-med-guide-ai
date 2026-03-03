package com.runanywhere.kotlin_starter_example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.kotlin_starter_example.services.ModelService
import com.runanywhere.kotlin_starter_example.ui.components.ModelLoaderWidget
import com.runanywhere.kotlin_starter_example.ui.theme.AccentCyan
import com.runanywhere.kotlin_starter_example.ui.theme.AccentViolet
import com.runanywhere.kotlin_starter_example.ui.theme.PrimaryDark
import com.runanywhere.kotlin_starter_example.ui.theme.PrimaryMid
import com.runanywhere.kotlin_starter_example.ui.theme.SurfaceCard
import com.runanywhere.kotlin_starter_example.ui.theme.TextMuted
import com.runanywhere.kotlin_starter_example.ui.theme.TextPrimary
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.chat
import com.runanywhere.sdk.public.extensions.VoiceAgent.VoiceSessionConfig
import com.runanywhere.sdk.public.extensions.VoiceAgent.VoiceSessionEvent
import com.runanywhere.sdk.public.extensions.streamVoiceSession
import com.runanywhere.sdk.public.extensions.TTS.TTSOptions
import com.runanywhere.sdk.public.extensions.synthesize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Medical system prompt - core safety rules for rural emergencies
private const val MEDICAL_SYSTEM_PROMPT = """
You are MedGuide, an offline emergency medical assistant for remote/rural areas with NO internet access.

CRITICAL SAFETY RULES:
1. NEVER prescribe ANY medicine, drug name, or dosage - ONLY provide non-medication first-aid guidance.
2. Examples of SAFE guidance: "apply pressure to stop bleeding", "keep the person warm", "do CPR at 100 beats/min", "elevate the injured area", "call emergency services ASAP"
3. Examples of UNSAFE guidance: "take ibuprofen 500mg", "use amoxicillin", "inject insulin" - ABSOLUTELY FORBIDDEN
4. After providing first-aid steps, ALWAYS offer to help locate nearest hospital/health center.
5. If user location is unknown, ASK for village/town name and provide approximate distance and directions.
6. Keep responses SHORT (max 3-4 sentences) for clear voice output in noisy rural environments.
7. End EVERY response with: "⚠️ This is AI guidance only. Seek professional medical help immediately."

Remember: You are a life-saving FIRST-AID assistant, NOT a doctor. Your job is to guide people safely until professional help arrives.
"""

// Helper function to play WAV audio for TTS response
private suspend fun playWavAudio(wavData: ByteArray) = 
    withContext(Dispatchers.IO) {
        if (wavData.size < 44) return@withContext
        
        val buffer = ByteBuffer.wrap(wavData).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(20)
        val numChannels = buffer.short.toInt()
        val sampleRate = buffer.int
        buffer.int // byteRate
        buffer.short // blockAlign
        val bitsPerSample = buffer.short.toInt()
        
        // Find data chunk
        var dataOffset = 36
        while (dataOffset < wavData.size - 8) {
            if (wavData[dataOffset] == 'd'.code.toByte() &&
                wavData[dataOffset + 1] == 'a'.code.toByte() &&
                wavData[dataOffset + 2] == 't'.code.toByte() &&
                wavData[dataOffset + 3] == 'a'.code.toByte()) break
            dataOffset++
        }
        dataOffset += 8
        
        if (dataOffset >= wavData.size) return@withContext
        
        val pcmData = wavData.copyOfRange(dataOffset, wavData.size)
        val channelConfig = if (numChannels == 1) 
            AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
        val audioFormat = if (bitsPerSample == 16) 
            AudioFormat.ENCODING_PCM_16BIT else AudioFormat.ENCODING_PCM_8BIT
        
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(audioFormat)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBufferSize, pcmData.size))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        
        audioTrack.write(pcmData, 0, pcmData.size)
        audioTrack.play()
        
        val durationMs = (pcmData.size.toLong() * 1000) / (sampleRate * numChannels * (bitsPerSample / 8))
        kotlinx.coroutines.delay(durationMs + 100)
        
        audioTrack.stop()
        audioTrack.release()
    }

// Audio capture service for voice input (internal to avoid conflict with VoicePipelineScreen)
internal class ChatAudioCaptureService {
    private var audioRecord: AudioRecord? = null
    
    @Volatile
    private var isRecording = false
    
    companion object {
        const val SAMPLE_RATE = 16000
        const val CHUNK_SIZE_MS = 100 // Emit chunks every 100ms
    }
    
    fun startCapture(): Flow<ByteArray> = callbackFlow {
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val chunkSize = (SAMPLE_RATE * 2 * CHUNK_SIZE_MS) / 1000
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(bufferSize, chunkSize * 2)
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                close(IllegalStateException("AudioRecord initialization failed"))
                return@callbackFlow
            }
            
            audioRecord?.startRecording()
            isRecording = true
            
            val readJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                val buffer = ByteArray(chunkSize)
                while (isActive && isRecording) {
                    val bytesRead = audioRecord?.read(buffer, 0, chunkSize) ?: -1
                    if (bytesRead > 0) {
                        trySend(buffer.copyOf(bytesRead))
                    }
                }
            }
            
            awaitClose {
                readJob.cancel()
                stopCapture()
            }
        } catch (e: Exception) {
            stopCapture()
            close(e)
        }
    }
    
    fun stopCapture() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    modelService: ModelService = viewModel(),
    modifier: Modifier = Modifier
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    
    // Voice support state
    var isListening by remember { mutableStateOf(false) }
    var isLastInputVoice by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
    // Audio capture service and voice session job
    val audioCaptureService = remember { ChatAudioCaptureService() }
    var voiceSessionJob by remember { mutableStateOf<Job?>(null) }
    
    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }
    
    // Check permission on launch
    LaunchedEffect(Unit) {
        hasAudioPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            voiceSessionJob?.cancel()
            audioCaptureService.stopCapture()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat - LLM") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark
                )
            )
        },
        containerColor = PrimaryDark
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Model loader section
            if (!modelService.isLLMLoaded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    ModelLoaderWidget(
                        modelName = "SmolLM2 360M",
                        isDownloading = modelService.isLLMDownloading,
                        isLoading = modelService.isLLMLoading,
                        isLoaded = modelService.isLLMLoaded,
                        downloadProgress = modelService.llmDownloadProgress,
                        onLoadClick = { modelService.downloadAndLoadLLM() }
                    )
                    
                    modelService.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
            
            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (messages.isEmpty() && modelService.isLLMLoaded) {
                    item {
                        EmptyStateMessage()
                    }
                }
                
                items(messages) { message ->
                    ChatMessageBubble(message)
                }
            }
            
            // Input section
            if (modelService.isLLMLoaded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceCard.copy(alpha = 0.8f),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type or tap mic for voice...") },
                            readOnly = isGenerating || isListening,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = PrimaryMid,
                                unfocusedContainerColor = PrimaryMid,
                                disabledContainerColor = PrimaryMid,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 4
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // MICROPHONE BUTTON for voice input
                        IconButton(
                            onClick = {
                                if (!hasAudioPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (!isListening) {
                                        // Start voice session
                                        isListening = true
                                        isLastInputVoice = true
                                        voiceSessionJob = scope.launch {
                                            try {
                                                val audioFlow = audioCaptureService.startCapture()
                                                val config = VoiceSessionConfig(
                                                    silenceDuration = 1.5,
                                                    autoPlayTTS = false,
                                                    continuousMode = false
                                                )
                                                
                                                RunAnywhere.streamVoiceSession(audioFlow, config)
                                                    .collect { event ->
                                                        when (event) {
                                                            is VoiceSessionEvent.Transcribed -> {
                                                                isListening = false
                                                                inputText = event.text
                                                                
                                                                // Auto-send the transcribed message
                                                                if (inputText.isNotBlank()) {
                                                                    val userMessage = inputText
                                                                    messages = messages + ChatMessage(userMessage, isUser = true)
                                                                    inputText = ""
                                                                    
                                                                    scope.launch {
                                                                        isGenerating = true
                                                                        listState.animateScrollToItem(messages.size)
                                                                        
                                                                        try {
                                                                            // Prepend medical system prompt to user message
                                                                            val promptedMessage = "$MEDICAL_SYSTEM_PROMPT\n\nUser: $userMessage\nMedGuide:"
                                                                            val response = RunAnywhere.chat(promptedMessage)
                                                                            messages = messages + ChatMessage(response, isUser = false)
                                                                            listState.animateScrollToItem(messages.size)
                                                                            
                                                                            // Auto-speak response if voice input was used
                                                                            if (isLastInputVoice && modelService.isTTSLoaded) {
                                                                                try {
                                                                                    val audioOutput = RunAnywhere.synthesize(response, TTSOptions())
                                                                                    playWavAudio(audioOutput.audioData)
                                                                                } catch (e: Exception) {
                                                                                    Log.w("ChatScreen", "TTS failed: ${e.message}")
                                                                                }
                                                                                isLastInputVoice = false
                                                                            }
                                                                        } catch (e: Exception) {
                                                                            messages = messages + ChatMessage(
                                                                                "Error: ${e.message}",
                                                                                isUser = false
                                                                            )
                                                                        } finally {
                                                                            isGenerating = false
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            is VoiceSessionEvent.Error -> {
                                                                isListening = false
                                                                Log.e("ChatScreen", "Voice error: ${event.message}")
                                                            }
                                                            else -> {}
                                                        }
                                                    }
                                            } catch (e: Exception) {
                                                isListening = false
                                                Log.e("ChatScreen", "Voice session error: ${e.message}")
                                            }
                                        }
                                    } else {
                                        // Stop voice session
                                        isListening = false
                                        voiceSessionJob?.cancel()
                                        audioCaptureService.stopCapture()
                                    }
                                }
                            },
                            enabled = !isGenerating && (modelService.isLLMLoaded && modelService.isSTTLoaded)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                contentDescription = if (isListening) "Stop recording" else "Voice input",
                                tint = if (isListening) AccentViolet else if (!modelService.isSTTLoaded) TextMuted else AccentCyan
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        FloatingActionButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isGenerating) {
                                    val userMessage = inputText
                                    messages = messages + ChatMessage(userMessage, isUser = true)
                                    inputText = ""
                                    
                                    scope.launch {
                                        isGenerating = true
                                        listState.animateScrollToItem(messages.size)
                                        
                                        try {
                                            // Prepend medical system prompt to user message
                                            val promptedMessage = "$MEDICAL_SYSTEM_PROMPT\n\nUser: $userMessage\nMedGuide:"
                                            val response = RunAnywhere.chat(promptedMessage)
                                            messages = messages + ChatMessage(response, isUser = false)
                                            listState.animateScrollToItem(messages.size)
                                        } catch (e: Exception) {
                                            messages = messages + ChatMessage(
                                                "Error: ${e.message}",
                                                isUser = false
                                            )
                                        } finally {
                                            isGenerating = false
                                        }
                                    }
                                }
                            },
                            containerColor = if (isGenerating) AccentViolet else if (inputText.isBlank()) TextMuted else AccentCyan
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.AutoMirrored.Rounded.Send, "Send")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.SmartToy,
            contentDescription = null,
            tint = AccentCyan,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Start a conversation",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Type a message below to chat with the AI",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Icon(
                imageVector = Icons.Rounded.SmartToy,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 16.dp else 4.dp,
                topEnd = if (message.isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) AccentCyan else SurfaceCard
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.isUser) Color.White else TextPrimary
            )
        }
        
        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = AccentViolet,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 4.dp)
            )
        }
    }
}
