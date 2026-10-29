param(
    [switch]$BuildOnly,
    [string]$DataDirectory = 'data',
    [ValidateSet('auto', 'always', 'never')]
    [string]$ScreenMode = 'auto'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $projectRoot
try {
    $buildPath = Join-Path $projectRoot 'bin/app'
    New-Item -ItemType Directory -Path $buildPath -Force | Out-Null
    $sources = @(Get-ChildItem -LiteralPath src -Recurse -Filter '*.java' | ForEach-Object FullName)
    & javac -encoding UTF-8 -d $buildPath $sources
    if ($LASTEXITCODE -ne 0) {
        throw 'Compilation failed.'
    }
    if ($BuildOnly) {
        return
    }

    $previousInputEncoding = [Console]::InputEncoding
    $previousOutputEncoding = [Console]::OutputEncoding
    try {
        [Console]::InputEncoding = New-Object System.Text.UTF8Encoding($false)
        [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
        & java '-Dfile.encoding=UTF-8' "-Dsmartsite.dataDir=$DataDirectory" "-Dsmartsite.screen=$ScreenMode" -cp $buildPath Main
        if ($LASTEXITCODE -ne 0) {
            throw "Application exited with code $LASTEXITCODE."
        }
    } finally {
        [Console]::InputEncoding = $previousInputEncoding
        [Console]::OutputEncoding = $previousOutputEncoding
    }
} finally {
    Pop-Location
}
