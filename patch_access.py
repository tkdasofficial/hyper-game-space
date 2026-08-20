import re

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'overlayComposeView = ComposeView(context).apply {',
    'overlayComposeView = ComposeView(context).apply {\n                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS'
)

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'w') as f:
    f.write(content)
