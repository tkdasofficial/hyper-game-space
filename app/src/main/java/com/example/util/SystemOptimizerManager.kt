package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SystemOptimizerManager {

    /**
     * Retrieves the current system memory profile.
     */
    suspend fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo = withContext(Dispatchers.IO) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        memoryInfo
    }

    /**
     * Forcefully flushes non-essential background processes to free up RAM.
     * Uses KILL_BACKGROUND_PROCESSES permission.
     */
    suspend fun clearBackgroundRAM(context: Context, excludePackages: Set<String> = emptySet()): Int = withContext(Dispatchers.IO) {
        var killedCount = 0
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            val myPackage = context.packageName

            for (appInfo in packages) {
                val packageName = appInfo.packageName
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                
                // Do not kill system applications or ourselves
                if (!isSystemApp && packageName != myPackage && !excludePackages.contains(packageName)) {
                    activityManager.killBackgroundProcesses(packageName)
                    killedCount++
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        killedCount
    }
}
