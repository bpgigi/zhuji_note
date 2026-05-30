$ErrorActionPreference = 'Stop'

$script:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$script:ADB = "$script:ANDROID_HOME\platform-tools\adb.exe"
$script:SERIAL = if ($env:ZHUJI_SERIAL) { $env:ZHUJI_SERIAL } else { 'emulator-5554' }
$script:PKG = 'com.zhuji.note'
$script:SCREENS = Join-Path (Split-Path $PSScriptRoot -Parent) 'docs\screens'
$script:UIDUMP = Join-Path $env:TEMP 'zhuji_ui.xml'

function Invoke-Adb { & $script:ADB -s $script:SERIAL @args 2>$null }

function Assert-Device {
    $state = (& $script:ADB -s $script:SERIAL get-state 2>$null)
    if ($state -ne 'device') { throw "AVD $script:SERIAL not ready (state=$state). Start it first." }
}

function Set-ThreeButtonNav {
    Invoke-Adb shell cmd overlay enable-exclusive com.android.internal.systemui.navbar.threebutton | Out-Null
    Start-Sleep -Milliseconds 800
    Invoke-Adb shell settings put global window_animation_scale 0 | Out-Null
    Invoke-Adb shell settings put global transition_animation_scale 0 | Out-Null
    Invoke-Adb shell settings put global animator_duration_scale 0 | Out-Null
}

function Hide-Keyboard {
    $ime = Invoke-Adb shell dumpsys input_method | Select-String 'mInputShown=true'
    if ($ime) { Invoke-Adb shell input keyevent 111 | Out-Null; Start-Sleep -Milliseconds 500 }
}

function Get-UiXml {
    for ($i = 0; $i -lt 3; $i++) {
        Invoke-Adb shell uiautomator dump /sdcard/zhuji_ui.xml | Out-Null
        Invoke-Adb pull /sdcard/zhuji_ui.xml $script:UIDUMP | Out-Null
        if (Test-Path $script:UIDUMP) { return Get-Content -LiteralPath $script:UIDUMP -Raw -Encoding UTF8 }
        Start-Sleep -Milliseconds 400
    }
    throw 'uiautomator dump failed after 3 tries'
}

function Find-Node {
    param([Parameter(Mandatory)][string]$Text, [switch]$Exact)
    $xml = Get-UiXml
    if ([string]::IsNullOrEmpty($xml)) { return $null }
    $best = $null
    foreach ($m in [regex]::Matches($xml, '<node[^>]*?>')) {
        $tag = $m.Value
        $t = if ($tag -match 'text="([^"]*)"') { $matches[1] } else { '' }
        $d = if ($tag -match 'content-desc="([^"]*)"') { $matches[1] } else { '' }
        $label = if ($t) { $t } else { $d }
        if (-not $label) { continue }
        $hit = if ($Exact) { $label -eq $Text } else { $label -like "*$Text*" }
        if (-not $hit) { continue }
        if ($tag -match 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"') {
            $best = [pscustomobject]@{
                Label = $label
                X     = ([int]$matches[1] + [int]$matches[3]) / 2 -as [int]
                Y     = ([int]$matches[2] + [int]$matches[4]) / 2 -as [int]
            }
            break
        }
    }
    return $best
}

function Tap-Text {
    param([Parameter(Mandatory)][string]$Text, [switch]$Exact, [int]$SettleMs = 1200)
    Hide-Keyboard
    $node = Find-Node -Text $Text -Exact:$Exact
    if (-not $node) { throw "Tap-Text: no node matching '$Text'" }
    Invoke-Adb shell input tap $node.X $node.Y | Out-Null
    Start-Sleep -Milliseconds $SettleMs
    return $node
}

function Tap-XY {
    param([int]$X, [int]$Y, [int]$SettleMs = 1000)
    Invoke-Adb shell input tap $X $Y | Out-Null
    Start-Sleep -Milliseconds $SettleMs
}

function Type-Text {
    param([Parameter(Mandatory)][string]$Text)
    $esc = $Text -replace ' ', '%s'
    Invoke-Adb shell input text $esc | Out-Null
    Start-Sleep -Milliseconds 500
}

function Wait-Text {
    param([Parameter(Mandatory)][string]$Text, [int]$TimeoutSec = 60, [int]$PollMs = 1500)
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        $xml = Get-UiXml
        if ($xml -match [regex]::Escape($Text)) { return $true }
        Start-Sleep -Milliseconds $PollMs
    }
    return $false
}

function Wait-TextGone {
    param([Parameter(Mandatory)][string]$Text, [int]$TimeoutSec = 90, [int]$PollMs = 1500)
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        $xml = Get-UiXml
        if ($xml -notmatch [regex]::Escape($Text)) { return $true }
        Start-Sleep -Milliseconds $PollMs
    }
    return $false
}

function Save-Shot {
    param([Parameter(Mandatory)][string]$Name)
    if (-not (Test-Path $script:SCREENS)) { New-Item -ItemType Directory -Path $script:SCREENS -Force | Out-Null }
    $dst = Join-Path $script:SCREENS $Name
    Invoke-Adb shell screencap -p /sdcard/zhuji_shot.png | Out-Null
    Invoke-Adb pull /sdcard/zhuji_shot.png $dst | Out-Null
    return $dst
}

function Restart-App {
    Invoke-Adb shell am force-stop $script:PKG | Out-Null
    Start-Sleep -Milliseconds 800
    Invoke-Adb shell am start -n "$script:PKG/.MainActivity" | Out-Null
    Start-Sleep -Seconds 3
}

function Get-VisibleTexts {
    $xml = Get-UiXml
    $out = @()
    foreach ($m in [regex]::Matches($xml, '<node[^>]*?>')) {
        $tag = $m.Value
        $t = if ($tag -match 'text="([^"]*)"') { $matches[1] } else { '' }
        $d = if ($tag -match 'content-desc="([^"]*)"') { $matches[1] } else { '' }
        $label = if ($t) { $t } else { $d }
        if ($label) { $out += $label }
    }
    return $out
}

function Scroll-FindChip {
    param([Parameter(Mandatory)][string]$Text, [int]$MaxSwipes = 5)
    $node = Find-Node -Text $Text -Exact
    $tries = 0
    while (-not $node -and $tries -lt $MaxSwipes) {
        $row = Find-Node -Text '总结'
        $y = if ($row) { $row.Y } else { 1969 }
        Invoke-Adb shell input swipe 900 $y 200 $y 350 | Out-Null
        Start-Sleep -Milliseconds 600
        $node = Find-Node -Text $Text -Exact
        $tries++
    }
    return $node
}

function Run-AiAction {
    param(
        [Parameter(Mandatory)][string]$Action,
        [Parameter(Mandatory)][string]$ShotName,
        [int]$TimeoutSec = 80
    )
    $chip = Scroll-FindChip -Text $Action
    if (-not $chip) { return "FAIL: chip '$Action' not found" }
    Invoke-Adb shell input tap $chip.X $chip.Y | Out-Null
    $ok = Wait-Text -Text '应用到笔记' -TimeoutSec $TimeoutSec
    Start-Sleep -Milliseconds 800
    Save-Shot -Name $ShotName | Out-Null
    if ($ok) { return "OK: $Action -> $ShotName" } else { return "TIMEOUT: $Action" }
}

function Test-AiActionClean {
    param(
        [Parameter(Mandatory)][string]$Action,
        [Parameter(Mandatory)][string]$ShotName,
        [string]$NoteText = 'Kotlin_Coroutines_Notes',
        [int]$TimeoutSec = 110
    )
    Restart-App
    Tap-Text -Text $NoteText | Out-Null
    Hide-Keyboard
    Tap-Text -Text '打开 AI 助手' | Out-Null
    $chip = Scroll-FindChip -Text $Action
    if (-not $chip) { return "FAIL: chip '$Action' not found" }
    Invoke-Adb shell input tap $chip.X $chip.Y | Out-Null
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    $done = $false
    while ((Get-Date) -lt $deadline) {
        $xml = Get-UiXml
        if ($xml -match '应用到笔记' -or $xml -match '复制') { $done = $true; break }
        Start-Sleep -Milliseconds 2000
    }
    Start-Sleep -Milliseconds 1000
    Save-Shot -Name $ShotName | Out-Null
    if ($done) { return "OK: $Action -> $ShotName" } else { return "TIMEOUT: $Action" }
}
