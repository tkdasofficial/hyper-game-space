package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VisionEnhancerManager {
    private var windowManager: WindowManager? = null
    private var enhancerView: EnhancerOverlayView? = null

    enum class VisionProfile {
        GRASS_SPOTTER, 
        HIGH_CONTRAST, 
        SHADOW_BOOST,
        NIGHT_VISION
    }

    /**
     * Applies a hardware-accelerated translucent screen filter using ColorMatrix 
     * to naturally enhance contrast and distinct colors underneath.
     */
    suspend fun applyFilter(context: Context, profile: VisionProfile) = withContext(Dispatchers.Main) {
        removeFilter() // Clean up existing if any
        
        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            enhancerView = EnhancerOverlayView(context, profile)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            )

            windowManager?.addView(enhancerView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            enhancerView = null
        }
    }

    suspend fun removeFilter() = withContext(Dispatchers.Main) {
        try {
            enhancerView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            enhancerView = null
        }
    }

    private class EnhancerOverlayView(context: Context, profile: VisionProfile) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        init {
            val matrix = ColorMatrix()
            when (profile) {
                VisionProfile.GRASS_SPOTTER -> {
                    // Pulls red/yellow forward aggressively while neutralizing heavy green/foliage.
                    matrix.set(floatArrayOf(
                        1.4f, 0f, 0f, 0f, 0f,
                        0f, 0.9f, 0f, 0f, 0f,
                        0f, 0f, 0.9f, 0f, 0f,
                        0f, 0f, 0f, 0.25f, 0f
                    ))
                }
                VisionProfile.HIGH_CONTRAST -> {
                    // Deepens blacks and brightens whites, increasing total contrast ratio.
                    matrix.set(floatArrayOf(
                        1.5f, 0f, 0f, 0f, -40f,
                        0f, 1.5f, 0f, 0f, -40f,
                        0f, 0f, 1.5f, 0f, -40f,
                        0f, 0f, 0f, 0.2f, 0f
                    ))
                }
                VisionProfile.SHADOW_BOOST -> {
                    // Flattens extreme blacks directly into mid-greys (lifting shadows).
                    matrix.set(floatArrayOf(
                        1.1f, 0f, 0f, 0f, 40f,
                        0f, 1.1f, 0f, 0f, 40f,
                        0f, 0f, 1.1f, 0f, 40f,
                        0f, 0f, 0f, 0.2f, 0f
                    ))
                }
                VisionProfile.NIGHT_VISION -> {
                    // Casts a monochromatic green luminance filter across the screen.
                    matrix.set(floatArrayOf(
                        0f, 0f, 0f, 0f, 0f,
                        0.5f, 1.5f, 0.5f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0.25f, 0f
                    ))
                }
            }
            paint.colorFilter = ColorMatrixColorFilter(matrix)
            
            // As a SYSTEM_ALERT_WINDOW, we draw a semi-transparent white/grey rectangle
            // The ColorMatrix filter interacts with this layer to produce a translucent colored tint over the screen.
            paint.color = Color.argb(60, 255, 255, 255) 
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
    }
}
