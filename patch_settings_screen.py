import re

with open('app/src/main/java/com/hyper/game/space/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Add to categories
content = content.replace('"Performance Mode",', '"Toolbox Trigger Configuration",\n    "Performance Mode",')

# Add to when(selectedCategory)
content = content.replace('"Performance Mode" -> PerformanceModeSettings()', '"Toolbox Trigger Configuration" -> ToolboxTriggerSettings()\n                    "Performance Mode" -> PerformanceModeSettings()')

# Add Composable
toolbox_composable = """@Composable
fun ToolboxTriggerSettings(context: Context = androidx.compose.ui.platform.LocalContext.current) {
    Column(modifier = Modifier.fillMaxSize()) {
        var trigger by remember { mutableStateOf(SettingsManager.getToolboxTriggerType(context)) }
        val triggerTypes = listOf(
            "Floating Icon",
            "Single Finger Swipe (Left Center)",
            "2 Fingers Swipe (Top Corners)",
            "Dual Edge Swipe (Sides)"
        )
        
        DropdownSetting(
            "Opening Method",
            triggerTypes,
            trigger
        ) { 
            trigger = it
            SettingsManager.setToolboxTriggerType(context, it)
        }
    }
}
"""

content = content + "\n" + toolbox_composable

with open('app/src/main/java/com/hyper/game/space/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
