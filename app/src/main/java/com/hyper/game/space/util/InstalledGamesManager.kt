package com.hyper.game.space.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        
        // Notify the accessibility service (which runs in a separate process)
        val intent = Intent("com.hyper.game.space.UPDATE_GAMES")
        intent.putStringArrayListExtra("games", ArrayList(games))
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
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

    fun isGame(appInfo: ApplicationInfo): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            appInfo.category == ApplicationInfo.CATEGORY_GAME
        } else {
            @Suppress("DEPRECATION")
            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
        }
    }

    suspend fun getDashboardApps(context: Context): List<ApplicationInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selectedGames = getSelectedGames(context)

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        val launcherApps = resolveInfos.map { it.activityInfo.applicationInfo }.distinctBy { it.packageName }
        
        launcherApps.filter { appInfo ->
            isGame(appInfo) || selectedGames.contains(appInfo.packageName)
        }
    }

    suspend fun getAllLauncherApps(context: Context): List<ApplicationInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        resolveInfos.map { it.activityInfo.applicationInfo }.distinctBy { it.packageName }
    }
}
