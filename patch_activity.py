with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

if "import androidx.compose.ui.platform.LocalContext" not in content:
    content = content.replace("import androidx.compose.ui.platform.LocalConfiguration", "import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.platform.LocalConfiguration")
if "import android.content.Intent" not in content:
    content = content.replace("import android.os.Bundle", "import android.content.Intent\nimport android.os.Bundle")

content = content.replace("com.example.ManageAppsActivity::class.java", "ManageAppsActivity::class.java")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
