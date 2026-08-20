import re

with open('app/src/main/java/com/hyper/game/space/service/GameDetectionService.kt', 'r') as f:
    content = f.read()

# Replace SharedPreferences listener
content = content.replace("class GameDetectionService : AccessibilityService(), SharedPreferences.OnSharedPreferenceChangeListener {", 
"""import android.content.BroadcastReceiver
import android.content.IntentFilter

class GameDetectionService : AccessibilityService() {""")

on_service_connected_new = """    private val updateReceiver = object : BroadcastReceiver() {
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
    }"""

content = re.sub(r'    override fun onServiceConnected\(\) \{.*?cachedGameSet = InstalledGamesManager\.getSelectedGames\(applicationContext\)\n    \}', on_service_connected_new, content, flags=re.DOTALL)

# Remove onSharedPreferenceChanged
content = re.sub(r'    override fun onSharedPreferenceChanged.*?\n    \}', '', content, flags=re.DOTALL)

# Update onDestroy
on_destroy_new = """    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(updateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        SystemIntegrationController.onGameExited(applicationContext, this)
    }"""
content = re.sub(r'    override fun onDestroy\(\) \{.*?\n    \}', on_destroy_new, content, flags=re.DOTALL)

# Remove unused vars
content = content.replace("    private lateinit var prefs: SharedPreferences", "")

with open('app/src/main/java/com/hyper/game/space/service/GameDetectionService.kt', 'w') as f:
    f.write(content)
