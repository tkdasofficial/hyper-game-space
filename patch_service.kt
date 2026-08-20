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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Try to restart the service if it's removed from recents
        val restartIntent = Intent(applicationContext, this.javaClass)
        restartIntent.setPackage(packageName)
        // startService(restartIntent) 
    }
