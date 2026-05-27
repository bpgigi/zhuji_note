$ErrorActionPreference = 'Stop'

$ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
if (-not (Test-Path "$ANDROID_HOME\platform-tools\adb.exe")) {
    throw "Android SDK not found at $ANDROID_HOME"
}
$adb = "$ANDROID_HOME\platform-tools\adb.exe"
$out = Join-Path -Path (Resolve-Path 'docs\screens') -ChildPath ("{0:yyyyMMdd-HHmmss}.png" -f (Get-Date))
& $adb shell screencap -p /sdcard/zhuji_capture.png
& $adb pull /sdcard/zhuji_capture.png $out | Out-Null
& $adb shell rm /sdcard/zhuji_capture.png | Out-Null
Write-Host "saved: $out"
