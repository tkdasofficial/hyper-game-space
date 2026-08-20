import re

with open("app/src/main/java/com/example/MainViewModel.kt", "r") as f:
    content = f.read()

toggle_code = """
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
"""
content = content.replace("override fun onCleared()", toggle_code)

with open("app/src/main/java/com/example/MainViewModel.kt", "w") as f:
    f.write(content)
