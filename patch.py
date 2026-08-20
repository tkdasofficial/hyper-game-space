import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add DisposableEffect imports
if "import androidx.compose.runtime.DisposableEffect" not in content:
    content = content.replace("import androidx.compose.runtime.remember", "import androidx.compose.runtime.DisposableEffect\nimport androidx.compose.runtime.remember")

# Add LocalLifecycleOwner and LocalContext
if "import androidx.compose.ui.platform.LocalContext" not in content:
    content = content.replace("import androidx.compose.ui.platform.LocalConfiguration", "import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.platform.LocalConfiguration")
if "import androidx.lifecycle.compose.LocalLifecycleOwner" not in content:
    content = content.replace("import androidx.lifecycle.compose.collectAsStateWithLifecycle", "import androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport androidx.lifecycle.compose.LocalLifecycleOwner")

# Insert DisposableEffect
effect_code = """    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadGames()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
"""
content = re.sub(r'(var showSettings by remember { mutableStateOf\(false\) }\n)', r'\1\n' + effect_code, content)

# Change Add button
content = content.replace('IconButton(onClick = {}) {', 'IconButton(onClick = { context.startActivity(Intent(context, com.example.ManageAppsActivity::class.java)) }) {')

# Empty state logic
empty_state_code = """
            if (games.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Games Added",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
"""
# find the start of the Row that contains the list and hero and wrap them (or just the list and hero)
# actually, it's easier to just replace the row contents if empty, but wait, the top bar is inside Box.
# Let's replace the Box content:
# The structure is: Box(modifier = Modifier.fillMaxSize().background(Color.Black)) { Row(...) { /* Left Area */ ... /* Center Area */ ... /* Right Area */ ... } Row(...) { /* Top Bar */ } }

# If games.isEmpty(), show empty state instead of the middle Row.
pattern = r'(// Main Content Area\n\s*Row\(\n\s*modifier = Modifier\n\s*\.fillMaxWidth\(\)\n\s*\.align\(Alignment\.Center\)\n\s*\.padding\(top = 64\.dp\),\n\s*verticalAlignment = Alignment\.CenterVertically\n\s*\) \{)(.*?)(\s*\}\n\s*// Top Bar)'

def replacer(match):
    return f"{match.group(1)}\nif (games.isEmpty()) {{ Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {{ Text(\"No Games Added\", color = Color.White, fontSize = 24.sp) }} }} else {{ {match.group(2)} }} \n{match.group(3)}"

content = re.sub(pattern, replacer, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
