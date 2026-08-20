package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.InstalledGamesManager
import com.example.util.MetricsCollector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MetricsState(
    val fps: Int = 0,
    val ramPercentage: Int = 0,
    val ping: String = "--",
    val gpuLoad: String = "--",
    val cpuLoad: String = "--"
)

data class GameItem(
    val packageName: String,
    val name: String,
    val isSelected: Boolean,
    val isSystemBox: Boolean = false,
    val icon: android.graphics.drawable.Drawable? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val metricsCollector = MetricsCollector(application)

    private val _metricsState = MutableStateFlow(MetricsState())
    val metricsState: StateFlow<MetricsState> = _metricsState

    private val _installedGames = MutableStateFlow<List<GameItem>>(emptyList())
    val installedGames: StateFlow<List<GameItem>> = _installedGames

    init {
        metricsCollector.startFpsMonitor()
        startMetricsLoop()
        loadGames()
    }

    private fun startMetricsLoop() {
        viewModelScope.launch {
            while (isActive) {
                val ram = metricsCollector.getRamUsagePercentage()
                val ping = metricsCollector.getPing()
                val gpu = metricsCollector.getGpuLoadApprox()
                val cpu = metricsCollector.getCpuLoadApprox()
                val fps = metricsCollector.getFps()
                
                _metricsState.value = MetricsState(
                    fps = fps,
                    ramPercentage = ram,
                    ping = ping,
                    gpuLoad = gpu,
                    cpuLoad = cpu
                )
                delay(1000)
            }
        }
    }

    fun loadGames() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val pm = context.packageManager
            
            val dashboardApps = InstalledGamesManager.getDashboardApps(context)
            val selected = InstalledGamesManager.getSelectedGames(context)
            
            val mapped = dashboardApps.map { appInfo ->
                GameItem(
                    packageName = appInfo.packageName,
                    name = appInfo.loadLabel(pm).toString(),
                    isSelected = true, // they are on dashboard, so considered selected/enabled
                    icon = appInfo.loadIcon(pm)
                )
            }
            
            _installedGames.value = mapped
        }
    }

    
    fun toggleGameSelection(packageName: String) {
        if (packageName == "com.example.systembox") return // Prevent toggling the default box
        
        val context = getApplication<Application>()
        val current = InstalledGamesManager.getSelectedGames(context).toMutableSet()
        
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        
        InstalledGamesManager.saveSelectedGames(context, current)
        loadGames() // Refresh UI
    }

    override fun onCleared()
 {
        super.onCleared()
        metricsCollector.stopFpsMonitor()
    }
}
