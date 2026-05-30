# ZhujiNote 全功能逐步截图脚本
# 用法: pwsh -File full_walkthrough.ps1
# 设备已 boot、APK 已装。本脚本自身会执行完毕(有界 sleep)，不会无限阻塞。
$ErrorActionPreference = 'Continue'
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$dev = 'emulator-5554'
$dst = 'D:\Projects\zhuji_note\docs\screens'
$apiKey = $env:ZHUJI_DEEPSEEK_KEY
New-Item -ItemType Directory -Path $dst -Force | Out-Null

function Shot($name) {
    & $adb -s $dev shell screencap -p /sdcard/zj.png
    & $adb -s $dev pull /sdcard/zj.png "$dst\$name" 2>&1 | Out-Null
    Write-Output "  shot: $name"
}
function Tap($x, $y) { & $adb -s $dev shell input tap $x $y; Start-Sleep -Milliseconds 700 }
function TapW($x, $y, $sec) { & $adb -s $dev shell input tap $x $y; Start-Sleep -Seconds $sec }
function Type($t) { & $adb -s $dev shell input text "$t"; Start-Sleep -Milliseconds 500 }
function Swipe($x1,$y1,$x2,$y2,$ms) { & $adb -s $dev shell input swipe $x1 $y1 $x2 $y2 $ms; Start-Sleep -Milliseconds 600 }
function Back() { & $adb -s $dev shell input keyevent 4; Start-Sleep -Milliseconds 700 }
function Night($on) { & $adb -s $dev shell "cmd uimode night $on"; Start-Sleep -Seconds 1 }

Write-Output "=== 脚本开始 ==="
& $adb -s $dev shell am force-stop com.zhuji.note
& $adb -s $dev shell am start -n com.zhuji.note/.MainActivity | Out-Null
Start-Sleep -Seconds 3
Write-Output "ready"
