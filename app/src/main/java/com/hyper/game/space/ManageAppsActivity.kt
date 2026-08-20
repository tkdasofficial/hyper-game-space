package com.hyper.game.space

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hyper.game.space.util.InstalledGamesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageAppsActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var btnBack: ImageView
    private lateinit var adapter: ManageAppsAdapter

    private var allApps: List<ApplicationInfo> = emptyList()
    private var selectedPackages: MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        setContentView(R.layout.activity_manage_apps)

        recyclerView = findViewById(R.id.recyclerView)
        etSearch = findViewById(R.id.etSearch)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = ManageAppsAdapter(this, emptyList()) { appInfo, isChecked ->
            if (isChecked) {
                InstalledGamesManager.addSelectedGame(this, appInfo.packageName)
                selectedPackages.add(appInfo.packageName)
            } else {
                InstalledGamesManager.removeSelectedGame(this, appInfo.packageName)
                selectedPackages.remove(appInfo.packageName)
            }
            filterApps(etSearch.text.toString())
        }
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadApps()
    }

    private fun loadApps() {
        lifecycleScope.launch(Dispatchers.IO) {
            selectedPackages = InstalledGamesManager.getSelectedGames(this@ManageAppsActivity).toMutableSet()
            allApps = InstalledGamesManager.getAllLauncherApps(this@ManageAppsActivity)

            val sortedApps = sortAndGroupApps(allApps)

            withContext(Dispatchers.Main) {
                adapter.updateData(sortedApps, selectedPackages)
            }
        }
    }

    private fun filterApps(query: String) {
        lifecycleScope.launch(Dispatchers.Default) {
            val pm = packageManager
            val filtered = if (query.isEmpty()) {
                allApps
            } else {
                allApps.filter { it.loadLabel(pm).toString().contains(query, ignoreCase = true) }
            }
            val sorted = sortAndGroupApps(filtered)
            withContext(Dispatchers.Main) {
                adapter.updateData(sorted, selectedPackages)
            }
        }
    }

    private fun sortAndGroupApps(apps: List<ApplicationInfo>): List<ApplicationInfo> {
        val pm = packageManager
        // SECTION 1: Added apps (Toggled ON) or Games (if they are treated as ON by default)
        // Wait, the requirement says "Display all currently enabled/added apps/games at the top... with their toggles switched ON".
        // Let's define "is ON" as: app is selected OR app is a game
        val (enabled, disabled) = apps.partition { appInfo ->
            InstalledGamesManager.isGame(appInfo) || selectedPackages.contains(appInfo.packageName)
        }
        
        val sortedEnabled = enabled.sortedBy { it.loadLabel(pm).toString().lowercase() }
        val sortedDisabled = disabled.sortedBy { it.loadLabel(pm).toString().lowercase() }
        
        return sortedEnabled + sortedDisabled
    }
}
