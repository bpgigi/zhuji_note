$ErrorActionPreference = 'Continue'
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$proj = 'D:\Projects\zhuji_note'
$dst = "$proj\docs\screens"
New-Item -ItemType Directory -Path $dst -Force | Out-Null

function Screenshot($name) {
    & $adb -s emulator-5554 shell screencap -p /sdcard/zj.png
    & $adb -s emulator-5554 pull /sdcard/zj.png "$dst\$name" 2>&1 | Out-Null
    Write-Output "  -> $name"
}

Write-Output "=== UIAutomator E2E Screenshot Flow ==="
& $adb -s emulator-5554 shell am force-stop com.zhuji.note
& $adb -s emulator-5554 shell am start -n com.zhuji.note/.MainActivity
Start-Sleep -Seconds 4
Screenshot "e2e-01-home.png"

& $adb -s emulator-5554 shell input tap 540 2300
Start-Sleep -Seconds 2
Screenshot "e2e-02-new-note.png"

& $adb -s emulator-5554 shell input tap 540 500
Start-Sleep -Seconds 1
& $adb -s emulator-5554 shell input text "TestNote"
Start-Sleep -Seconds 1
Screenshot "e2e-03-typed-title.png"

& $adb -s emulator-5554 shell input keyevent 4
Start-Sleep -Seconds 2
Screenshot "e2e-04-back-to-list.png"

& $adb -s emulator-5554 shell input tap 950 316
Start-Sleep -Seconds 2
Screenshot "e2e-05-settings.png"

& $adb -s emulator-5554 shell input keyevent 4
Start-Sleep -Seconds 1
& $adb -s emulator-5554 shell input tap 700 316
Start-Sleep -Seconds 2
Screenshot "e2e-06-stats.png"

& $adb -s emulator-5554 shell input keyevent 4
Start-Sleep -Seconds 1
& $adb -s emulator-5554 shell input tap 825 316
Start-Sleep -Seconds 2
Screenshot "e2e-07-trash.png"

& $adb -s emulator-5554 shell "cmd uimode night yes"
Start-Sleep -Seconds 2
& $adb -s emulator-5554 shell input keyevent 4
Start-Sleep -Seconds 1
Screenshot "e2e-08-home-dark.png"

& $adb -s emulator-5554 shell "cmd uimode night no"
Write-Output "=== Done ==="
