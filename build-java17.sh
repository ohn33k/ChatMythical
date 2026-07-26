#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
BASE="${COBBLEMON_JAR:-$ROOT/inputs/Cobblemon-forge-1.5.2+1.20.1.jar}"
MYTHICAL="${MYTHICAL_JAR:-$ROOT/inputs/MythicalCobbled-2.1.13.jar}"
OCULUS="${OCULUS_JAR:-$ROOT/inputs/oculus-mc1.20.1-1.8.0.jar}"
rm -rf "$ROOT/build" "$ROOT/dist"
mkdir -p "$ROOT/build/main-stub-classes" "$ROOT/build/classes" "$ROOT/build/bridge-classes" "$ROOT/build/patcher-classes" "$ROOT/build/overlay" "$ROOT/dist"
find "$ROOT/tools/compile-stubs" -name '*.java' -print0 | xargs -0 javac --release 17 -d "$ROOT/build/main-stub-classes"
find "$ROOT/src/main/java" -name '*.java' -print0 | xargs -0 javac --release 17 -cp "$BASE:$ROOT/build/main-stub-classes" -d "$ROOT/build/classes"
find "$ROOT/tools/stubs" "$ROOT/src/bridge/java" -name '*.java' -print0 | xargs -0 javac --release 17 -cp "$BASE:$OCULUS" -d "$ROOT/build/bridge-classes"
find "$ROOT/tools/asm" -maxdepth 1 -name '*.java' -print0 | xargs -0 javac -source 17 -target 17 --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED -d "$ROOT/build/patcher-classes"
cp -a "$ROOT/src/main/resources/." "$ROOT/build/overlay/"
python "$ROOT/tools/python/build_compatible_assets.py" --cobblemon "$BASE" --mythical "$MYTHICAL" --output "$ROOT/build/overlay" --audit "$ROOT/generated/compatibility-audit-v0.2.csv" --manifest "$ROOT/generated/compatibility-manifest-v0.2.json" --threshold 0.97
java --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED -cp "$ROOT/build/patcher-classes" PatchSunlitSkins "$BASE" "$ROOT/build/appearance.jar" "$ROOT/build/classes" "ALPHA v0.2 Java 17"
java --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED -cp "$ROOT/build/patcher-classes" PatchBridgeJars "$OCULUS" "$ROOT/dist/oculus-mc1.20.1-1.8.0-SunlitCompatibleSkins-Java17.jar" "$ROOT/build/appearance.jar" "$ROOT/build/render.jar" "$ROOT/build/bridge-classes"
python "$ROOT/tools/python/merge_overlay.py" --base "$ROOT/build/render.jar" --overlay "$ROOT/build/overlay" --output "$ROOT/dist/Cobblemon-forge-1.5.2+1.20.1-SunlitCompatibleSkins-ALPHA-v0.2-Java17.jar"
python "$ROOT/tools/python/generate_grid.py" --manifest "$ROOT/generated/compatibility-manifest-v0.2.json" --output "$ROOT/generated/kubejs/server_scripts/sunlitSkinGrid.js" --columns 8 --rows 8
python "$ROOT/tools/python/verify_java17.py" "$ROOT/dist/Cobblemon-forge-1.5.2+1.20.1-SunlitCompatibleSkins-ALPHA-v0.2-Java17.jar"
echo 'Build complete.'
