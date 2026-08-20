import re

with open('app/src/main/java/com/hyper/game/space/service/GameDetectionService.kt', 'r') as f:
    content = f.read()

imports = """import android.app.AlarmManager
import android.app.PendingIntent
import android.os.SystemClock
import android.content.Intent
"""
if "android.app.AlarmManager" not in content:
    content = content.replace("import android.content.Intent", imports)


foreground_func = """    private fun startForegroundGuard() {
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
    }"""

on_start_command = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundGuard()
        return START_STICKY
    }"""
content = re.sub(r'    override fun onStartCommand.*?return START_STICKY\n    \}', on_start_command, content, flags=re.DOTALL)

on_service_connected_new = """    override fun onServiceConnected() {
        super.onServiceConnected()
        startForegroundGuard()
        try {
            startService(Intent(applicationContext, GameDetectionService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }"""
content = content.replace("    override fun onServiceConnected() {\n        super.onServiceConnected()\n        \n        try {", on_service_connected_new)

task_and_unbind = """    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            val restartServiceIntent = Intent(applicationContext, GameDetectionService::class.java)
            restartServiceIntent.setPackage(packageName)
            val restartServicePendingIntent = PendingIntent.getService(
                applicationContext, 
                1, 
                restartServiceIntent, 
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmService = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // Keep components alive conceptually, resetting only non-essential states if needed
        return super.onUnbind(intent)
    }

    override fun onDestroy() {"""

content = content.replace("    override fun onDestroy() {", foreground_func + "\n\n" + task_and_unbind)

with open('app/src/main/java/com/hyper/game/space/service/GameDetectionService.kt', 'w') as f:
    f.write(content)
