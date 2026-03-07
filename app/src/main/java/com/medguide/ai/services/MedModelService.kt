package com.medguide.ai.services

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.runanywhere.sdk.core.types.InferenceFramework
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.Models.ModelCategory
import com.runanywhere.sdk.public.extensions.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MedModelService : ViewModel() {

    // ---------------- LLM State ----------------

    var isLLMDownloading by mutableStateOf(false)
        private set
    var llmDownloadProgress by mutableFloatStateOf(0f)
        private set
    var isLLMLoading by mutableStateOf(false)
        private set
    var isLLMLoaded by mutableStateOf(false)
        private set

    // ---------------- STT State ----------------

    var isSTTDownloading by mutableStateOf(false)
        private set
    var sttDownloadProgress by mutableFloatStateOf(0f)
        private set
    var isSTTLoading by mutableStateOf(false)
        private set
    var isSTTLoaded by mutableStateOf(false)
        private set

    // ---------------- TTS State ----------------

    var isTTSDownloading by mutableStateOf(false)
        private set
    var ttsDownloadProgress by mutableFloatStateOf(0f)
        private set
    var isTTSLoading by mutableStateOf(false)
        private set
    var isTTSLoaded by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    companion object {

        const val LLM_MODEL_ID = "smollm2-360m-instruct-q8_0"
        const val STT_MODEL_ID = "sherpa-onnx-whisper-tiny.en"
        const val TTS_MODEL_ID = "vits-piper-en_US-lessac-medium"

        const val MEDICAL_SYSTEM_PROMPT = """
You are MedGuide AI, an emergency medical assistant for offline use in India.

You provide:
• first aid guidance
• emergency triage
• symptom explanation
• medicine safety advice

Always recommend calling emergency services (108 in India) when needed.
Keep answers clear and short.
Never give a definitive diagnosis.
"""

        fun registerDefaultModels() {

            // LLM
            RunAnywhere.registerModel(
                id = LLM_MODEL_ID,
                name = "SmolLM2 360M",
                url = "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf",
                framework = InferenceFramework.LLAMA_CPP,
                modality = ModelCategory.LANGUAGE,
                memoryRequirement = 400_000_000
            )

            // Speech To Text
            RunAnywhere.registerModel(
                id = STT_MODEL_ID,
                name = "Whisper Tiny",
                url = "https://github.com/RunanywhereAI/sherpa-onnx/releases/download/runanywhere-models-v1/sherpa-onnx-whisper-tiny.en.tar.gz",
                framework = InferenceFramework.ONNX,
                modality = ModelCategory.SPEECH_RECOGNITION
            )

            // Text To Speech
            RunAnywhere.registerModel(
                id = TTS_MODEL_ID,
                name = "Piper English Voice",
                url = "https://github.com/RunanywhereAI/sherpa-onnx/releases/download/runanywhere-models-v1/vits-piper-en_US-lessac-medium.tar.gz",
                framework = InferenceFramework.ONNX,
                modality = ModelCategory.SPEECH_SYNTHESIS
            )
        }
    }

    init {
        viewModelScope.launch {
            refreshModelState()
        }
    }

    // ---------------- Model State ----------------

    private suspend fun refreshModelState() {

        isLLMLoaded = RunAnywhere.isLLMModelLoaded()
        isSTTLoaded = RunAnywhere.isSTTModelLoaded()
        isTTSLoaded = RunAnywhere.isTTSVoiceLoaded()
    }

    private suspend fun isModelDownloaded(modelId: String): Boolean {

        val models = RunAnywhere.availableModels()

        return models.find { it.id == modelId }?.localPath != null
    }

    // ---------------- LLM ----------------

    fun downloadAndLoadLLM() {

        if (isLLMDownloading || isLLMLoading) return

        viewModelScope.launch {

            try {

                errorMessage = null

                if (!isModelDownloaded(LLM_MODEL_ID)) {

                    isLLMDownloading = true

                    RunAnywhere.downloadModel(LLM_MODEL_ID)
                        .catch { errorMessage = it.message }
                        .collect { progress ->
                            llmDownloadProgress = progress.progress
                        }

                    isLLMDownloading = false
                }

                isLLMLoading = true

                RunAnywhere.loadLLMModel(LLM_MODEL_ID)

                isLLMLoaded = true
                isLLMLoading = false

                refreshModelState()

            } catch (e: Exception) {

                errorMessage = e.message
                isLLMLoading = false
                isLLMDownloading = false
            }
        }
    }

    // ---------------- STT ----------------

    fun downloadAndLoadSTT() {

        if (isSTTDownloading || isSTTLoading) return

        viewModelScope.launch {

            try {

                if (!isModelDownloaded(STT_MODEL_ID)) {

                    isSTTDownloading = true

                    RunAnywhere.downloadModel(STT_MODEL_ID)
                        .collect { progress ->
                            sttDownloadProgress = progress.progress
                        }

                    isSTTDownloading = false
                }

                isSTTLoading = true

                RunAnywhere.loadSTTModel(STT_MODEL_ID)

                isSTTLoaded = true
                isSTTLoading = false

                refreshModelState()

            } catch (e: Exception) {

                errorMessage = e.message
            }
        }
    }

    // ---------------- TTS ----------------

    fun downloadAndLoadTTS() {

        if (isTTSDownloading || isTTSLoading) return

        viewModelScope.launch {

            try {

                if (!isModelDownloaded(TTS_MODEL_ID)) {

                    isTTSDownloading = true

                    RunAnywhere.downloadModel(TTS_MODEL_ID)
                        .collect { progress ->
                            ttsDownloadProgress = progress.progress
                        }

                    isTTSDownloading = false
                }

                isTTSLoading = true

                RunAnywhere.loadTTSVoice(TTS_MODEL_ID)

                isTTSLoaded = true
                isTTSLoading = false

                refreshModelState()

            } catch (e: Exception) {

                errorMessage = e.message
            }
        }
    }

    // ---------------- Load All ----------------

    fun downloadAndLoadAllModels() {

        if (!isLLMLoaded) downloadAndLoadLLM()
        if (!isSTTLoaded) downloadAndLoadSTT()
        if (!isTTSLoaded) downloadAndLoadTTS()
    }

    // ---------------- AI Chat ----------------

    // CORRECTED: Added the optional `context` parameter to the method signature
    // to match the caller in FirstAidScreen.kt
    suspend fun getMedicalResponse(query: String, context: String? = null): String {

        return try {

            val contextString = if (context != null) "Context: $context\n\n" else ""
            val prompt = "$MEDICAL_SYSTEM_PROMPT\n\n${contextString}User: $query\nAssistant:"

            val response = RunAnywhere.chat(prompt)

            response ?: "No response generated."

        } catch (e: Exception) {

            "AI error: ${e.message}"
        }
    }

    // ---------------- Text To Speech ----------------

    suspend fun speakText(text: String) {

        try {

            RunAnywhere.tts(text)

        } catch (e: Exception) {

            Log.e("MedModelService", e.message ?: "TTS error")
        }
    }

    // ---------------- Speech To Text ----------------

    suspend fun transcribeAudio(audio: ByteArray): String {

        return try {

            RunAnywhere.stt(audio)

        } catch (e: Exception) {

            "Transcription error: ${e.message}"
        }
    }

    fun clearError() {

        errorMessage = null
    }
}