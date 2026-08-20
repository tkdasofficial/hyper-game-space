with open("app/src/main/java/com/example/ManageAppsActivity.kt", "r") as f:
    content = f.read()

toggle_logic = """            if (isChecked) {
                InstalledGamesManager.addSelectedGame(this, appInfo.packageName)
                selectedPackages.add(appInfo.packageName)
            } else {
                InstalledGamesManager.removeSelectedGame(this, appInfo.packageName)
                selectedPackages.remove(appInfo.packageName)
            }
            filterApps(etSearch.text.toString())"""

content = content.replace("""            if (isChecked) {
                InstalledGamesManager.addSelectedGame(this, appInfo.packageName)
                selectedPackages.add(appInfo.packageName)
            } else {
                InstalledGamesManager.removeSelectedGame(this, appInfo.packageName)
                selectedPackages.remove(appInfo.packageName)
            }""", toggle_logic)

with open("app/src/main/java/com/example/ManageAppsActivity.kt", "w") as f:
    f.write(content)
