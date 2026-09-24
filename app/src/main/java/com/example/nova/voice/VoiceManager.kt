package com.example.nova.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(private val context: Context) {

    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    var onCommandRecognized: ((String) -> Unit)? = null

    init {
        initSpeechRecognizer()
        initTextToSpeech()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _orbState.value = OrbState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {
                        _orbState.value = OrbState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB to 10dB into 0..1 range
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _rmsLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _orbState.value = OrbState.PROCESSING
                        _rmsLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        Log.w("VoiceManager", "Speech recognition error code: $error")
                        _orbState.value = OrbState.IDLE
                        _rmsLevel.value = 0f
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            _transcript.value = text
                            _orbState.value = OrbState.PROCESSING
                            onCommandRecognized?.invoke(text)
                        } else {
                            _orbState.value = OrbState.IDLE
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            _transcript.value = it
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    val result = tts.setLanguage(Locale.US)
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Pitch 1.15f and speech rate 1.05f provides a clear, futuristic female AI cadence
                        tts.setPitch(1.15f)
                        tts.setSpeechRate(1.05f)

                        // Attempt to pick a smooth female voice variant if available
                        try {
                            val femaleVoice = tts.voices?.firstOrNull {
                                it.locale == Locale.US && (it.name.contains("female", ignoreCase = true) || it.name.contains("en-us-x-sfg", ignoreCase = true))
                            }
                            if (femaleVoice != null) {
                                tts.voice = femaleVoice
                            }
                        } catch (e: Exception) {
                            Log.d("VoiceManager", "Using default TTS voice: ${e.message}")
                        }

                        _isTtsReady.value = true
                    }
                }
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _orbState.value = OrbState.SPEAKING
            }

            override fun onDone(utteranceId: String?) {
                _orbState.value = OrbState.IDLE
            }

            override fun onError(utteranceId: String?) {
                _orbState.value = OrbState.IDLE
            }
        })
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _transcript.value = "Speech recognition unavailable on this device"
            return
        }

        stopSpeaking()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
            _orbState.value = OrbState.LISTENING
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error starting listening", e)
            _orbState.value = OrbState.IDLE
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error stopping listening", e)
        }
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        if (text.isBlank()) return
        stopListening()
        _orbState.value = OrbState.SPEAKING
        val utteranceId = "NOVA_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        if (_orbState.value == OrbState.SPEAKING) {
            _orbState.value = OrbState.IDLE
        }
    }

    fun setOrbState(state: OrbState) {
        _orbState.value = state
    }

    fun setSimulatedRms(level: Float) {
        _rmsLevel.value = level
    }

    fun setTranscript(text: String) {
        _transcript.value = text
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error during destruction", e)
        }
    }
}
