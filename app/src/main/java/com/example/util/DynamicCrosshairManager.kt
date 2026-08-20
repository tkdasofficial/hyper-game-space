package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DynamicCrosshairManager {
    private var windowManager: WindowManager? = null
    private var crosshairView: CrosshairOverlayView? = null

    enum class CrosshairShape { CROSS, DOT, CIRCLE, DOT_AND_CIRCLE }

    data class CrosshairConfig(
        val color: Int = Color.RED,
        val opacity: Int = 255, // 0 to 255
        val radius: Float = 15f,
        val strokeWidth: Float = 4f,
        val shape: CrosshairShape = CrosshairShape.CROSS
    )

    /**
     * Mounts and draws a hardware-accelerated overlay window exactly at the center of the screen.
     * Note: Requires SYSTEM_ALERT_WINDOW permission.
     */
    suspend fun showCrosshair(context: Context, config: CrosshairConfig) = withContext(Dispatchers.Main) {
        if (crosshairView != null) return@withContext

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            crosshairView = CrosshairOverlayView(context, config)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            windowManager?.addView(crosshairView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            crosshairView = null
        }
    }

    suspend fun updateCrosshair(config: CrosshairConfig) = withContext(Dispatchers.Main) {
        crosshairView?.updateConfig(config)
    }

    suspend fun hideCrosshair() = withContext(Dispatchers.Main) {
        try {
            crosshairView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            crosshairView = null
        }
    }

    private class CrosshairOverlayView(context: Context, private var config: CrosshairConfig) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }

        fun updateConfig(newConfig: CrosshairConfig) {
            this.config = newConfig
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f

            paint.color = config.color
            paint.alpha = config.opacity
            paint.strokeWidth = config.strokeWidth
            
            when (config.shape) {
                CrosshairShape.CROSS -> {
                    paint.style = Paint.Style.STROKE
                    // Draw horizontal and vertical lines with a small center gap
                    val gap = config.radius * 0.3f
                    canvas.drawLine(cx - config.radius, cy, cx - gap, cy, paint)
                    canvas.drawLine(cx + gap, cy, cx + config.radius, cy, paint)
                    canvas.drawLine(cx, cy - config.radius, cx, cy - gap, paint)
                    canvas.drawLine(cx, cy + gap, cx, cy + config.radius, paint)
                }
                CrosshairShape.DOT -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(cx, cy, config.strokeWidth * 1.5f, paint)
                }
                CrosshairShape.CIRCLE -> {
                    paint.style = Paint.Style.STROKE
                    canvas.drawCircle(cx, cy, config.radius, paint)
                }
                CrosshairShape.DOT_AND_CIRCLE -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(cx, cy, config.strokeWidth, paint)
                    paint.style = Paint.Style.STROKE
                    canvas.drawCircle(cx, cy, config.radius, paint)
                }
            }
        }
    }
}
