import sys

filepath = "app/src/main/java/com/hyper/game/space/MainActivity.kt"
with open(filepath, "r") as f:
    content = f.read()

# Make sure we import DualEdgeSwipeBox
if "import com.hyper.game.space.ui.screens.gamespace.DualEdgeSwipeBox" not in content:
    content = content.replace("import androidx.compose.ui.geometry.Size", "import androidx.compose.ui.geometry.Size\nimport com.hyper.game.space.ui.screens.gamespace.DualEdgeSwipeBox\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.remember")

# Replace HyperGameSpaceApp(viewModel)
target_1 = "HyperGameSpaceApp(viewModel)"
replacement_1 = """var isOverlayEnabled by remember { mutableStateOf(true) }
                DualEdgeSwipeBox(isEnabled = isOverlayEnabled) {
                    HyperGameSpaceApp(viewModel)
                }"""
content = content.replace(target_1, replacement_1)

# Replace HyperGameSpaceApp() in Preview
target_2 = "HyperGameSpaceApp()"
replacement_2 = """DualEdgeSwipeBox {
            HyperGameSpaceApp()
        }"""
content = content.replace(target_2, replacement_2)

with open(filepath, "w") as f:
    f.write(content)

