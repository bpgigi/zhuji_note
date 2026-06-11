[CmdletBinding()]
param(
    [string]$InputApk,
    [string]$NewAppName,
    [string]$OutputDirectory,
    [switch]$Install,
    [switch]$ReplaceInstalled,
    [switch]$KeepDecoded,
    [switch]$Offline
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if (-not $NewAppName) {
    $NewAppName = [regex]::Unescape("\u52a9\u8bb0 ZhujiNote\uff08APKTool\u9a8c\u8bc1\u7248\uff09")
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$toolDirectory = Join-Path $repoRoot ".apktool-tools"
$workDirectory = Join-Path $repoRoot ".apktool-work"
if (-not $OutputDirectory) {
    $OutputDirectory = Join-Path $repoRoot "dist\apktool"
}

$apktoolVersion = "3.0.2"
$signerVersion = "1.3.0"
$apktoolUrl = "https://github.com/iBotPeaches/Apktool/releases/download/v$apktoolVersion/apktool_$apktoolVersion.jar"
$signerUrl = "https://github.com/patrickfav/uber-apk-signer/releases/download/v$signerVersion/uber-apk-signer-$signerVersion.jar"
$jreUrl = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"
$ciArtifactUrl = "https://nightly.link/bpgigi/zhuji_note/workflows/android-ci.yml/main/app-debug-apk.zip"
$apktoolSha256 = "EEE4669A704A14E0623407E6701B0B91887E61E1E4049CB7A82833E14AE8B5FD"
$signerSha256 = "E1299FD6FCF4DA527DD53735B56127E8EA922A321128123B9C32D619BBA1D835"

function Write-Step([string]$Message) {
    Write-Host "`n==> $Message" -ForegroundColor Cyan
}

function Invoke-External {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code ${LASTEXITCODE}: $FilePath $($Arguments -join ' ')"
    }
}

function Get-FullPath([string]$Path) {
    return $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Path)
}

function Get-VerifiedDownload {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$Destination,
        [string]$ExpectedSha256
    )

    if (-not (Test-Path -LiteralPath $Destination)) {
        if ($Offline) {
            throw "Offline mode is enabled and the required file is missing: $Destination"
        }
        New-Item -ItemType Directory -Force (Split-Path -Parent $Destination) | Out-Null
        Write-Host "Downloading $Url"
        Invoke-WebRequest -UseBasicParsing -Uri $Url -OutFile $Destination
    }

    if ($ExpectedSha256) {
        $actualHash = (Get-FileHash -LiteralPath $Destination -Algorithm SHA256).Hash
        if ($actualHash -ne $ExpectedSha256) {
            Remove-Item -LiteralPath $Destination -Force
            throw "SHA-256 mismatch for $Destination. Expected $ExpectedSha256, got $actualHash."
        }
    }

    return $Destination
}

function Resolve-Java {
    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand) {
        return $javaCommand.Source
    }

    if ($env:JAVA_HOME) {
        $javaHomeCommand = Join-Path $env:JAVA_HOME "bin\java.exe"
        if (Test-Path -LiteralPath $javaHomeCommand) {
            return $javaHomeCommand
        }
    }

    $bundledJava = Get-ChildItem -Path (Join-Path $toolDirectory "jre") -Filter java.exe -Recurse -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
    if ($bundledJava) {
        return $bundledJava
    }

    if ($Offline) {
        throw "Java was not found. Set JAVA_HOME or run once without -Offline."
    }

    Write-Step "Java not found; downloading a portable Eclipse Temurin 21 JRE"
    $jreArchive = Join-Path $toolDirectory "jre21.zip"
    Get-VerifiedDownload -Url $jreUrl -Destination $jreArchive | Out-Null
    $jreDirectory = Join-Path $toolDirectory "jre"
    if (Test-Path -LiteralPath $jreDirectory) {
        Remove-Item -LiteralPath $jreDirectory -Recurse -Force
    }
    New-Item -ItemType Directory -Force $jreDirectory | Out-Null
    Expand-Archive -LiteralPath $jreArchive -DestinationPath $jreDirectory -Force

    $bundledJava = Get-ChildItem -Path $jreDirectory -Filter java.exe -Recurse |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $bundledJava) {
        throw "Portable Java was downloaded but java.exe was not found."
    }
    return $bundledJava
}

function Resolve-Adb {
    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    if ($adbCommand) {
        return $adbCommand.Source
    }

    $sdkRoots = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, "$env:LOCALAPPDATA\Android\Sdk") |
        Where-Object { $_ }
    foreach ($sdkRoot in $sdkRoots) {
        $candidate = Join-Path $sdkRoot "platform-tools\adb.exe"
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }
    return $null
}

function Resolve-ZipAlign {
    $zipAlignCommand = Get-Command zipalign -ErrorAction SilentlyContinue
    if ($zipAlignCommand) {
        return $zipAlignCommand.Source
    }

    $sdkRoots = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, "$env:LOCALAPPDATA\Android\Sdk") |
        Where-Object { $_ }
    foreach ($sdkRoot in $sdkRoots) {
        $candidate = Get-ChildItem -Path (Join-Path $sdkRoot "build-tools") -Filter zipalign.exe -Recurse -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -First 1 -ExpandProperty FullName
        if ($candidate) {
            return $candidate
        }
    }

    $extractedZipAlign = Join-Path $toolDirectory "zipalign.exe"
    if (-not (Test-Path -LiteralPath $extractedZipAlign)) {
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        $archive = [System.IO.Compression.ZipFile]::OpenRead($signerJar)
        try {
            $entry = $archive.Entries |
                Where-Object { $_.FullName -match "^win-zipalign.*[.]exe$" } |
                Select-Object -First 1
            if (-not $entry) {
                throw "The signing tool does not contain a Windows zipalign binary."
            }
            [System.IO.Compression.ZipFileExtensions]::ExtractToFile(
                $entry,
                $extractedZipAlign,
                $true
            )
        }
        finally {
            $archive.Dispose()
        }
    }
    return $extractedZipAlign
}

function Resolve-InputApk {
    if ($InputApk) {
        $resolvedInput = Get-FullPath $InputApk
        if (-not (Test-Path -LiteralPath $resolvedInput)) {
            throw "Input APK does not exist: $resolvedInput"
        }
        return $resolvedInput
    }

    Write-Step "Downloading the latest ZhujiNote CI APK artifact"
    $artifactZip = Join-Path $workDirectory "input\app-debug-apk.zip"
    Get-VerifiedDownload -Url $ciArtifactUrl -Destination $artifactZip | Out-Null
    $artifactDirectory = Join-Path $workDirectory "input\artifact"
    if (Test-Path -LiteralPath $artifactDirectory) {
        Remove-Item -LiteralPath $artifactDirectory -Recurse -Force
    }
    New-Item -ItemType Directory -Force $artifactDirectory | Out-Null
    Expand-Archive -LiteralPath $artifactZip -DestinationPath $artifactDirectory -Force
    $downloadedApk = Get-ChildItem -Path $artifactDirectory -Filter *.apk -Recurse |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $downloadedApk) {
        throw "The CI artifact did not contain an APK."
    }
    return $downloadedApk
}

if ($ReplaceInstalled -and -not $Install) {
    throw "-ReplaceInstalled can only be used together with -Install."
}

New-Item -ItemType Directory -Force $toolDirectory, $workDirectory, $OutputDirectory | Out-Null
$OutputDirectory = Get-FullPath $OutputDirectory

Write-Step "Resolving Java and pinned reverse-engineering tools"
$java = Resolve-Java
$apktoolJar = Join-Path $toolDirectory "apktool_$apktoolVersion.jar"
$signerJar = Join-Path $toolDirectory "uber-apk-signer-$signerVersion.jar"
Get-VerifiedDownload -Url $apktoolUrl -Destination $apktoolJar -ExpectedSha256 $apktoolSha256 | Out-Null
Get-VerifiedDownload -Url $signerUrl -Destination $signerJar -ExpectedSha256 $signerSha256 | Out-Null

$sourceApk = Resolve-InputApk
$decodedDirectory = Join-Path $workDirectory "decoded"
$verificationDirectory = Join-Path $workDirectory "verification-decoded"
$unsignedApk = Join-Path $workDirectory "zhuji-note-apktool-unsigned.apk"
$signedDirectory = Join-Path $workDirectory "signed"

Remove-Item -LiteralPath $decodedDirectory -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $verificationDirectory -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $signedDirectory -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $unsignedApk -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $signedDirectory | Out-Null

Write-Step "Decoding APK with APKTool $apktoolVersion"
Invoke-External -FilePath $java -Arguments @(
    "-jar", $apktoolJar,
    "decode", "--force",
    "--no-src",
    "--output", $decodedDirectory,
    $sourceApk
)

Write-Step "Changing the decoded application label"
$stringsPath = Join-Path $decodedDirectory "res\values\strings.xml"
if (-not (Test-Path -LiteralPath $stringsPath)) {
    throw "Decoded resource file was not found: $stringsPath"
}

$stringsXml = New-Object System.Xml.XmlDocument
$stringsXml.PreserveWhitespace = $true
$stringsXml.Load($stringsPath)
$appNameNode = $stringsXml.SelectSingleNode("/resources/string[@name='app_name']")
if (-not $appNameNode) {
    throw "The app_name resource was not found in $stringsPath"
}
$oldAppName = $appNameNode.InnerText
$appNameNode.InnerText = $NewAppName

$xmlSettings = New-Object System.Xml.XmlWriterSettings
$xmlSettings.Encoding = New-Object System.Text.UTF8Encoding($false)
$xmlSettings.Indent = $false
$xmlWriter = [System.Xml.XmlWriter]::Create($stringsPath, $xmlSettings)
try {
    $stringsXml.Save($xmlWriter)
}
finally {
    $xmlWriter.Dispose()
}
Write-Host "app_name: '$oldAppName' -> '$NewAppName'"

Write-Step "Rebuilding the modified APK"
Invoke-External -FilePath $java -Arguments @(
    "-jar", $apktoolJar,
    "build", $decodedDirectory,
    "--output", $unsignedApk
)

Write-Step "Zip-aligning, signing, and verifying the APK"
$zipAlign = Resolve-ZipAlign
Invoke-External -FilePath $java -Arguments @(
    "-jar", $signerJar,
    "--apks", $unsignedApk,
    "--out", $signedDirectory,
    "--zipAlignPath", $zipAlign,
    "--allowResign"
)

$signedApk = Get-ChildItem -Path $signedDirectory -Filter *.apk |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $signedApk) {
    throw "The signing tool did not produce an APK."
}

Invoke-External -FilePath $java -Arguments @(
    "-jar", $signerJar,
    "--apks", $signedApk,
    "--zipAlignPath", $zipAlign,
    "--onlyVerify"
)

Write-Step "Verifying the modified resource in the final signed APK"
Invoke-External -FilePath $java -Arguments @(
    "-jar", $apktoolJar,
    "decode", "--force",
    "--no-src",
    "--output", $verificationDirectory,
    $signedApk
)
$verificationStringsPath = Join-Path $verificationDirectory "res\values\strings.xml"
$verificationXml = New-Object System.Xml.XmlDocument
$verificationXml.Load($verificationStringsPath)
$verifiedAppName = $verificationXml.SelectSingleNode("/resources/string[@name='app_name']").InnerText
if ($verifiedAppName -ne $NewAppName) {
    throw "Resource verification failed. Expected '$NewAppName', got '$verifiedAppName'."
}
Write-Host "Verified app_name in signed APK: '$verifiedAppName'"

$finalApk = Join-Path $OutputDirectory "zhuji-note-apktool-signed.apk"
Copy-Item -LiteralPath $signedApk -Destination $finalApk -Force

$inputHash = (Get-FileHash -LiteralPath $sourceApk -Algorithm SHA256).Hash
$outputHash = (Get-FileHash -LiteralPath $finalApk -Algorithm SHA256).Hash
$reportPath = Join-Path $OutputDirectory "verification.md"
$report = @"
# APKTool Repackaging Verification

- Verification time: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss K")
- APKTool: $apktoolVersion
- Signing tool: uber-apk-signer $signerVersion
- Original app name: $oldAppName
- Modified app name: $NewAppName
- Original APK SHA-256: $inputHash
- Repackaged APK SHA-256: $outputHash
- Signature and zipalign verification: passed
- Re-decoded app_name verification: passed
- Output APK: $finalApk

Modified resource: ``res/values/strings.xml``, key ``app_name``.
"@
[System.IO.File]::WriteAllText($reportPath, $report, (New-Object System.Text.UTF8Encoding($false)))

if ($Install) {
    Write-Step "Installing and launching the modified APK"
    $adb = Resolve-Adb
    if (-not $adb) {
        throw "adb was not found. Add Android SDK platform-tools to PATH or set ANDROID_HOME."
    }

    & $adb install -r $finalApk
    if ($LASTEXITCODE -ne 0) {
        if (-not $ReplaceInstalled) {
            throw "Installation failed. If the original app is installed with a different signature, rerun with -Install -ReplaceInstalled (this removes its app data)."
        }
        Invoke-External -FilePath $adb -Arguments @("uninstall", "com.zhuji.note")
        Invoke-External -FilePath $adb -Arguments @("install", $finalApk)
    }
    Invoke-External -FilePath $adb -Arguments @(
        "shell", "am", "start", "-n", "com.zhuji.note/.MainActivity"
    )
}

if (-not $KeepDecoded) {
    Remove-Item -LiteralPath $decodedDirectory -Recurse -Force
    Remove-Item -LiteralPath $verificationDirectory -Recurse -Force
}

Write-Host "`nCompleted successfully." -ForegroundColor Green
Write-Host "Signed APK: $finalApk"
Write-Host "Report:     $reportPath"
