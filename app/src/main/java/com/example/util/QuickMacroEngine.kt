package com.example.util

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object QuickMacroEngine {

    data class TapCoordinate(val x: Float, val y: Float, val delayBeforeMs: Long = 0L)

    /**
     * Executes a rapid sequence of touch events (e.g., Crouch + Jump).
     * Uses Coroutines to handle precise delays without blocking the main UI or Accessibility threads.
     */
    fun executeMacroSequence(service: AccessibilityService, sequence: List<TapCoordinate>) {
        CoroutineScope(Dispatchers.Default).launch {
            for (tap in sequence) {
                if (tap.delayBeforeMs > 0) {
                    delay(tap.delayBeforeMs)
                }
                dispatchTapSync(service, tap.x, tap.y)
            }
        }
    }

    /**
     * Dispatches a single virtual tap utilizing a smooth stroke path for minimal latency.
     */
    private fun dispatchTapSync(service: AccessibilityService, x: Float, y: Float) {
        val path = Path().apply {
            moveTo(x, y)
        }
        
        // 10ms duration to simulate an extremely fast, responsive physical tap
        val stroke = GestureDescription.StrokeDescription(path, 0, 10L)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()
            
        service.dispatchGesture(gesture, null, null)
    }
}
