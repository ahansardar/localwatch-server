$ErrorActionPreference = "Stop"

$root = "F:\LocalWatch-Toolchain"
$downloads = Join-Path $root "downloads"
$jdkRoot = Join-Path $root "jdk"
$sdkRoot = Join-Path $root "android-sdk"
$tempRoot = Join-Path $root "temp"
New-Item -ItemType Directory -Force -Path $downloads, $jdkRoot, $sdkRoot, $tempRoot | Out-Null
$env:TEMP = $tempRoot
$env:TMP = $tempRoot
$env:USERPROFILE = Join-Path $root "user-home"
$env:HOME = $env:USERPROFILE
$env:HOMEDRIVE = "F:"
$env:HOMEPATH = "\LocalWatch-Toolchain\user-home"
$env:APPDATA = Join-Path $root "appdata\roaming"
$env:LOCALAPPDATA = Join-Path $root "appdata\local"
$env:ANDROID_USER_HOME = Join-Path $root "android-user-home"
$env:GRADLE_USER_HOME = Join-Path $root "gradle-home"
New-Item -ItemType Directory -Force -Path $env:USERPROFILE, $env:ANDROID_USER_HOME, $env:GRADLE_USER_HOME, $env:APPDATA, $env:LOCALAPPDATA | Out-Null

function Get-ReliableDownload {
    param([string]$Uri, [string]$Destination)
    if (Test-Path -LiteralPath $Destination) { return }
    & curl.exe -L --fail --retry 8 --retry-delay 2 --retry-all-errors -o $Destination $Uri
    if ($LASTEXITCODE -ne 0) { throw "Download failed: $Uri" }
}

$jdkZip = Join-Path $downloads "microsoft-jdk-17-windows-x64.zip"
Get-ReliableDownload "https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.zip" $jdkZip
if (-not (Get-ChildItem -LiteralPath $jdkRoot -Directory -ErrorAction SilentlyContinue | Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") })) {
    Expand-Archive -LiteralPath $jdkZip -DestinationPath $jdkRoot
}
$javaHome = Get-ChildItem -LiteralPath $jdkRoot -Directory |
    Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
    Select-Object -First 1 -ExpandProperty FullName

$toolsZip = Join-Path $downloads "commandlinetools-win-14742923_latest.zip"
Get-ReliableDownload "https://dl.google.com/android/repository/commandlinetools-win-14742923_latest.zip" $toolsZip
$latest = Join-Path $sdkRoot "cmdline-tools\latest"
if (-not (Test-Path -LiteralPath (Join-Path $latest "bin\sdkmanager.bat"))) {
    $extract = Join-Path $tempRoot "cmdline-tools"
    if (Test-Path -LiteralPath $extract) { Remove-Item -LiteralPath $extract -Recurse -Force }
    Expand-Archive -LiteralPath $toolsZip -DestinationPath $extract
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $latest) | Out-Null
    Move-Item -LiteralPath (Join-Path $extract "cmdline-tools") -Destination $latest
}

$env:JAVA_HOME = $javaHome
$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_SDK_ROOT = $sdkRoot
$env:GRADLE_USER_HOME = Join-Path $root "gradle-home"
New-Item -ItemType Directory -Force -Path $env:GRADLE_USER_HOME, $env:USERPROFILE, $env:ANDROID_USER_HOME | Out-Null

$sdkManager = Join-Path $latest "bin\sdkmanager.bat"
$yes = (1..30 | ForEach-Object { "y" }) -join "`n"
$yes | & $sdkManager "--sdk_root=$sdkRoot" --licenses | Out-Host
& $sdkManager "--sdk_root=$sdkRoot" "platform-tools" "platforms;android-35" "build-tools;35.0.0"
if ($LASTEXITCODE -ne 0) { throw "Android SDK installation failed with code $LASTEXITCODE" }

Set-Content -LiteralPath "F:\LocalWatch-Server\local.properties" -Value "sdk.dir=F\:\\LocalWatch-Toolchain\\android-sdk" -Encoding ASCII

$gradleZip = Join-Path $downloads "gradle-8.9-bin.zip"
$gradleRoot = Join-Path $root "gradle-8.9"
Get-ReliableDownload "https://services.gradle.org/distributions/gradle-8.9-bin.zip" $gradleZip
if (-not (Test-Path -LiteralPath (Join-Path $gradleRoot "bin\gradle.bat"))) {
    Expand-Archive -LiteralPath $gradleZip -DestinationPath $root
}
Push-Location "F:\LocalWatch-Server"
try {
    & (Join-Path $gradleRoot "bin\gradle.bat") wrapper --gradle-version 8.9 --distribution-type bin
    if ($LASTEXITCODE -ne 0) { throw "Gradle wrapper creation failed with code $LASTEXITCODE" }
} finally {
    Pop-Location
}
Write-Host "F:-only Android toolchain is ready at $root"
