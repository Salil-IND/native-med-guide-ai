```kotlin
package com.medguide.ai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.medguide.ai.services.MedModelService
import com.medguide.ai.ui.theme.EmergencyRed
import com.medguide.ai.ui.theme.MedBlue
import com.medguide.ai.ui.theme.SafeGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
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
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    text =
                    "Hello! I'm MedGuide AI, your offline emergency medical assistant.\n\n" +
                            "You can ask me about:\n" +
                            "• First aid steps\n" +
                            "• Symptom assessment\n" +
                            "• Drug dosages\n" +
                            "• Emergency procedures\n\n" +
                            "Tap the mic button or type your question.\n" +
                            "In emergencies call 108.",
                    isUser = false
                )
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        statusText =
            if (granted) "Microphone ready"
            else "Microphone permission denied"
    }

    fun scrollToBottom(index: Int) {
        scope.launch {
            listState.animateScrollToItem(index)
        }
    }

    fun sendMessage(text: String) {

        if (text.isBlank() || isProcessing) return

        if (!modelService.isLLMLoaded) {
            statusText = "Load AI model first in Setup"
            return
        }

        val userMessage = ChatMessage(text = text, isUser = true)

        val updatedUserMessages = messages + userMessage
        messages = updatedUserMessages
        scrollToBottom(updatedUserMessages.size - 1)

        inputText = ""
        isProcessing = true
        statusText = "Thinking..."

        scope.launch {

            try {

                val response = withContext(Dispatchers.IO) {
                    modelService.getMedicalResponse(text)
                }

                val aiMessage =
                    ChatMessage(text = response, isUser = false)

                val updatedMessages = messages + aiMessage
                messages = updatedMessages
                scrollToBottom(updatedMessages.size - 1)

                if (modelService.isTTSLoaded) {
                    launch(Dispatchers.IO) {
                        modelService.speakText(response)
                    }
                }

            } catch (e: Exception) {

                val errorMessage =
                    ChatMessage(
                        text = "Error generating response. Please try again.",
                        isUser = false
                    )

                messages = messages + errorMessage
                scrollToBottom(messages.size - 1)
            }

            isProcessing = false
            statusText = ""
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(
                            "🎤 Voice Medical Assistant",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            if (modelService.isLLMLoaded)
                                "AI Ready • Offline Mode"
                            else
                                "Load models to enable AI",
                            fontSize = 11.sp,
                            color =
                            if (modelService.isLLMLoaded)
                                SafeGreen
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },

                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },

        bottomBar = {

            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val hasMicPermission =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                    val scale by animateFloatAsState(
                        targetValue = if (isProcessing) 1.1f else 1f,
                        animationSpec = tween(300),
                        label = ""
                    )

                    FloatingActionButton(

                        enabled = !isProcessing,

                        onClick = {

                            if (!hasMicPermission) {
                                micPermission.launch(
                                    Manifest.permission.RECORD_AUDIO
                                )
                                return@FloatingActionButton
                            }

                            if (!modelService.isSTTLoaded) {
                                statusText = "Load STT model in Setup"
                                return@FloatingActionButton
                            }

                            scope.launch {

                                statusText = "Listening..."

                                val speech =
                                    withContext(Dispatchers.IO) {
                                        modelService.listenSpeech()
                                    }

                                if (!speech.isNullOrBlank()) {
                                    sendMessage(speech)
                                }

                                statusText = ""
                            }
                        },

                        modifier = Modifier
                            .size(52.dp)
                            .scale(scale),

                        containerColor = EmergencyRed,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Mic, null)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("Ask a medical question...")
                        },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        enabled = !isProcessing
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = { sendMessage(inputText) },
                        enabled =
                        inputText.isNotBlank() && !isProcessing,
                        modifier = Modifier.size(48.dp),
                        colors =
                        IconButtonDefaults.filledIconButtonColors(
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

                            Icon(
                                Icons.Default.Send,
                                null,
                                tint = Color.White
                            )
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
                        color =
                        MaterialTheme.colorScheme.onSurfaceVariant
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

            item {

                Column {

                    Text(
                        "Quick Questions:",
                        fontSize = 12.sp,
                        color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                    )

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

                        Row(
                            horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                        ) {

                            row.forEach { q ->

                                AssistChip(
                                    onClick = { sendMessage(q) },
                                    label = {
                                        Text(q, fontSize = 11.sp)
                                    },
                                    enabled =
                                    !isProcessing &&
                                            modelService.isLLMLoaded
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(
                messages,
                key = { it.id }
            ) { msg ->

                MessageBubble(msg, screenWidth)
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    screenWidth: androidx.compose.ui.unit.Dp
) {

    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
        if (isUser) Arrangement.End
        else Arrangement.Start
    ) {

        if (!isUser) {

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EmergencyRed),
                contentAlignment = Alignment.Center
            ) {
                Text("🏥")
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = screenWidth * 0.75f)
                .clip(
                    RoundedCornerShape(
                        topStart =
                        if (isUser) 16.dp else 4.dp,
                        topEnd =
                        if (isUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(
                    if (isUser)
                        MedBlue
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {

            Text(
                message.text,
                color =
                if (isUser) Color.White
                else MaterialTheme.colorScheme.onSurface,
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

                Icon(
                    Icons.Default.Person,
                    null,
                    tint = Color.White
                )
            }
        }
    }
}
```
