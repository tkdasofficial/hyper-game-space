package com.hyper.game.space.service

import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.hyper.game.space.util.HardwareTriggerMapper
import com.hyper.game.space.util.InstalledGamesManager
import com.hyper.game.space.util.SystemIntegrationController

class GameDetectionService : AccessibilityService() {
    private var currentPackage: String? = null
    private var cachedGameSet: Set<String> = emptySet()

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.hyper.game.space.UPDATE_GAMES") {
                val updatedGames = intent.getStringArrayListExtra("games")
                if (updatedGames != null) {
                    cachedGameSet = updatedGames.toSet()
                } else {
                    cachedGameSet = InstalledGamesManager.getSelectedGames(applicationContext)
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        startForegroundGuard()

        try {
            startService(Intent(applicationContext, GameDetectionService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Register BroadcastReceiver for cross-process updates
        val filter = IntentFilter("com.hyper.game.space.UPDATE_GAMES")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(updateReceiver, filter)
        }

        // Load initially
        cachedGameSet = InstalledGamesManager.getSelectedGames(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundGuard()
        return START_STICKY
    }

    private fun startForegroundGuard() {
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
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        
        // Ignore system UI and own package to prevent thrashing
        if (packageName == "com.android.systemui" || packageName == applicationContext.packageName) return

        // State machine: Only trigger when active package changes
        if (packageName != currentPackage) {
            currentPackage = packageName
            checkAndToggleGameSpace(packageName)
        }
    }
    
    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Intercept volume keys when in a game
        return HardwareTriggerMapper.onKeyEvent(event, this) || super.onKeyEvent(event)
    }

    private fun checkAndToggleGameSpace(packageName: String) {
        if (cachedGameSet.contains(packageName)) {
            // Game launched - Execute master initialization
            SystemIntegrationController.onGameLaunched(applicationContext, this, packageName)
        } else {
            // Non-game launched (or game exited) - Execute master teardown
            SystemIntegrationController.onGameExited(applicationContext, this)
        }
    }

    override fun onInterrupt() {
        // Handle interruptions safely
        SystemIntegrationController.onGameExited(applicationContext, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(updateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        SystemIntegrationController.onGameExited(applicationContext, this)
    }
}
