package com.medguide.ai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medguide.ai.services.MedModelService
import com.medguide.ai.ui.screens.DrugInfoScreen
import com.medguide.ai.ui.screens.EmergencyScreen
import com.medguide.ai.ui.screens.FirstAidScreen
import com.medguide.ai.ui.screens.HomeScreen
import com.medguide.ai.ui.screens.ModelSetupScreen
import com.medguide.ai.ui.screens.SymptomCheckerScreen
import com.medguide.ai.ui.screens.VoiceAssistantScreen
import com.medguide.ai.ui.theme.MedGuideTheme
import com.runanywhere.sdk.core.onnx.ONNX
import com.runanywhere.sdk.foundation.bridge.extensions.CppBridgeModelPaths
import com.runanywhere.sdk.llm.llamacpp.LlamaCPP
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.SDKEnvironment
import com.runanywhere.sdk.public.extensions.loadSTTModel
import com.runanywhere.sdk.public.extensions.loadTTSVoice
import com.runanywhere.sdk.storage.AndroidPlatformContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AndroidPlatformContext.initialize(this)
        RunAnywhere.initialize(environment = SDKEnvironment.DEVELOPMENT)

        val runanywherePath = java.io.File(filesDir, "runanywhere").absolutePath
        CppBridgeModelPaths.setBaseDirectory(runanywherePath)

        try {
            LlamaCPP.register(priority = 100)
        } catch (e: Throwable) {
            Log.w("MainActivity", "LlamaCPP partial failure: ${e.message}")
        }
        ONNX.register(priority = 100)

        MedModelService.registerDefaultModels()

        lifecycleScope.launch {
            try {
                RunAnywhere.loadSTTModel(MedModelService.STT_MODEL_ID)
                RunAnywhere.loadTTSVoice(MedModelService.TTS_MODEL_ID)
            } catch (e: Exception) {
                Log.w("MainActivity", "Voice models not yet downloaded: ${e.message}")
            }
        }

        setContent {
            MedGuideTheme {
                MedGuideApp()
            }
        }
    }
}

@Composable
fun MedGuideApp() {
    val navController = rememberNavController()
    val modelService: MedModelService = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToEmergency = { navController.navigate("emergency") },
                onNavigateToFirstAid = { navController.navigate("first_aid") },
                onNavigateToVoiceAssistant = { navController.navigate("voice_assistant") },
                onNavigateToSymptomChecker = { navController.navigate("symptom_checker") },
                onNavigateToDrugInfo = { navController.navigate("drug_info") },
                onNavigateToModels = { navController.navigate("models") }
            )
        }
        composable("emergency") {
            EmergencyScreen(
                onNavigateBack = { navController.popBackStack() },
                modelService = modelService
            )
        }
        composable("first_aid") {
            FirstAidScreen(
                onNavigateBack = { navController.popBackStack() },
                modelService = modelService
            )
        }
        composable("voice_assistant") {
            VoiceAssistantScreen(
                onNavigateBack = { navController.popBackStack() },
                modelService = modelService
            )
        }
        composable("symptom_checker") {
            SymptomCheckerScreen(
                onNavigateBack = { navController.popBackStack() },
                modelService = modelService
            )
        }
        composable("drug_info") {
            DrugInfoScreen(
                onNavigateBack = { navController.popBackStack() },
                modelService = modelService
            )
        }
        composable("models") {
            ModelSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                modelService = modelService
            )
        }
    }
}