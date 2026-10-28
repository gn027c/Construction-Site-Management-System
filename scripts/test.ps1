param([string[]]$TestClass)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $projectRoot
try {
    $buildPath = Join-Path $projectRoot 'bin/ws6-tests'
    New-Item -ItemType Directory -Path $buildPath -Force | Out-Null
    $sources = @(Get-ChildItem -LiteralPath src,tests -Recurse -Filter '*.java' | ForEach-Object FullName)
    & javac -encoding UTF-8 -d $buildPath $sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
    if (-not $TestClass) {
        $TestClass = @(Get-ChildItem -LiteralPath tests -Filter '*Test.java' | ForEach-Object BaseName)
    }
    foreach ($class in $TestClass) {
        & java -Dfile.encoding=UTF-8 -cp $buildPath $class
        if ($LASTEXITCODE -ne 0) { throw "Test failed: $class" }
    }
} finally {
    Pop-Location
}
