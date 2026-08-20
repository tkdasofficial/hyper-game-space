import re

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'r') as f:
    content = f.read()

# Replace left/right with single
content = re.sub(r'    private var leftTriggerView: View\? = null\n    private var rightTriggerView: View\? = null\n    private var lastLeftSwipeTime = 0L\n    private var lastRightSwipeTime = 0L', '    private var singleTriggerView: View? = null', content)

show_overlay_func = """    @SuppressLint("ClickableViewAccessibility")
    suspend fun showOverlayTrigger(context: Context) = withContext(Dispatchers.Main) {
        if (singleTriggerView != null) return@withContext
        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            val density = context.resources.displayMetrics.density
            val isLandscape = context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            
            // 2 centimeters long ~ 75dp, 4dp thick
            val lengthPx = (75 * density).toInt()
            val thicknessPx = (4 * density).toInt()
            val touchAreaPx = (32 * density).toInt()

            val container = FrameLayout(context)
            
            val lineView = View(context).apply {
                val drawable = android.graphics.drawable.GradientDrawable().apply {
                    setColor(0x88FFFFFF.toInt()) // Semi-transparent white
                    cornerRadius = (4 * density)
                }
                background = drawable
            }
            
            val stickParams = FrameLayout.LayoutParams(thicknessPx, lengthPx).apply {
                gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.START
            }
            container.addView(lineView, stickParams)

            val params = WindowManager.LayoutParams(
                touchAreaPx,
                lengthPx + (40 * density).toInt(), 
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.START
                x = 0
                y = (context.resources.displayMetrics.heightPixels / 2 - lengthPx / 2) // Fixed center-left place
            }

            var initialTouchX = 0f
            var hasTriggered = false
            
            container.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialTouchX = event.rawX
                        hasTriggered = false
                        lineView.setBackgroundColor(0xFF00FF00.toInt()) // Green to show touch
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!hasTriggered) {
                            val dx = event.rawX - initialTouchX
                            
                            // Fixed path: swipe right by 30dp to open
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
            singleTriggerView = container
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""
content = re.sub(r'    @SuppressLint\("ClickableViewAccessibility"\)\n    suspend fun showOverlayTrigger.*?(?=    private fun showFullOverlay)', show_overlay_func + '\n\n', content, flags=re.DOTALL)

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
content = re.sub(r'    suspend fun hideOverlay.*\}', hide_overlay_func + '\n}', content, flags=re.DOTALL)

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'w') as f:
    f.write(content)
