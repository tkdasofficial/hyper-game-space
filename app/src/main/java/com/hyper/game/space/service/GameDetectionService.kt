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

class GameDetectionService : AccessibilityService(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var currentPackage: String? = null
    private var cachedGameSet: Set<String> = emptySet()
    private lateinit var prefs: SharedPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        // Explicitly start the service so it transitions to a "started" state.
        // This ensures START_STICKY and stopWithTask="false" are honored if the app is swiped from recents.
        try {
            startService(Intent(applicationContext, GameDetectionService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initialize preferences and cache to prevent heavy I/O on window changes
        prefs = applicationContext.getSharedPreferences("hyper_game_space_prefs", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)
        cachedGameSet = InstalledGamesManager.getSelectedGames(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "selected_games") {
            cachedGameSet = InstalledGamesManager.getSelectedGames(applicationContext)
        }
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
        if (::prefs.isInitialized) {
            prefs.unregisterOnSharedPreferenceChangeListener(this)
        }
        SystemIntegrationController.onGameExited(applicationContext, this)
    }
}
