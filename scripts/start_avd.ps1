$ErrorActionPreference = 'Continue'
$ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$adb = "$ANDROID_HOME\platform-tools\adb.exe"
$emu = "$ANDROID_HOME\emulator\emulator.exe"

Write-Host "starting AVD zhuji_avd..."
Start-Process -NoNewWindow -FilePath $emu `
  -ArgumentList @('-avd','zhuji_avd','-no-boot-anim','-no-snapshot-save','-gpu','swiftshader_indirect','-noaudio') `
  -RedirectStandardOutput "$env:TEMP\emu_stdout.log" -RedirectStandardError "$env:TEMP\emu_stderr.log"

Write-Host "waiting boot..."
& $adb wait-for-device
$boot = ''
for ($i = 0; $i -lt 60; $i++) {
    $boot = (& $adb shell getprop sys.boot_completed 2>$null).Trim()
    if ($boot -eq '1') { break }
    Start-Sleep -Seconds 3
}
if ($boot -ne '1') { Write-Error "AVD did not finish boot in 180s" }
Write-Host "AVD ready."
& $adb shell input keyevent 82 | Out-Null
& $adb shell settings put global window_animation_scale 0 | Out-Null
& $adb shell settings put global transition_animation_scale 0 | Out-Null
& $adb shell settings put global animator_duration_scale 0 | Out-Null
