import re

# 1. Patch GameDetectionService.kt
with open('app/src/main/java/com/hyper/game/space/service/GameDetectionService.kt', 'r') as f:
    service_content = f.read()

on_start_command = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "accessibility_service_channel"
        val channelName = "Game Space Background Service"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val chan = android.app.NotificationChannel(channelId, channelName, android.app.NotificationManager.IMPORTANCE_MIN)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(chan)
        }
        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setContentTitle("Hyper Game Space Active")
            .setContentText("Monitoring game launches...")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MIN)
            .build()
        
        try {
            startForeground(1001, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }"""

service_content = re.sub(r'    override fun onStartCommand.*?return START_STICKY\n    \}', on_start_command, service_content, flags=re.DOTALL)

with open('app/src/main/java/com/hyper/game/space/service/GameDetectionService.kt', 'w') as f:
    f.write(service_content)

# 2. Patch GameSpaceOverlayManager.kt
with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'r') as f:
    overlay_content = f.read()

target_flags = "WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED"

# Replace triggers (which had FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_NO_LIMITS)
overlay_content = overlay_content.replace(
    'WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS',
    target_flags
)

# Replace full overlay (which had FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_IN_SCREEN or FLAG_HARDWARE_ACCELERATED)
overlay_content = overlay_content.replace(
    'WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED',
    target_flags
)

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'w') as f:
    f.write(overlay_content)
