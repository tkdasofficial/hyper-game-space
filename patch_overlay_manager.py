import re

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'r') as f:
    content = f.read()

# Replace variables
content = re.sub(r'private var leftTriggerView: View\? = null\s*private var rightTriggerView: View\? = null', 'private var singleTriggerView: View? = null', content)
content = re.sub(r'private var lastLeftSwipeTime = 0L\s*private var lastRightSwipeTime = 0L', '', content)

# Replace showOverlayTrigger
show_overlay_func = """    @SuppressLint("ClickableViewAccessibility")
    suspend fun showOverlayTrigger(context: Context) = withContext(Dispatchers.Main) {
        if (singleTriggerView != null) return@withContext
        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            val density = context.resources.displayMetrics.density
            val isLandscape = context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            
            // 1.5cm long ~ 60dp, 0.5mm thick ~ 2dp
            val lengthPx = (60 * density).toInt()
            val thicknessPx = (2 * density).toInt()
            val touchAreaPx = (32 * density).toInt()

            val container = FrameLayout(context)
            
            val lineView = View(context).apply {
                val drawable = android.graphics.drawable.GradientDrawable().apply {
                    setColor(0x88FFFFFF.toInt()) // Semi-transparent white
                    cornerRadius = (4 * density)
                }
                background = drawable
            }
            
            // For landscape, if it's placed Top-Left, maybe adjust bounds, but simple center-vertical stick is fine.
            val stickParams = FrameLayout.LayoutParams(thicknessPx, lengthPx).apply {
                gravity = android.view.Gravity.CENTER
            }
            container.addView(lineView, stickParams)

            val params = WindowManager.LayoutParams(
                touchAreaPx,
                lengthPx + (20 * density).toInt(), 
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.START
                x = 0
                y = if (isLandscape) 0 else (context.resources.displayMetrics.heightPixels / 2 - lengthPx / 2)
            }

            var initialTouchX = 0f
            var initialTouchY = 0f
            var initialParamX = 0
            var initialParamY = 0
            var isLongPress = false
            
            val longPressRunnable = Runnable {
                isLongPress = true
                lineView.setBackgroundColor(0xFF00FF00.toInt()) // Turn green to indicate drag mode
            }

            container.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        initialParamX = params.x
                        initialParamY = params.y
                        isLongPress = false
                        container.postDelayed(longPressRunnable, 400) // 400ms to enter drag mode
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        
                        if (isLongPress) {
                            params.x = initialParamX + dx.toInt()
                            params.y = initialParamY + dy.toInt()
                            windowManager?.updateViewLayout(container, params)
                        } else {
                            if (Math.abs(dx) > 20 || Math.abs(dy) > 20) {
                                container.removeCallbacks(longPressRunnable)
                                // Swipe detected (Right or Down)
                                if (dx > 20 || dy > 20) {
                                    showFullOverlay(context)
                                }
                            }
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        container.removeCallbacks(longPressRunnable)
                        val drawable = android.graphics.drawable.GradientDrawable().apply {
                            setColor(0x88FFFFFF.toInt())
                            cornerRadius = (4 * density)
                        }
                        lineView.background = drawable
                        
                        if (isLongPress) {
                            // Snap to nearest edge
                            val displayMetrics = context.resources.displayMetrics
                            if (params.x < displayMetrics.widthPixels / 2) {
                                params.x = 0
                            } else {
                                params.x = displayMetrics.widthPixels - touchAreaPx
                            }
                            windowManager?.updateViewLayout(container, params)
                        } else {
                            val dx = event.rawX - initialTouchX
                            val dy = event.rawY - initialTouchY
                            if (Math.abs(dx) < 10 && Math.abs(dy) < 10) {
                                // It was a tap
                                showFullOverlay(context)
                            }
                        }
                        true
                    }
                    else -> false
                }
            }

            windowManager?.addView(container, params)
            singleTriggerView = container
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""
content = re.sub(r'    @SuppressLint\("ClickableViewAccessibility"\)\n    suspend fun showOverlayTrigger.*?(?=    private fun showFullOverlay)', show_overlay_func + '\n\n', content, flags=re.DOTALL)

# Replace references in showFullOverlay and hideFullOverlay and hideOverlay
content = content.replace('leftTriggerView?.visibility = View.GONE\n            rightTriggerView?.visibility = View.GONE', 'singleTriggerView?.visibility = View.GONE')
content = content.replace('leftTriggerView?.visibility = View.VISIBLE\n            rightTriggerView?.visibility = View.VISIBLE', 'singleTriggerView?.visibility = View.VISIBLE')

hide_overlay_func = """    suspend fun hideOverlay() = withContext(Dispatchers.Main) {
        try {
            hideFullOverlay()
            singleTriggerView?.let { windowManager?.removeView(it) }
            singleTriggerView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""
content = re.sub(r'    suspend fun hideOverlay.*\}', hide_overlay_func, content, flags=re.DOTALL)

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'w') as f:
    f.write(content)
