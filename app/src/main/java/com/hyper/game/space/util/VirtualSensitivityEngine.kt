package com.hyper.game.space.util

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Path
import android.graphics.PixelFormat
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

object VirtualSensitivityEngine {
    private var windowManager: WindowManager? = null
    private var touchOverlayView: View? = null
    private var isEngineActive = false

    var xAxisMultiplier: Float = 1.0f
    var yAxisMultiplier: Float = 1.0f

    /**
     * Mounts a full-screen, invisible touch interceptor.
     * Scales touch movement deltas and dispatches real simulated gestures via AccessibilityService.
     */
    @SuppressLint("ClickableViewAccessibility")
    suspend fun startEngine(context: Context, service: AccessibilityService) = withContext(Dispatchers.Main) {
        if (isEngineActive) return@withContext

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            touchOverlayView = View(context).apply {
                var lastX = 0f
                var lastY = 0f
                var syntheticX = 0f
                var syntheticY = 0f

                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            lastX = event.rawX
                            lastY = event.rawY
                            syntheticX = lastX
                            syntheticY = lastY
                            dispatchScaledTap(service, syntheticX, syntheticY, isDown = true)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val deltaX = event.rawX - lastX
                            val deltaY = event.rawY - lastY
                            
                            // Only dispatch if movement is significant to reduce latency queueing
                            if (abs(deltaX) > 1f || abs(deltaY) > 1f) {
                                lastX = event.rawX
                                lastY = event.rawY

                                syntheticX += deltaX * xAxisMultiplier
                                syntheticY += deltaY * yAxisMultiplier

                                dispatchScaledSwipe(service, syntheticX, syntheticY)
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            dispatchScaledTap(service, syntheticX, syntheticY, isDown = false)
                        }
                    }
                    true // Consume the event natively
                }
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSPARENT
            )

            windowManager?.addView(touchOverlayView, params)
            isEngineActive = true
        } catch (e: Exception) {
            e.printStackTrace()
            isEngineActive = false
        }
    }

    suspend fun stopEngine() = withContext(Dispatchers.Main) {
        try {
            touchOverlayView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            touchOverlayView = null
            isEngineActive = false
        }
    }

    private fun dispatchScaledTap(service: AccessibilityService, x: Float, y: Float, isDown: Boolean) {
        val path = Path().apply { moveTo(x, y) }
        // For a seamless hold, we would need continuous stroke tracking. 
        // We use a minimum 1ms stroke for instant interaction scaling.
        val stroke = GestureDescription.StrokeDescription(path, 0, 10L, !isDown)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        service.dispatchGesture(gesture, null, null)
    }
    
    private fun dispatchScaledSwipe(service: AccessibilityService, x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y); lineTo(x + 1f, y + 1f) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 10L, true)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        service.dispatchGesture(gesture, null, null)
    }
}
