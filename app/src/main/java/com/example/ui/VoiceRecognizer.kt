package com.example.ui

import android.content.Context
import android.os.Build
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

class VoiceRecognizer(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var listeningChannel: Channel<String>? = null
    private var isListening = false

    fun startListening(): ReceiveChannel<String> {
        if (isListening) {
            return Channel(Channel.UNLIMITED).also { it.close() }
        }

        listeningChannel = Channel(Channel.UNLIMITED)
        isListening = true

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle) {
                Log.d("VoiceRecognizer", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("VoiceRecognizer", "Beginning of speech")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: android.os.Bundle) {}

            override fun onEndOfSpeech() {
                Log.d("VoiceRecognizer", "End of speech")
            }

            override fun onError(error: Int) {
                Log.e("VoiceRecognizer", "Speech recognition error: $error")
                listeningChannel?.close()
                isListening = false
                speechRecognizer?.destroy()
                speechRecognizer = null
            }

            override fun onResults(results: android.os.Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { text ->
                    listeningChannel?.trySend(text)
                }
                listeningChannel?.close()
                isListening = false
                speechRecognizer?.destroy()
                speechRecognizer = null
            }

            override fun onPartialResults(partialResults: android.os.Bundle) {}

            override fun onEvent(eventType: Int, params: android.os.Bundle) {}
        }

        recognizer.setRecognitionListener(listener)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            }
        }

        recognizer.startListening(intent)

        return listeningChannel!!
    }

    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
            listeningChannel?.close()
            isListening = false
        }
    }

    fun destroy() {
        stopListening()
    }
}