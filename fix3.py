with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace('Text("Play", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)\n                } } }', 'Text("Play", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)\n                } }')

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
