package com.medguide.ai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medguide.ai.data.FirstAidTopic
import com.medguide.ai.data.MedicalKnowledgeBase
import com.medguide.ai.services.MedModelService
import com.medguide.ai.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstAidScreen(
    onNavigateBack: () -> Unit,
    modelService: MedModelService
) {
    val scope = rememberCoroutineScope()

    var expandedId by remember { mutableStateOf<String?>(null) }
    var aiResponses by remember { mutableStateOf(mapOf<String, String>()) }
    var loadingId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🩹 First Aid Guide", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "📖 These guides work completely offline. Tap 'Ask AI' for personalized guidance when the model is loaded.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF0D47A1)
                    )
                }
            }

            items(MedicalKnowledgeBase.firstAidTopics) { topic ->
                FirstAidCard(
                    topic = topic,
                    isExpanded = expandedId == topic.id,
                    aiResponse = aiResponses[topic.id],
                    isLoadingAI = loadingId == topic.id,
                    aiEnabled = modelService.isLLMLoaded,
                    onToggle = { expandedId = if (expandedId == topic.id) null else topic.id },
                    onAskAI = { question ->
                        loadingId = topic.id
                        scope.launch {
                            val response = modelService.getMedicalResponse(
                                question,
                                context = "First Aid topic: ${topic.title}"
                            )
                            aiResponses = aiResponses + (topic.id to response)
                            loadingId = null
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun FirstAidCard(
    topic: FirstAidTopic,
    isExpanded: Boolean,
    aiResponse: String?,
    isLoadingAI: Boolean,
    aiEnabled: Boolean,
    onToggle: () -> Unit,
    onAskAI: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(topic.icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(topic.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(topic.category, fontSize = 11.sp, color = WarningOrange)
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        topic.content,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    topic.steps.forEach { step ->
                        val isSection = step.endsWith(":") || step.all { it.isUpperCase() || it == ' ' || it == ':' }
                        Text(
                            step,
                            fontSize = if (isSection) 12.sp else 14.sp,
                            fontWeight = if (isSection) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSection) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI Ask section
                    if (aiResponse != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🤖 AI Response:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(aiResponse, fontSize = 13.sp, lineHeight = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (isLoadingAI) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI is processing...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onAskAI("Tell me more about ${topic.title} first aid. What should I do step by step?") },
                            enabled = aiEnabled,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (aiEnabled) "🤖 Ask AI for More Detail"
                                else "🔒 Load AI Model for More Detail",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}