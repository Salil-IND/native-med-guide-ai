package com.medguide.ai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.medguide.ai.services.MedModelService
import com.medguide.ai.ui.theme.EmergencyRed
import com.medguide.ai.ui.theme.MedBlue
import com.medguide.ai.ui.theme.SafeGreen
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen(
    onNavigateBack: () -> Unit,
    modelService: MedModelService
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    var messages by remember {
        mutableStateOf(listOf(
            ChatMessage(
                "Hello! I'm MedGuide AI, your offline emergency medical assistant. " +
                        "You can ask me about:\n• First aid steps\n• Symptom assessment\n• Drug dosages\n• Emergency procedures\n\n" +
                        "Tap the mic button or type your question. In emergencies, call 108!",
                isUser = false
            )
        ))
    }
    var inputText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) statusText = "Microphone ready"
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isProcessing) return
        messages = messages + ChatMessage(text, isUser = true)
        inputText = ""
        isProcessing = true
        statusText = "Thinking..."

        scope.launch {
            val response = modelService.getMedicalResponse(text)
            messages = messages + ChatMessage(response, isUser = false)
            isProcessing = false
            statusText = ""
            // Auto-speak response
            if (modelService.isTTSLoaded) {
                modelService.speakText(response)
            }
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🎤 Voice Medical Assistant", fontWeight = FontWeight.Bold)
                        Text(
                            if (modelService.isLLMLoaded) "AI Ready • Offline Mode" else "Load models to enable AI",
                            fontSize = 11.sp,
                            color = if (modelService.isLLMLoaded) SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (!modelService.isLLMLoaded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI model not loaded. Go to Setup to download models (required once, works offline forever).",
                                fontSize = 12.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mic button
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    val pulseAnim = rememberInfiniteTransition()
                    val scale by pulseAnim.animateFloat(
                        initialValue = 1f, targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse)
                    )

                    FloatingActionButton(
                        onClick = {
                            if (!hasMicPermission) {
                                micPermission.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                // Voice recording placeholder — integrate with RunAnywhere STT
                                statusText = "Voice recording requires STT model"
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .scale(if (isProcessing) scale else 1f),
                        containerColor = if (hasMicPermission) EmergencyRed else Color.Gray,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice input", modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text input
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask a medical question...", fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        enabled = !isProcessing
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send button
                    FilledIconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isProcessing,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MedBlue
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }

                if (statusText.isNotEmpty()) {
                    Text(
                        statusText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Quick action chips
            item {
                Column {
                    Text("Quick Questions:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    val quickQuestions = listOf(
                        "How to do CPR?",
                        "Signs of heart attack",
                        "Snake bite first aid",
                        "How to treat burns?",
                        "Dosage of paracetamol",
                        "Signs of stroke"
                    )
                    quickQuestions.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { q ->
                                AssistChip(
                                    onClick = { sendMessage(q) },
                                    label = { Text(q, fontSize = 11.sp) },
                                    enabled = !isProcessing && modelService.isLLMLoaded
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(messages) { msg ->
                MessageBubble(message = msg)
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EmergencyRed),
                contentAlignment = Alignment.Center
            ) {
                Text("🏥", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isUser) 16.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(
                    if (isUser) MedBlue
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MedBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}