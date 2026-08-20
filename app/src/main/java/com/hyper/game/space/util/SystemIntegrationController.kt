package com.hyper.game.space.util

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.hyper.game.space.service.GameSpaceNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SystemIntegrationController {
    
    /**
     * Master initialization sequence when a mapped game launches.
     */
    fun onGameLaunched(context: Context, service: AccessibilityService, packageName: String) {
        CoroutineScope(Dispatchers.Default).launch {
            // 1. RAM Optimization & System Cleanup
            SystemOptimizerManager.clearBackgroundRAM(context, setOf(context.packageName))
            
            // 2. Performance & Threading
            ForceFpsManager.prioritizeForegroundThreads()
            
            // Apply Graphic Driver
            if (SettingsManager.getGameDriver(context)) {
                SystemSettingsOverride.overrideGraphicsDriver(context, true)
            }
            
            // 3. Audio Enhancements (Boost high frequencies)
            if (SettingsManager.getFootstepEQ(context)) {
                AudioEngineController.enableFootstepEQ()
            }
            
            // 4. Visual Overlays
            DynamicCrosshairManager.showCrosshair(context, DynamicCrosshairManager.CrosshairConfig())
            
            val profile = when(SettingsManager.getColorProfile(context)) {
                "GRASS_SPOTTER" -> VisionColorOverlayService.Profile.GRASS_SPOTTER
                "HIGH_CONTRAST" -> VisionColorOverlayService.Profile.HIGH_CONTRAST
                "SHADOW_BOOST" -> VisionColorOverlayService.Profile.SHADOW_BOOST
                else -> VisionColorOverlayService.Profile.NONE
            }
            VisionColorOverlayService.applyFilter(context, profile)

            // 5. Virtual Sensitivity Engine
            VirtualSensitivityEngine.xAxisMultiplier = SettingsManager.getVirtualSensitivityX(context)
            VirtualSensitivityEngine.yAxisMultiplier = SettingsManager.getVirtualSensitivityY(context)
            VirtualSensitivityEngine.startEngine(context, service)

            // 6. Hardware Triggers & Macro initialization
            HardwareTriggerMapper.isTriggerEngineActive = true
            
            // Example mappings for volume keys
            HardwareTriggerMapper.mapKey(KeyEvent.KEYCODE_VOLUME_UP, 500f, 500f)
            HardwareTriggerMapper.mapKey(KeyEvent.KEYCODE_VOLUME_DOWN, 800f, 600f)

            // 7. Start Persistent Notification Service
            val intent = Intent(context, GameSpaceNotificationService::class.java).apply {
                putExtra("package_name", packageName)
            }
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Safety teardown sequence when the game exits or goes to the background.
     */
    fun onGameExited(context: Context, service: AccessibilityService) {
        CoroutineScope(Dispatchers.Default).launch {
            // Revert System Driver
            SystemSettingsOverride.overrideGraphicsDriver(context, false)
            
            // Disable hardware triggers
            HardwareTriggerMapper.isTriggerEngineActive = false
            HardwareTriggerMapper.clearMappings()

            // Disable Virtual Sensitivity
            VirtualSensitivityEngine.stopEngine()

            // Cleanup Visual Overlays
            DynamicCrosshairManager.hideCrosshair()
            VisionColorOverlayService.removeFilter()

            // Release Audio Resources
            AudioEngineController.disableAudioEffects()

            // Stop Notification Service
            try {
                val intent = Intent(context, GameSpaceNotificationService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
