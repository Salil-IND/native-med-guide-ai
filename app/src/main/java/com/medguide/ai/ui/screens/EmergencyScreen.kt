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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medguide.ai.data.EmergencyProtocol
import com.medguide.ai.data.MedicalKnowledgeBase
import com.medguide.ai.data.Severity
import com.medguide.ai.services.MedModelService
import com.medguide.ai.ui.theme.EmergencyRed
import com.medguide.ai.ui.theme.SafeGreen
import com.medguide.ai.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    onNavigateBack: () -> Unit,
    modelService: MedModelService
) {
    var expandedId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🚨 Emergency Protocols", fontWeight = FontWeight.Bold)
                        Text("Tap any emergency for step-by-step guide", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFB71C1C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
                // Emergency Call Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📞", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("India Emergency: 108", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            Text("Ambulance • Police: 100 • Fire: 101 • Women: 1091", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                        }
                    }
                }
            }

            items(MedicalKnowledgeBase.emergencyProtocols) { protocol ->
                EmergencyCard(
                    protocol = protocol,
                    isExpanded = expandedId == protocol.id,
                    onToggle = {
                        expandedId = if (expandedId == protocol.id) null else protocol.id
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun EmergencyCard(
    protocol: EmergencyProtocol,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val severityColor = when (protocol.severity) {
        Severity.CRITICAL -> EmergencyRed
        Severity.HIGH -> WarningOrange
        Severity.MEDIUM -> Color(0xFFF9A825)
        Severity.LOW -> SafeGreen
    }
    val severityLabel = when (protocol.severity) {
        Severity.CRITICAL -> "CRITICAL"
        Severity.HIGH -> "HIGH"
        Severity.MEDIUM -> "MEDIUM"
        Severity.LOW -> "LOW"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(protocol.icon, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        protocol.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(severityColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(severityLabel, color = severityColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        if (protocol.callAmbulance) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFB71C1C).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("CALL 108", color = Color(0xFFB71C1C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
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
                    Spacer(modifier = Modifier.height(12.dp))

                    // Steps
                    protocol.steps.forEach { step ->
                        val isHeader = step.startsWith("USE ") || step.endsWith(":")
                        Text(
                            step,
                            fontSize = if (isHeader) 12.sp else 14.sp,
                            fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isHeader) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }

                    // Warnings
                    if (protocol.warnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Important Notes", fontWeight = FontWeight.Bold, color = WarningOrange, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                protocol.warnings.forEach { warning ->
                                    Text("• $warning", fontSize = 12.sp, color = Color(0xFF5D4037), modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}