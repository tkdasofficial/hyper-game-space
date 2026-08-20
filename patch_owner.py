import re

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'r') as f:
    content = f.read()

# Add imports
content = content.replace('import androidx.lifecycle.LifecycleRegistry', 'import androidx.lifecycle.LifecycleRegistry\nimport androidx.lifecycle.ViewModelStore\nimport androidx.lifecycle.ViewModelStoreOwner\nimport androidx.lifecycle.setViewTreeViewModelStoreOwner')

# Add interface
content = content.replace('class MyLifecycleOwner : SavedStateRegistryOwner {', 'class MyLifecycleOwner : SavedStateRegistryOwner, ViewModelStoreOwner {\n    private val store = ViewModelStore()\n    override val viewModelStore: ViewModelStore get() = store')

# Set owner
content = content.replace('setViewTreeSavedStateRegistryOwner(lifecycleOwner)', 'setViewTreeSavedStateRegistryOwner(lifecycleOwner)\n                setViewTreeViewModelStoreOwner(lifecycleOwner)')

with open('app/src/main/java/com/hyper/game/space/util/GameSpaceOverlayManager.kt', 'w') as f:
    f.write(content)
