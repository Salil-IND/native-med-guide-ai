package com.medguide.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medguide.ai.data.MedicalKnowledgeBase
import com.medguide.ai.services.MedModelService
import com.medguide.ai.ui.theme.EmergencyRed
import com.medguide.ai.ui.theme.MedBlue
import com.medguide.ai.ui.theme.SafeGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomCheckerScreen(
    onNavigateBack: () -> Unit,
    modelService: MedModelService
) {
    val scope = rememberCoroutineScope()


    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }
    var aiAssessment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var patientAge by remember { mutableStateOf("Adult") }
    var showResult by remember { mutableStateOf(false) }

    fun runAssessment() {
        if (selectedSymptoms.isEmpty()) return
        isLoading = true
        showResult = false
        val symptomsText = selectedSymptoms.joinToString(", ")
        val prompt = "Patient (${patientAge}) has these symptoms: $symptomsText. " +
                "What could this be? What first aid or action should be taken? " +
                "Is this an emergency requiring immediate 108 call? Be concise and practical."

        scope.launch {
            aiAssessment = modelService.getMedicalResponse(prompt)
            isLoading = false
            showResult = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🩺 Symptom Checker", fontWeight = FontWeight.Bold) },
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
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Age selector
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Patient Age Group", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Child (<12)", "Adult", "Elderly (60+)").forEach { age ->
                                FilterChip(
                                    selected = patientAge == age,
                                    onClick = { patientAge = age },
                                    label = { Text(age, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Symptom categories
            items(MedicalKnowledgeBase.symptomCategories.entries.toList()) { (category, symptoms) ->
                SymptomCategoryCard(
                    category = category,
                    symptoms = symptoms,
                    selectedSymptoms = selectedSymptoms,
                    onToggleSymptom = { symptom ->
                        selectedSymptoms = if (selectedSymptoms.contains(symptom)) {
                            selectedSymptoms - symptom
                        } else {
                            selectedSymptoms + symptom
                        }
                    }
                )
            }

            // Selected count + Assess button
            item {
                Column {
                    if (selectedSymptoms.isNotEmpty()) {
                        Text(
                            "${selectedSymptoms.size} symptom(s) selected: ${selectedSymptoms.joinToString(", ")}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = { runAssessment() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedSymptoms.isNotEmpty() && modelService.isLLMLoaded && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MedBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing symptoms...")
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Assess Symptoms with AI")
                        }
                    }

                    if (!modelService.isLLMLoaded) {
                        Text(
                            "⚠️ AI model not loaded. Go to Model Setup to enable this feature.",
                            fontSize = 12.sp,
                            color = Color(0xFFB71C1C),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // AI Result
            if (showResult && aiAssessment.isNotEmpty()) {
                item {
                    val isEmergency = aiAssessment.contains("108", ignoreCase = true) ||
                            aiAssessment.contains("emergency", ignoreCase = true) ||
                            aiAssessment.contains("immediately", ignoreCase = true)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEmergency) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isEmergency) "🚨" else "🩺", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isEmergency) "⚠️ Potential Emergency" else "Assessment Result",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEmergency) EmergencyRed else SafeGreen,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                aiAssessment,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "⚠️ This is AI guidance only, not medical diagnosis. Consult a doctor for proper evaluation.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SymptomCategoryCard(
    category: String,
    symptoms: List<String>,
    selectedSymptoms: Set<String>,
    onToggleSymptom: (String) -> Unit
) {
    val categoryEmoji = mapOf(
        "Chest & Heart" to "❤️",
        "Head & Brain" to "🧠",
        "Breathing" to "🫁",
        "Abdomen" to "🫃",
        "Allergic" to "⚠️",
        "Trauma" to "🩸",
        "Fever" to "🌡️"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "${categoryEmoji[category] ?: "•"} $category",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            symptoms.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { symptom ->
                        val selected = selectedSymptoms.contains(symptom)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) EmergencyRed.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onToggleSymptom(symptom) }
                                .padding(8.dp)
                        ) {
                            Text(
                                symptom,
                                fontSize = 12.sp,
                                color = if (selected) EmergencyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}