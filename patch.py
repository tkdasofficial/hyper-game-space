import re

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'r') as f:
    content = f.read()

content = re.sub(r'private var singleTriggerView: View\? = null', 'private var leftTriggerView: View? = null\n    private var rightTriggerView: View? = null\n    private var lastLeftSwipeTime = 0L\n    private var lastRightSwipeTime = 0L', content)

show_overlay_func = """    @SuppressLint("ClickableViewAccessibility")
    suspend fun showOverlayTrigger(context: Context) = withContext(Dispatchers.Main) {
        if (leftTriggerView != null || rightTriggerView != null) return@withContext
        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            val density = context.resources.displayMetrics.density
            
            // Touch area thickness (e.g., 20dp wide zone on the edge)
            val touchThicknessPx = (20 * density).toInt()
            
            // "swipe 2 centimeters" (~20mm is roughly 126dp)
            val swipeDistanceThresholdPx = (126 * density).toInt()

            fun createEdgeTrigger(isLeft: Boolean): View {
                val container = FrameLayout(context)
                container.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                val params = WindowManager.LayoutParams(
                    touchThicknessPx,
                    WindowManager.LayoutParams.MATCH_PARENT, // Full height! Any position works.
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    android.graphics.PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = android.view.Gravity.TOP or if (isLeft) android.view.Gravity.START else android.view.Gravity.END
                }

                var initialTouchX = 0f
                var initialTouchY = 0f
                var hasTriggered = false

                container.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            hasTriggered = false
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (!hasTriggered) {
                                val dx = event.rawX - initialTouchX
                                val dy = event.rawY - initialTouchY
                                
                                val swipeDistance = if (isLeft) dx else -dx
                                
                                // Must swipe inward by ~2cm
                                if (swipeDistance >= swipeDistanceThresholdPx) {
                                    hasTriggered = true
                                    val now = System.currentTimeMillis()
                                    
                                    if (isLeft) {
                                        lastLeftSwipeTime = now
                                        if (now - lastRightSwipeTime <= 1500L) { // 1.5s window
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
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            true
                        }
                        else -> false
                    }
                }

                windowManager?.addView(container, params)
                return container
            }

            leftTriggerView = createEdgeTrigger(isLeft = true)
            rightTriggerView = createEdgeTrigger(isLeft = false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""
content = re.sub(r'    @SuppressLint\("ClickableViewAccessibility"\)\n    suspend fun showOverlayTrigger.*?(?=    private fun showFullOverlay)', show_overlay_func + '\n\n', content, flags=re.DOTALL)

content = content.replace('singleTriggerView?.visibility = View.GONE', 'leftTriggerView?.visibility = View.GONE\n            rightTriggerView?.visibility = View.GONE')
content = content.replace('singleTriggerView?.visibility = View.VISIBLE', 'leftTriggerView?.visibility = View.VISIBLE\n            rightTriggerView?.visibility = View.VISIBLE')

hide_overlay_func = """    suspend fun hideOverlay() = withContext(Dispatchers.Main) {
        try {
            hideFullOverlay()
            leftTriggerView?.let { windowManager?.removeView(it) }
            rightTriggerView?.let { windowManager?.removeView(it) }
            leftTriggerView = null
            rightTriggerView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""
content = re.sub(r'    suspend fun hideOverlay.*\}', hide_overlay_func + '\n}', content, flags=re.DOTALL)

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'w') as f:
    f.write(content)
