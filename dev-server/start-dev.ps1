$ErrorActionPreference = 'Stop'
$ServerDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ServerDir

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
    if ($process.ExitCode -ne 0) { throw 'Java could not be executed.' }
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
    throw 'Java was not found.'
}

if (-not (Test-Path '.\server.jar')) {
    throw 'server.jar is missing. Run setup-paper.bat first.'
}

$JavaExe = Resolve-JavaExecutable
$javaVersionOutput = Get-JavaVersionText $JavaExe
if ($javaVersionOutput -notmatch 'version\s+"25(?:\.|\")') {
    Write-Host $javaVersionOutput
    Write-Host "[Danta] Java executable: $JavaExe"
    throw 'Java 25 is required. Fix JAVA_HOME/PATH or set DANTA_JAVA_HOME.'
}

if (-not (Test-Path '.\eula.txt')) {
    throw 'eula.txt is missing. Run setup-paper.bat first.'
}
$eula = Get-Content '.\eula.txt' -Raw
if ($eula -notmatch '(?m)^eula=true\s*$') {
    throw 'If you accept the Mojang EULA, change eula=false to eula=true in eula.txt.'
}

Write-Host "[Danta] Java 25 detected: $JavaExe"
Write-Host '[Danta] Paper development server starting...'
& $JavaExe -Xms2G -Xmx4G -jar '.\server.jar' --nogui
