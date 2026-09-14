$ErrorActionPreference = 'Stop'

$GradleVersion = '9.7.1'
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$ToolsDir = Join-Path $ProjectRoot '.tools'
$GradleHome = Join-Path $ToolsDir "gradle-$GradleVersion"
$GradleBat = Join-Path $GradleHome 'bin\gradle.bat'
$DistributionZip = Join-Path $ToolsDir "gradle-$GradleVersion-bin.zip"
$DistributionUrl = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
$PluginDir = Join-Path $PSScriptRoot 'plugins'

function Get-JavaExe {
    if ($env:DANTA_JAVA_HOME) {
        $candidate = Join-Path $env:DANTA_JAVA_HOME 'bin\java.exe'
        if (Test-Path $candidate) { return $candidate }
        throw "DANTA_JAVA_HOME is set but java.exe was not found: $candidate"
    }
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME 'bin\java.exe'
        if (Test-Path $candidate) { return $candidate }
    }
    $cmd = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    throw 'Java was not found. Install JDK 25 or set DANTA_JAVA_HOME.'
}

function Get-JavaMajor([string]$JavaExe) {
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $JavaExe
    $psi.Arguments = '-version'
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi

    if (-not $process.Start()) {
        throw "Failed to start Java: $JavaExe"
    }

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    $combined = ($stderr + "`n" + $stdout).Trim()
    $first = ($combined -split "`r?`n" | Where-Object { $_.Trim() -ne '' } | Select-Object -First 1)

    if ($first -match 'version\s+"([0-9]+)') {
        return [int]$Matches[1]
    }

    throw "Could not detect Java version from: $first"
}

Write-Host '[Danta] DEV build and deploy'
Write-Host "[Danta] Gradle $GradleVersion / Java 25 / Paper 26.2 build 123"

$JavaExe = Get-JavaExe
$JavaMajor = Get-JavaMajor $JavaExe
if ($JavaMajor -ne 25) {
    throw "Java 25 is required. Detected Java $JavaMajor at $JavaExe. Set DANTA_JAVA_HOME to your JDK 25 folder if Java 21 must remain the Windows default."
}
$JavaHome = Split-Path (Split-Path $JavaExe -Parent) -Parent
$env:JAVA_HOME = $JavaHome
$env:PATH = (Join-Path $JavaHome 'bin') + ';' + $env:PATH
Write-Host "[Danta] Java 25 detected: $JavaHome"

New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null
New-Item -ItemType Directory -Force -Path $PluginDir | Out-Null

if (-not (Test-Path $GradleBat)) {
    Write-Host "[Danta] Downloading Gradle $GradleVersion..."
    if (Test-Path $DistributionZip) { Remove-Item $DistributionZip -Force }
    Invoke-WebRequest -Uri $DistributionUrl -OutFile $DistributionZip -UseBasicParsing
    Write-Host '[Danta] Extracting Gradle...'
    Expand-Archive -Path $DistributionZip -DestinationPath $ToolsDir -Force
    Remove-Item $DistributionZip -Force
}

if (-not (Test-Path $GradleBat)) {
    throw "Gradle bootstrap failed: $GradleBat not found."
}

Write-Host '[Danta] Building paper-plugin...'
Push-Location $ProjectRoot
try {
    & $GradleBat --no-daemon --console=plain clean ':paper-plugin:jar'
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE." }
} finally {
    Pop-Location
}

$Jar = Get-ChildItem (Join-Path $ProjectRoot 'paper-plugin\build\libs') -Filter 'danta-server-*.jar' |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $Jar) { throw 'Built plugin JAR was not found.' }

Get-ChildItem $PluginDir -Filter 'danta-server-*.jar' -ErrorAction SilentlyContinue | Remove-Item -Force
$Destination = Join-Path $PluginDir $Jar.Name
Copy-Item $Jar.FullName $Destination -Force

Write-Host ''
Write-Host '[Danta] Build and deploy complete.'
Write-Host "[Danta] Plugin: $Destination"
Write-Host '[Danta] Next:'
Write-Host '[Danta] 1) Stop the dev server if it is running.'
Write-Host '[Danta] 2) Run start-dev.bat.'
Write-Host '[Danta] 3) Join and run /danta.'
