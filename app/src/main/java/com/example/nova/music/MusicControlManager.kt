package com.example.nova.music

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class CyberTrack(
    val title: String,
    val artist: String,
    val genre: String,
    val query: String
)

class MusicControlManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow(
        CyberTrack("Cyberpunk Pulse Synthwave", "Nova Systems Audio", "Darksynth", "synthwave cyberpunk ambient instrumental")
    )
    val currentTrack: StateFlow<CyberTrack> = _currentTrack.asStateFlow()

    val curatedPresets = listOf(
        CyberTrack("Neo Tokyo 2099", "Kavinsky / Master Boot Record", "Synthwave", "neo tokyo darksynth 2099"),
        CyberTrack("Neural Interface Overdrive", "Cyberpunk 2077 OST", "Industrial Bass", "cyberpunk 2077 rebel path combat mix"),
        CyberTrack("Matrix Terminal Rain", "Ambient Sci-Fi", "Focus Drone", "lofi sci fi ambient cyberpunk rain"),
        CyberTrack("Quantum Resonance", "Nova AI Core", "Glitchwave", "synthwave electronic futuristic beats")
    )

    fun openYouTubeSearch(query: String) {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val appIntent = Intent(Intent.ACTION_SEARCH).apply {
            setPackage("com.google.android.youtube")
            putExtra("query", query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(appIntent)
        } catch (e: Exception) {
            // Fallback to browser web intent
            val webUri = Uri.parse("https://www.youtube.com/results?search_query=$encoded")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }

    fun dispatchMediaPlayPause() {
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        audioManager.dispatchMediaKeyEvent(event)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        audioManager.dispatchMediaKeyEvent(eventUp)
        _isPlaying.value = !_isPlaying.value
    }

    fun dispatchMediaNext() {
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)
        audioManager.dispatchMediaKeyEvent(event)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT)
        audioManager.dispatchMediaKeyEvent(eventUp)
    }

    fun dispatchMediaPrevious() {
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        audioManager.dispatchMediaKeyEvent(event)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        audioManager.dispatchMediaKeyEvent(eventUp)
    }

    fun raiseVolume() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    fun lowerVolume() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    fun selectPreset(track: CyberTrack) {
        _currentTrack.value = track
        openYouTubeSearch(track.query)
        _isPlaying.value = true
    }
}
