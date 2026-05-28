$ErrorActionPreference = 'Continue'
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$proj = 'D:\Projects\zhuji_note'
$report = "$proj\docs\reports\monkey-output.txt"
New-Item -ItemType Directory -Path "$proj\docs\reports" -Force | Out-Null
& $adb -s emulator-5554 shell monkey -p com.zhuji.note --throttle 200 --ignore-crashes --ignore-timeouts --ignore-security-exceptions -v 5000 2>&1 | Out-File -FilePath $report -Encoding UTF8
Write-Output "Monkey test done. Report: $report"
$crashes = Select-String -LiteralPath $report -Pattern "CRASH|ANR" -SimpleMatch | Measure-Object | Select-Object -ExpandProperty Count
Write-Output "Crashes/ANRs found: $crashes"
