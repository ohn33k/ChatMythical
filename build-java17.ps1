$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Base = if ($env:COBBLEMON_JAR) { $env:COBBLEMON_JAR } else { Join-Path $Root "inputs/Cobblemon-forge-1.5.2+1.20.1.jar" }
$Mythical = if ($env:MYTHICAL_JAR) { $env:MYTHICAL_JAR } else { Join-Path $Root "inputs/MythicalCobbled-2.1.13.jar" }
$Oculus = if ($env:OCULUS_JAR) { $env:OCULUS_JAR } else { Join-Path $Root "inputs/oculus-mc1.20.1-1.8.0.jar" }
$Build = Join-Path $Root "build"
$Dist = Join-Path $Root "dist"
Remove-Item -Recurse -Force $Build,$Dist -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force "$Build/main-stub-classes","$Build/classes","$Build/bridge-classes","$Build/patcher-classes","$Build/overlay",$Dist | Out-Null
$MainStubs = Get-ChildItem "$Root/tools/compile-stubs" -Recurse -Filter *.java | ForEach-Object FullName
& javac --release 17 -d "$Build/main-stub-classes" $MainStubs
$MainSources = Get-ChildItem "$Root/src/main/java" -Recurse -Filter *.java | ForEach-Object FullName
& javac --release 17 -cp "$Base;$Build/main-stub-classes" -d "$Build/classes" $MainSources
$BridgeSources = @(Get-ChildItem "$Root/tools/stubs","$Root/src/bridge/java" -Recurse -Filter *.java | ForEach-Object FullName)
& javac --release 17 -cp "$Base;$Oculus" -d "$Build/bridge-classes" $BridgeSources
$PatchSources = Get-ChildItem "$Root/tools/asm" -Filter *.java | ForEach-Object FullName
& javac -source 17 -target 17 --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED -d "$Build/patcher-classes" $PatchSources
Copy-Item -Path "$Root\src\main\resources\*" -Destination "$Root\build\overlay" -Recurse -Force
& python "$Root/tools/python/build_compatible_assets.py" --cobblemon $Base --mythical $Mythical --output "$Build/overlay" --audit "$Root/generated/compatibility-audit-v0.2.csv" --manifest "$Root/generated/compatibility-manifest-v0.2.json" --threshold 0.97
& java --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED -cp "$Build/patcher-classes" PatchSunlitSkins $Base "$Build/appearance.jar" "$Build/classes" "ALPHA v0.2 Java 17"
& java --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED -cp "$Build/patcher-classes" PatchBridgeJars $Oculus "$Dist/oculus-mc1.20.1-1.8.0-SunlitCompatibleSkins-Java17.jar" "$Build/appearance.jar" "$Build/render.jar" "$Build/bridge-classes"
& python "$Root/tools/python/merge_overlay.py" --base "$Build/render.jar" --overlay "$Build/overlay" --output "$Dist/Cobblemon-forge-1.5.2+1.20.1-SunlitCompatibleSkins-ALPHA-v0.2-Java17.jar"
& python "$Root/tools/python/generate_grid.py" --manifest "$Root/generated/compatibility-manifest-v0.2.json" --output "$Root/generated/kubejs/server_scripts/sunlitSkinGrid.js" --columns 8 --rows 8
& python "$Root/tools/python/verify_java17.py" "$Dist/Cobblemon-forge-1.5.2+1.20.1-SunlitCompatibleSkins-ALPHA-v0.2-Java17.jar"
Write-Host "Build complete."
