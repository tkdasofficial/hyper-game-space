import sys

with open("app/src/main/java/com/hyper/game/space/ui/screens/gamespace/GameSpaceOverlay.kt", "r") as f:
    content = f.read()

content = content.replace("fun DualEdgeSwipeBox(\n    modifier: Modifier = Modifier,\n    content: @Composable () -> Unit\n) {", "fun DualEdgeSwipeBox(\n    modifier: Modifier = Modifier,\n    isEnabled: Boolean = true,\n    content: @Composable () -> Unit\n) {")

content = content.replace("val density = LocalDensity.current", "val density = LocalDensity.current\n    val config = LocalConfiguration.current\n    val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE")

pointer_input_str = "modifier = modifier\n            .fillMaxSize()\n            .pointerInput(Unit) {"
new_pointer_input_str = "modifier = modifier\n            .fillMaxSize()\n            .pointerInput(isEnabled, isLandscape) {\n                if (!isEnabled || !isLandscape) return@pointerInput"

content = content.replace(pointer_input_str, new_pointer_input_str)

with open("app/src/main/java/com/hyper/game/space/ui/screens/gamespace/GameSpaceOverlay.kt", "w") as f:
    f.write(content)

