#!/bin/bash
set -e

# Update build.gradle.kts
sed -i 's/namespace = "com.example"/namespace = "com.hyper.game.space"/' app/build.gradle.kts
sed -i 's/applicationId = "com.aistudio.hypergamespace.rnxq"/applicationId = "com.hyper.game.space"/' app/build.gradle.kts

# Rename src/main
mkdir -p app/src/main/java/com/hyper/game/space
mv app/src/main/java/com/example/* app/src/main/java/com/hyper/game/space/
rm -rf app/src/main/java/com/example

# Rename src/test
mkdir -p app/src/test/java/com/hyper/game/space
mv app/src/test/java/com/example/* app/src/test/java/com/hyper/game/space/
rm -rf app/src/test/java/com/example

# Rename src/androidTest
mkdir -p app/src/androidTest/java/com/hyper/game/space
mv app/src/androidTest/java/com/example/* app/src/androidTest/java/com/hyper/game/space/
rm -rf app/src/androidTest/java/com/example

# Find all files and replace package/import strings
find app/src -type f -name "*.kt" -exec sed -i 's/package com.example/package com.hyper.game.space/g' {} +
find app/src -type f -name "*.kt" -exec sed -i 's/import com.example/import com.hyper.game.space/g' {} +

# Additionally search for com.example string in kotlin files just in case
find app/src -type f -name "*.kt" -exec sed -i 's/"com.example.systembox"/"com.hyper.game.space.systembox"/g' {} +

echo "Done"
