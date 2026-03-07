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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medguide.ai.data.DrugInfo
import com.medguide.ai.data.MedicalKnowledgeBase
import com.medguide.ai.services.MedModelService
import com.medguide.ai.ui.theme.EmergencyRed
import com.medguide.ai.ui.theme.MedBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugInfoScreen(
    onNavigateBack: () -> Unit,
    modelService: MedModelService
) {
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDrug by remember { mutableStateOf<DrugInfo?>(null) }
    var aiDrugQuery by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val filteredDrugs = if (searchQuery.isEmpty()) {
        MedicalKnowledgeBase.commonDrugs
    } else {
        MedicalKnowledgeBase.commonDrugs.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.genericName.contains(searchQuery, ignoreCase = true) ||
                    it.uses.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💊 Drug Information", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; selectedDrug = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search drug name or condition...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Drug list or drug detail
            if (selectedDrug == null) {
                items(filteredDrugs) { drug ->
                    DrugListCard(drug = drug, onClick = { selectedDrug = drug })
                }

                // AI drug query
                item {
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ask AI about any drug", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = aiDrugQuery,
                        onValueChange = { aiDrugQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. 'Can I take ibuprofen with blood pressure medication?'") },
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (aiDrugQuery.isBlank()) return@Button
                            isLoading = true
                            scope.launch {
                                aiResponse = modelService.getMedicalResponse(
                                    aiDrugQuery,
                                    context = "Drug information query"
                                )
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = aiDrugQuery.isNotBlank() && modelService.isLLMLoaded && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MedBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Ask AI")
                    }

                    if (aiResponse.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("🤖 AI Answer:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(aiResponse, fontSize = 14.sp, lineHeight = 21.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "⚠️ Always confirm with a pharmacist or doctor.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    DrugDetailView(
                        drug = selectedDrug!!,
                        onBack = { selectedDrug = null }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun DrugListCard(drug: DrugInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(drug.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(drug.genericName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(drug.uses, fontSize = 13.sp, color = MedBlue)
        }
    }
}

@Composable
fun DrugDetailView(drug: DrugInfo, onBack: () -> Unit) {
    Column {
        TextButton(onClick = onBack) {
            Text("← Back to Drug List")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💊 ${drug.name}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(drug.genericName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(14.dp))

                DrugInfoRow("🎯 Used For", drug.uses)
                DrugInfoRow("👨 Adult Dosage", drug.dosageAdult)
                DrugInfoRow("👦 Child Dosage", drug.dosageChild)

                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("⚠️ Warnings", fontWeight = FontWeight.Bold, color = EmergencyRed, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(drug.warnings, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                DrugInfoRow("😮 Side Effects", drug.sideEffects)
                DrugInfoRow("🔗 Drug Interactions", drug.interactions)
            }
        }
    }
}

@Composable
fun DrugInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MedBlue)
        Text(value, fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
        Divider(modifier = Modifier.padding(top = 6.dp))
    }
}