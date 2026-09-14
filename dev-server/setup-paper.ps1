$ErrorActionPreference = 'Stop'

$MinecraftVersion = '26.2'
$PaperBuild = 123
$UserAgent = 'DantaServerDev/0.1'
$ServerDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$JarPath = Join-Path $ServerDir 'server.jar'

Write-Host '[Danta] DEV-003 Paper development server setup'
Write-Host "[Danta] Minecraft $MinecraftVersion / Paper build $PaperBuild / Java 25"

# Keep this script ASCII-only so it works reliably in Windows PowerShell 5.1.
function Get-JavaVersionText([string]$JavaExe) {
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $JavaExe
    $psi.Arguments = '-version'
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi
    [void]$process.Start()
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    if ($process.ExitCode -ne 0) {
        throw 'Java could not be executed. Check your Java installation and PATH.'
    }
    return ($stdout + $stderr)
}

function Resolve-JavaExecutable {
    if ($env:DANTA_JAVA_HOME) {
        $candidate = Join-Path $env:DANTA_JAVA_HOME 'bin\java.exe'
        if (Test-Path $candidate) { return $candidate }
    }
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME 'bin\java.exe'
        if (Test-Path $candidate) {
            $text = Get-JavaVersionText $candidate
            if ($text -match 'version\s+"25(?:\.|\")') { return $candidate }
        }
    }
    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand) { return $javaCommand.Source }
    throw 'Java was not found. Install a Java 25 JDK, then run this script again.'
}

$JavaExe = Resolve-JavaExecutable
$javaVersionOutput = Get-JavaVersionText $JavaExe
if ($javaVersionOutput -notmatch 'version\s+"25(?:\.|\")') {
    Write-Host $javaVersionOutput
    Write-Host "[Danta] Java executable: $JavaExe"
    throw 'This project is locked to Java 25. Install Java 25 and make JAVA_HOME/PATH point to it, or set DANTA_JAVA_HOME.'
}
Write-Host "[Danta] Java 25 detected: $JavaExe"

$headers = @{ 'User-Agent' = $UserAgent }
$buildsUrl = "https://fill.papermc.io/v3/projects/paper/versions/$MinecraftVersion/builds"
Write-Host "[Danta] Looking up Paper $MinecraftVersion build $PaperBuild..."
$builds = Invoke-RestMethod -Uri $buildsUrl -Headers $headers
$build = $builds | Where-Object { $_.id -eq $PaperBuild } | Select-Object -First 1
if (-not $build) {
    throw "Paper $MinecraftVersion build $PaperBuild was not found. Re-check the DEV-001 version lock."
}
if ($build.channel -ne 'STABLE') {
    throw "Locked build $PaperBuild is not marked STABLE. The script will not switch builds automatically."
}

$download = $build.downloads.'server:default'
if (-not $download -or -not $download.url) {
    throw 'Could not find the Paper server JAR download URL.'
}

$needDownload = $true
if (Test-Path $JarPath) {
    if ($download.checksums.sha256) {
        $currentHash = (Get-FileHash -Algorithm SHA256 $JarPath).Hash.ToLowerInvariant()
        if ($currentHash -eq $download.checksums.sha256.ToLowerInvariant()) {
            $needDownload = $false
            Write-Host '[Danta] Existing server.jar passed SHA-256 verification.'
        }
    }
}

if ($needDownload) {
    Write-Host "[Danta] Downloading $($download.name)..."
    Invoke-WebRequest -Uri $download.url -Headers $headers -OutFile $JarPath
    if ($download.checksums.sha256) {
        $actualHash = (Get-FileHash -Algorithm SHA256 $JarPath).Hash.ToLowerInvariant()
        $expectedHash = $download.checksums.sha256.ToLowerInvariant()
        if ($actualHash -ne $expectedHash) {
            Remove-Item $JarPath -Force
            throw 'Downloaded Paper JAR failed SHA-256 verification.'
        }
        Write-Host '[Danta] Paper JAR passed SHA-256 verification.'
    }
}

$eulaPath = Join-Path $ServerDir 'eula.txt'
if (-not (Test-Path $eulaPath)) {
    @"
# Set this to true only if you accept the Mojang EULA:
# https://aka.ms/MinecraftEULA
eula=false
"@ | Set-Content -Path $eulaPath -Encoding ascii
    Write-Host '[Danta] Created eula.txt.'
}

Write-Host ''
Write-Host '[Danta] Setup complete.'
Write-Host '[Danta] 1) Review eula.txt and set eula=true if you accept the Mojang EULA.'
Write-Host '[Danta] 2) Run start-dev.bat.'
