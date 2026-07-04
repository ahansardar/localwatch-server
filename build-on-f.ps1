param(
    [ValidateSet("debug", "release", "bundle")]
    [string]$Target = "debug",
    [switch]$Clean
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $projectRoot.StartsWith("F:\", [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "This project must be built from F:. Current path: $projectRoot"
}

$toolchain = "F:\LocalWatch-Toolchain"
$javaHome = Get-ChildItem -LiteralPath "$toolchain\jdk" -Directory |
    Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName "bin\java.exe") } |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $javaHome) { throw "JDK not found under $toolchain\jdk. Run setup-toolchain-on-f.ps1 first." }

$env:JAVA_HOME = $javaHome
$env:ANDROID_HOME = "$toolchain\android-sdk"
$env:ANDROID_SDK_ROOT = "$toolchain\android-sdk"
$env:GRADLE_USER_HOME = "$toolchain\gradle-home"
$env:TEMP = "$toolchain\temp"
$env:TMP = "$toolchain\temp"
$env:USERPROFILE = "$toolchain\user-home"
$env:HOME = "$toolchain\user-home"
$env:HOMEDRIVE = "F:"
$env:HOMEPATH = "\LocalWatch-Toolchain\user-home"
$env:APPDATA = "$toolchain\appdata\roaming"
$env:LOCALAPPDATA = "$toolchain\appdata\local"
$env:ANDROID_USER_HOME = "$toolchain\android-user-home"

@(
    $env:GRADLE_USER_HOME, $env:TEMP, $env:USERPROFILE,
    $env:ANDROID_USER_HOME, $env:APPDATA, $env:LOCALAPPDATA
) | ForEach-Object { New-Item -ItemType Directory -Force -Path $_ | Out-Null }

$tasks = switch ($Target) {
    "debug" { @("testDebugUnitTest", "assembleDebug") }
    "release" { @("testReleaseUnitTest", "assembleRelease") }
    "bundle" { @("testReleaseUnitTest", "bundleRelease") }
}
if ($Clean) { $tasks = @("clean") + $tasks }

Push-Location $projectRoot
try {
    & ".\gradlew.bat" @tasks --no-daemon --stacktrace
    if ($LASTEXITCODE -ne 0) { throw "Gradle exited with code $LASTEXITCODE" }
} finally {
    Pop-Location
}
