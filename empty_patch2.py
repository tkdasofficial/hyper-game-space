import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

pattern = r'(// Main Content Row\s*\n\s*Row\(\s*\n\s*modifier = Modifier\.fillMaxSize\(\),\s*\n\s*verticalAlignment = Alignment\.CenterVertically\s*\n\s*\) \{)(.*?)(\s*\}\s*\n\s*// Top Bar)'

def replacer(match):
    return f"// Main Content Row\nif (games.isEmpty()) {{\n androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {{\n androidx.compose.material3.Text(\"No Games Added\", color = androidx.compose.ui.graphics.Color.White, fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)\n }}\n }} else {{ \n Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {{ {match.group(2)} }} }}\n{match.group(3)}"

content = re.sub(pattern, replacer, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
