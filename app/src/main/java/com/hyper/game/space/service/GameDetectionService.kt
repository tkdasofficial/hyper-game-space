package com.hyper.game.space.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.hyper.game.space.util.HardwareTriggerMapper
import com.hyper.game.space.util.InstalledGamesManager
import com.hyper.game.space.util.SystemIntegrationController

import android.content.BroadcastReceiver
import android.content.IntentFilter

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
        return START_STICKY
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
