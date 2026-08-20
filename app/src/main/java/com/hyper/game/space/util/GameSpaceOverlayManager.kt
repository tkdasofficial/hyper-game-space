package com.hyper.game.space.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.hyper.game.space.R
import com.hyper.game.space.ui.screens.gamespace.GameSpaceOverlayLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyLifecycleOwner : SavedStateRegistryOwner, ViewModelStoreOwner {
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    init {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
        
    fun stop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

object GameSpaceOverlayManager {
    private var windowManager: WindowManager? = null
    private var triggerViews: MutableList<View> = mutableListOf()
    private var lastLeftSwipeTime = 0L
    private var lastRightSwipeTime = 0L
    private var overlayComposeView: ComposeView? = null
    private var lifecycleOwner: MyLifecycleOwner? = null
    
    

    @SuppressLint("ClickableViewAccessibility")
    suspend fun showOverlayTrigger(context: Context) = withContext(Dispatchers.Main) {
        if (triggerViews.isNotEmpty()) return@withContext
        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val density = context.resources.displayMetrics.density
            val triggerType = SettingsManager.getToolboxTriggerType(context)

            if (triggerType == "2 Fingers Swipe (Top Corners)") {
                // Top corners invisible swipe
                val touchThicknessPx = (20 * density).toInt()
                val swipeDistanceThresholdPx = (126 * density).toInt()

                fun createEdgeTrigger(isLeft: Boolean): View {
                    val container = FrameLayout(context).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS }
                    val params = WindowManager.LayoutParams(
                        touchThicknessPx,
                        (150 * density).toInt(),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.TOP or if (isLeft) Gravity.START else Gravity.END
                    }

                    var initialTouchX = 0f
                    var hasTriggered = false

                    container.setOnTouchListener { _, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                initialTouchX = event.rawX
                                hasTriggered = false
                                true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                if (!hasTriggered) {
                                    val dx = event.rawX - initialTouchX
                                    val swipeDistance = if (isLeft) dx else -dx
                                    if (swipeDistance >= swipeDistanceThresholdPx) {
                                        hasTriggered = true
                                        val now = System.currentTimeMillis()
                                        if (isLeft) {
                                            lastLeftSwipeTime = now
                                            if (now - lastRightSwipeTime <= 1500L) {
                                                showFullOverlay(context)
                                                lastLeftSwipeTime = 0L
                                                lastRightSwipeTime = 0L
                                            }
                                        } else {
                                            lastRightSwipeTime = now
                                            if (now - lastLeftSwipeTime <= 1500L) {
                                                showFullOverlay(context)
                                                lastLeftSwipeTime = 0L
                                                lastRightSwipeTime = 0L
                                            }
                                        }
                                    }
                                }
                                true
                            }
                            else -> false
                        }
                    }
                    windowManager?.addView(container, params)
                    return container
                }
                triggerViews.add(createEdgeTrigger(true))
                triggerViews.add(createEdgeTrigger(false))

            } else if (triggerType == "Floating Icon") {
                val sizePx = (48 * density).toInt()
                val container = FrameLayout(context).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS }
                val iconView = ImageView(context).apply {
                    setImageResource(R.drawable.ic_launcher_foreground) // Fallback icon
                    setBackgroundResource(R.drawable.rounded_icon_bg) // We'll create this if needed, or just set color
                    setBackgroundColor(android.graphics.Color.parseColor("#88000000"))
                    setPadding(10, 10, 10, 10)
                }
                container.addView(iconView, FrameLayout.LayoutParams(sizePx, sizePx))

                val params = WindowManager.LayoutParams(
                    sizePx, sizePx,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = 100
                    y = 100
                }

                var initialX = 0
                var initialY = 0
                var initialTouchX = 0f
                var initialTouchY = 0f
                var isDragging = false

                container.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            isDragging = false
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val dx = event.rawX - initialTouchX
                            val dy = event.rawY - initialTouchY
                            if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                                isDragging = true
                                params.x = initialX + dx.toInt()
                                params.y = initialY + dy.toInt()
                                windowManager?.updateViewLayout(container, params)
                            }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (!isDragging) {
                                showFullOverlay(context)
                            }
                            true
                        }
                        else -> false
                    }
                }
                windowManager?.addView(container, params)
                triggerViews.add(container)

            } else if (triggerType == "Dual Edge Swipe (Sides)") {
                // Just visible thick bars on both sides that open on inward swipe
                val lengthPx = (100 * density).toInt()
                val thicknessPx = (6 * density).toInt()
                val touchAreaPx = (32 * density).toInt()

                fun createVisibleEdge(isLeft: Boolean): View {
                    val container = FrameLayout(context).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS }
                    val lineView = View(context).apply {
                        val drawable = android.graphics.drawable.GradientDrawable().apply {
                            setColor(0x88FFFFFF.toInt())
                            cornerRadius = (4 * density)
                        }
                        background = drawable
                    }
                    val stickParams = FrameLayout.LayoutParams(thicknessPx, lengthPx).apply {
                        gravity = Gravity.CENTER_VERTICAL or if (isLeft) Gravity.START else Gravity.END
                    }
                    container.addView(lineView, stickParams)

                    val params = WindowManager.LayoutParams(
                        touchAreaPx, lengthPx + (40 * density).toInt(),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL or if (isLeft) Gravity.START else Gravity.END
                    }

                    var initialTouchX = 0f
                    var hasTriggered = false
                    container.setOnTouchListener { _, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                initialTouchX = event.rawX
                                hasTriggered = false
                                true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                if (!hasTriggered) {
                                    val dx = event.rawX - initialTouchX
                                    val swipeDist = if (isLeft) dx else -dx
                                    if (swipeDist > 30 * density) {
                                        hasTriggered = true
                                        showFullOverlay(context)
                                    }
                                }
                                true
                            }
                            else -> false
                        }
                    }
                    windowManager?.addView(container, params)
                    return container
                }
                triggerViews.add(createVisibleEdge(true))
                triggerViews.add(createVisibleEdge(false))

            } else {
                // Default: Single Finger Swipe (Left Center)
                val lengthPx = (75 * density).toInt()
                val thicknessPx = (4 * density).toInt()
                val touchAreaPx = (32 * density).toInt()

                val container = FrameLayout(context).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS }
                val lineView = View(context).apply {
                    val drawable = android.graphics.drawable.GradientDrawable().apply {
                        setColor(0x88FFFFFF.toInt())
                        cornerRadius = (4 * density)
                    }
                    background = drawable
                }
                val stickParams = FrameLayout.LayoutParams(thicknessPx, lengthPx).apply {
                    gravity = Gravity.CENTER_VERTICAL or Gravity.START
                }
                container.addView(lineView, stickParams)

                val params = WindowManager.LayoutParams(
                    touchAreaPx, lengthPx + (40 * density).toInt(),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = 0
                    y = (context.resources.displayMetrics.heightPixels / 2 - lengthPx / 2)
                }

                var initialTouchX = 0f
                var hasTriggered = false
                container.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialTouchX = event.rawX
                            hasTriggered = false
                            lineView.setBackgroundColor(0xFF00FF00.toInt())
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (!hasTriggered) {
                                val dx = event.rawX - initialTouchX
                                if (dx > 30 * density) {
                                    hasTriggered = true
                                    showFullOverlay(context)
                                }
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            val drawable = android.graphics.drawable.GradientDrawable().apply {
                                setColor(0x88FFFFFF.toInt())
                                cornerRadius = (4 * density)
                            }
                            lineView.background = drawable
                            true
                        }
                        else -> false
                    }
                }
                windowManager?.addView(container, params)
                triggerViews.add(container)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showFullOverlay(context: Context) {
        if (overlayComposeView != null) return
        
        try {
            lifecycleOwner = MyLifecycleOwner()
            
            overlayComposeView = ComposeView(context).apply {
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setContent {
                    GameSpaceOverlayLayout(
                        isVisible = true,
                        onClose = { hideFullOverlay() }
                    )
                }
            }
            
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            )
            
            windowManager?.addView(overlayComposeView, params)
            triggerViews.forEach { it.visibility = View.GONE }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun hideFullOverlay() {
        try {
            overlayComposeView?.let {
                windowManager?.removeView(it)
            }
            lifecycleOwner?.stop()
            overlayComposeView = null
            lifecycleOwner = null
            triggerViews.forEach { it.visibility = View.VISIBLE }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun hideOverlay() = withContext(Dispatchers.Main) {
        try {
            hideFullOverlay()
            triggerViews.forEach { view ->
                try { windowManager?.removeView(view) } catch (e: Exception) {}
            }
            triggerViews.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
