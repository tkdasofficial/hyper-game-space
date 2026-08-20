import re

with open('app/src/main/java/com/hyper/game/space/util/InstalledGamesManager.kt', 'r') as f:
    content = f.read()

save_games_new = """    fun saveSelectedGames(context: Context, games: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_SELECTED_GAMES, games).apply()
        
        // Notify the accessibility service (which runs in a separate process)
        val intent = Intent("com.hyper.game.space.UPDATE_GAMES")
        intent.putStringArrayListExtra("games", ArrayList(games))
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }"""

content = re.sub(r'    fun saveSelectedGames\(context: Context, games: Set<String>\) \{.*?\}', save_games_new, content, flags=re.DOTALL)

with open('app/src/main/java/com/hyper/game/space/util/InstalledGamesManager.kt', 'w') as f:
    f.write(content)
