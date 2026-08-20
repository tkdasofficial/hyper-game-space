package com.hyper.game.space.util

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.KeyEvent
import java.util.concurrent.ConcurrentHashMap

object HardwareTriggerService {
    
    @Volatile
    var isGamingModeActive = false
    
    // Thread-safe map for rapid volume button clicks
    private val triggerMappings = ConcurrentHashMap<Int, Pair<Float, Float>>()

    fun setMapping(keyCode: Int, x: Float, y: Float) {
        triggerMappings[keyCode] = Pair(x, y)
    }

    fun clearMappings() {
        triggerMappings.clear()
    }

    /**
     * Intercepts hardware keys through the AccessibilityService.
     * Returns true to consume the event (preventing volume changes), or false to let it pass.
     */
    fun onKeyEvent(event: KeyEvent, service: AccessibilityService): Boolean {
        if (!isGamingModeActive) return false
        
        val mapping = triggerMappings[event.keyCode]
        if (mapping != null) {
            // We only want to trigger the touch gesture on the initial ACTION_DOWN
            // to avoid spamming the screen on release, but we must consume ACTION_UP as well
            // so the system volume UI doesn't appear.
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                dispatchTap(service, mapping.first, mapping.second)
            }
            return true // Consume the volume key
        }
        
        return false // Not mapped, allow default system behavior
    }

    private fun dispatchTap(service: AccessibilityService, x: Float, y: Float) {
        val path = Path().apply {
            moveTo(x, y)
        }
        
        // 15ms duration for a quick tap
        val stroke = GestureDescription.StrokeDescription(path, 0, 15L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        
        service.dispatchGesture(gesture, null, null)
    }
}
