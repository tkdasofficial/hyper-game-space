package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object InstalledGamesManager {
    private const val PREFS_NAME = "hyper_game_space_prefs"
    private const val KEY_SELECTED_GAMES = "selected_games"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedGames(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_SELECTED_GAMES, emptySet()) ?: emptySet()
    }

    fun saveSelectedGames(context: Context, games: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_SELECTED_GAMES, games).apply()
    }

    fun addSelectedGame(context: Context, packageName: String) {
        val current = getSelectedGames(context).toMutableSet()
        current.add(packageName)
        saveSelectedGames(context, current)
    }

    fun removeSelectedGame(context: Context, packageName: String) {
        val current = getSelectedGames(context).toMutableSet()
        current.remove(packageName)
        saveSelectedGames(context, current)
    }

    fun getInstalledGames(context: Context): List<ApplicationInfo> {
        val pm = context.packageManager
        
        // Retrieve all applications
        val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        // Filter out system apps, or selectively include categorised games
        return allApps.filter { appInfo ->
            val isUserApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            val isGameCategory = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }
            
            isUserApp || isGameCategory
        }
    }
}
