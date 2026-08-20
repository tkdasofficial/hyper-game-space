import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

pattern = r'(// Main Content Area\s*\n\s*Row\(\s*\n\s*modifier = Modifier\s*\n\s*\.fillMaxWidth\(\)\s*\n\s*\.align\(Alignment\.Center\)\s*\n\s*\.padding\(top = 64\.dp\),\s*\n\s*verticalAlignment = Alignment\.CenterVertically\s*\n\s*\) \{)(.*?)(\s*\}\s*\n\s*// Top Bar)'

def replacer(match):
    return f"{match.group(1)}\nif (games.isEmpty()) {{ androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {{ androidx.compose.material3.Text(\"No Games Added\", color = androidx.compose.ui.graphics.Color.White, fontSize = 24.sp) }} }} else {{ {match.group(2)} }}\n{match.group(3)}"

content = re.sub(pattern, replacer, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
