import re

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'r') as f:
    content = f.read()

# Replace singleTriggerView with triggerViews
content = content.replace('private var singleTriggerView: View? = null', 'private var triggerViews: MutableList<View> = mutableListOf()\n    private var lastLeftSwipeTime = 0L\n    private var lastRightSwipeTime = 0L')

show_overlay_func = """    @SuppressLint("ClickableViewAccessibility")
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
                    val container = FrameLayout(context)
                    val params = WindowManager.LayoutParams(
                        touchThicknessPx,
                        (150 * density).toInt(),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
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
                val container = FrameLayout(context)
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
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
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
                    val container = FrameLayout(context)
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
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
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

                val container = FrameLayout(context)
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
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
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
    }"""
content = re.sub(r'    @SuppressLint\("ClickableViewAccessibility"\)\n    suspend fun showOverlayTrigger.*?(?=    private fun showFullOverlay)', show_overlay_func + '\n\n', content, flags=re.DOTALL)

# Modify visibility updates
content = content.replace('singleTriggerView?.visibility = View.GONE', 'triggerViews.forEach { it.visibility = View.GONE }')
content = content.replace('singleTriggerView?.visibility = View.VISIBLE', 'triggerViews.forEach { it.visibility = View.VISIBLE }')

# Modify hideOverlay
hide_overlay_func = """    suspend fun hideOverlay() = withContext(Dispatchers.Main) {
        try {
            hideFullOverlay()
            triggerViews.forEach { view ->
                try { windowManager?.removeView(view) } catch (e: Exception) {}
            }
            triggerViews.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""
content = re.sub(r'    suspend fun hideOverlay.*\}', hide_overlay_func + '\n}', content, flags=re.DOTALL)

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'w') as f:
    f.write(content)
