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

object VisionColorOverlayService {
    private var windowManager: WindowManager? = null
    private var overlayView: VisionOverlayView? = null

    enum class Profile {
        NONE, GRASS_SPOTTER, HIGH_CONTRAST, SHADOW_BOOST
    }

    suspend fun applyFilter(context: Context, profile: Profile) = withContext(Dispatchers.Main) {
        removeFilter()
        if (profile == Profile.NONE) return@withContext

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            overlayView = VisionOverlayView(context, profile)

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

            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            overlayView = null
        }
    }

    suspend fun removeFilter() = withContext(Dispatchers.Main) {
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            overlayView = null
        }
    }

    private class VisionOverlayView(context: Context, profile: Profile) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        init {
            val matrix = ColorMatrix()
            when (profile) {
                Profile.GRASS_SPOTTER -> {
                    // Suppress green, boost red/yellow (Target isolation)
                    matrix.set(floatArrayOf(
                        1.4f, 0.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 0.8f, 0.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 0.9f, 0.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 0.25f, 0.0f
                    ))
                }
                Profile.HIGH_CONTRAST -> {
                    // High Contrast
                    matrix.set(floatArrayOf(
                        1.5f, 0.0f, 0.0f, 0.0f, -30.0f,
                        0.0f, 1.5f, 0.0f, 0.0f, -30.0f,
                        0.0f, 0.0f, 1.5f, 0.0f, -30.0f,
                        0.0f, 0.0f, 0.0f, 0.2f, 0.0f
                    ))
                }
                Profile.SHADOW_BOOST -> {
                    // Lift deep blacks
                    matrix.set(floatArrayOf(
                        1.1f, 0.0f, 0.0f, 0.0f, 50.0f,
                        0.0f, 1.1f, 0.0f, 0.0f, 50.0f,
                        0.0f, 0.0f, 1.1f, 0.0f, 50.0f,
                        0.0f, 0.0f, 0.0f, 0.2f, 0.0f
                    ))
                }
                Profile.NONE -> {}
            }
            paint.colorFilter = ColorMatrixColorFilter(matrix)
            paint.color = Color.argb(50, 255, 255, 255) // Base overlay layer to carry the filter
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
    }
}
