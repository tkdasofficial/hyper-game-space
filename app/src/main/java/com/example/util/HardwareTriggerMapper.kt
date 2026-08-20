package com.example.util

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.KeyEvent
import java.util.concurrent.ConcurrentHashMap

object HardwareTriggerMapper {
    
    @Volatile
    var isTriggerEngineActive = false
    
    // Map hardware keyCode to (X, Y) float coordinates
    private val mappings = ConcurrentHashMap<Int, Pair<Float, Float>>()

    fun mapKey(keyCode: Int, x: Float, y: Float) {
        mappings[keyCode] = Pair(x, y)
    }

    fun clearMappings() {
        mappings.clear()
    }

    /**
     * Intercepts key events (e.g. Volume Up/Down).
     * Dispatches ultra-fast zero-delay tap gestures to mapped coordinates.
     */
    fun onKeyEvent(event: KeyEvent, service: AccessibilityService): Boolean {
        if (!isTriggerEngineActive) return false
        
        val coordinate = mappings[event.keyCode]
        if (coordinate != null) {
            // Execute on ACTION_DOWN, but consume ACTION_UP so volume UI doesn't appear
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                dispatchRapidTap(service, coordinate.first, coordinate.second)
            }
            return true // Consume event
        }
        return false // Allow default behavior
    }

    private fun dispatchRapidTap(service: AccessibilityService, x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 10L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        service.dispatchGesture(gesture, null, null)
    }
}
