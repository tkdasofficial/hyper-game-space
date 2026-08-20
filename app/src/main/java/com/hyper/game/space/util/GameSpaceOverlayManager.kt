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
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.hyper.game.space.R
import com.hyper.game.space.ui.screens.gamespace.GameSpaceOverlayLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyLifecycleOwner : SavedStateRegistryOwner {
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
    private var leftTriggerView: View? = null
    private var rightTriggerView: View? = null
    private var overlayComposeView: ComposeView? = null
    private var lifecycleOwner: MyLifecycleOwner? = null

    @SuppressLint("ClickableViewAccessibility")
    suspend fun showOverlayTrigger(context: Context) = withContext(Dispatchers.Main) {
        if (leftTriggerView != null || rightTriggerView != null) return@withContext

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            // 40dp height (~1cm), 4dp width (~1mm)
            val density = context.resources.displayMetrics.density
            val widthPx = (4 * density).toInt()
            val heightPx = (40 * density).toInt()
            
            fun createTriggerView(isLeft: Boolean): View {
                val container = FrameLayout(context)
                
                val lineView = View(context).apply {
                    setBackgroundColor(0x88FFFFFF.toInt()) // Semi-transparent white
                    // Optional: Rounded corners could be done with a drawable, but simple color is fine
                }
                
                // Add padding area around the thin line to make it easier to touch (e.g., 20dp touch target width)
                val touchWidth = (20 * density).toInt()
                container.addView(lineView, FrameLayout.LayoutParams(widthPx, heightPx).apply {
                    gravity = Gravity.CENTER_VERTICAL or if (isLeft) Gravity.START else Gravity.END
                })

                val params = WindowManager.LayoutParams(
                    touchWidth,
                    heightPx * 2, // Larger touch area vertically
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL or if (isLeft) Gravity.START else Gravity.END
                }

                var initialTouchX = 0f
                var moved = false

                container.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialTouchX = event.rawX
                            moved = false
                            lineView.setBackgroundColor(0xFFFFFFFF.toInt()) // Solid on touch
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val dx = event.rawX - initialTouchX
                            // Swipe inwards
                            val isSwipeInward = if (isLeft) dx > 20 else dx < -20
                            if (isSwipeInward && !moved) {
                                moved = true
                                showFullOverlay(context)
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            lineView.setBackgroundColor(0x88FFFFFF.toInt()) // Revert transparency
                            if (!moved && event.action == MotionEvent.ACTION_UP) {
                                showFullOverlay(context) // Also open on tap
                            }
                            true
                        }
                        else -> false
                    }
                }

                windowManager?.addView(container, params)
                return container
            }

            leftTriggerView = createTriggerView(isLeft = true)
            rightTriggerView = createTriggerView(isLeft = false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun showFullOverlay(context: Context) {
        if (overlayComposeView != null) return
        
        try {
            lifecycleOwner = MyLifecycleOwner()
            
            overlayComposeView = ComposeView(context).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            )
            
            windowManager?.addView(overlayComposeView, params)
            leftTriggerView?.visibility = View.GONE
            rightTriggerView?.visibility = View.GONE
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
            leftTriggerView?.visibility = View.VISIBLE
            rightTriggerView?.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun hideOverlay() = withContext(Dispatchers.Main) {
        try {
            hideFullOverlay()
            leftTriggerView?.let { windowManager?.removeView(it) }
            rightTriggerView?.let { windowManager?.removeView(it) }
            leftTriggerView = null
            rightTriggerView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

