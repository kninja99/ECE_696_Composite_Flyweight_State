<#
.SYNOPSIS
    Load-tests all three echo-server concurrency strategies with Apache JMeter and
    generates the comparison graphs.

.DESCRIPTION
    For each strategy (single-threaded, thread-per-connection, thread-pool) and each
    concurrent-client count (5, 10, 50, 100, 500) this script:
      1. starts the server (java -cp target/classes ...),
      2. runs the parameterised JMeter plan jmeter/echo_load_test.jmx against it for
         a fixed duration, writing results/<STRATEGY>_<clients>.jtl,
      3. stops the server.
    It then runs ChartGenerator to produce results/summary.csv, results/throughput.png
    and results/latency.png.

    The run is time-bounded (-Duration seconds per cell), so even the single-threaded
    server (~10 connections/second under the 100 ms overhead) finishes quickly.

.PREREQUISITES
    - A JDK on PATH (java, javac). Tested with JDK 17+.
    - Apache JMeter on PATH (the 'jmeter' command). Download:
      https://jmeter.apache.org/download_jmeter.cgi  (unzip, add bin/ to PATH)
    - The project compiled. If target/classes is missing this script compiles the
      server/client classes for you with javac. ChartGenerator needs JFreeChart, so
      chart generation prefers Maven (mvn) or a cached JFreeChart jar; if neither is
      available, run ChartGenerator from Eclipse (Run As > Java Application, argument
      "results").

.EXAMPLE
    ./run_load_tests.ps1
    ./run_load_tests.ps1 -Port 5000 -PoolSizes 10,50 -Duration 20 -Clients 5,10,50,100,500
#>

[CmdletBinding()]
param(
    [int]    $Port      = 5000,
    [int[]]  $PoolSizes = @(10, 50),   # one thread-pool run (and chart series) per size
    [int]    $Duration  = 20,          # seconds of load per (strategy, clients) cell
    [int]    $RampUp    = 2,           # seconds to ramp all clients up
    [int[]]  $Clients   = @(5, 10, 50, 100, 500),
    [string] $JMeter    = 'jmeter',    # path to the JMeter launcher
    [string] $ResultsDir = ''          # defaults to <project>/results
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

$Classes    = Join-Path $ProjectRoot 'target/classes'
if (-not $ResultsDir) { $ResultsDir = Join-Path $ProjectRoot 'results' }
$Jmx        = Join-Path $ProjectRoot 'jmeter/echo_load_test.jmx'

# Ordered list of server runs. Each becomes one chart series, so keys must match
# what ChartGenerator recognises: SINGLE, THREADPERCONN, and POOL<size> (one run
# per requested thread-pool size, e.g. POOL10 and POOL50).
$Runs = New-Object System.Collections.Generic.List[object]
$Runs.Add([pscustomobject]@{ Key = 'SINGLE';        Class = 'edu.arizona.ece696.echo.SingleThreadEchoServer';        Args = @("$Port") })
$Runs.Add([pscustomobject]@{ Key = 'THREADPERCONN'; Class = 'edu.arizona.ece696.echo.ThreadPerConnectionEchoServer'; Args = @("$Port") })
foreach ($ps in $PoolSizes) {
    $Runs.Add([pscustomobject]@{ Key = ("POOL{0}" -f $ps); Class = 'edu.arizona.ece696.echo.ThreadPoolEchoServer'; Args = @("$Port", "$ps") })
}

function Test-Command($name) {
    return [bool](Get-Command $name -ErrorAction SilentlyContinue)
}

# --- Sanity checks --------------------------------------------------------------
if (-not (Test-Command 'java')) { throw 'java not found on PATH.' }
if (-not (Test-Command $JMeter)) {
    throw "JMeter launcher '$JMeter' not found on PATH. Install Apache JMeter and add its bin/ to PATH, or pass -JMeter <path>."
}

# --- Ensure the server classes are compiled ------------------------------------
if (-not (Test-Path (Join-Path $Classes 'edu/arizona/ece696/echo/EchoProtocol.class'))) {
    Write-Host 'Compiling server classes with javac...' -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path $Classes | Out-Null
    $srcDir = Join-Path $ProjectRoot 'src/main/java/edu/arizona/ece696/echo'
    $coreSources = @(
        'EchoProtocol.java',
        'SingleThreadEchoServer.java',
        'ThreadPerConnectionEchoServer.java',
        'ThreadPoolEchoServer.java',
        'EchoClient.java'
    ) | ForEach-Object { Join-Path $srcDir $_ }
    & javac -d $Classes @coreSources
    if ($LASTEXITCODE -ne 0) { throw 'javac failed.' }
}

# --- Fresh results directory ---------------------------------------------------
New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null
Get-ChildItem -Path $ResultsDir -Filter '*.jtl' -ErrorAction SilentlyContinue | Remove-Item -Force

function Wait-ForPort([int]$p, [int]$timeoutSec = 15) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $client = New-Object System.Net.Sockets.TcpClient
            $client.Connect('127.0.0.1', $p)
            $client.Close()
            return $true
        } catch {
            Start-Sleep -Milliseconds 200
        }
    }
    return $false
}

# Returns the PID listening on $p, or $null. We manage servers by port ownership
# rather than the PID from Start-Process, because the Oracle "javapath\java.exe"
# on PATH is a launcher stub: it exits immediately and the real JVM is re-parented,
# so its Start-Process PID cannot be used to stop the server.
function Get-PortOwnerPid([int]$p) {
    $conn = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) { return [int]$conn.OwningProcess }
    return $null
}

# The listening PID, but only if it is one of OUR echo servers (never a stranger).
function Get-EchoServerPid([int]$p) {
    $ownerPid = Get-PortOwnerPid $p
    if ($null -eq $ownerPid) { return $null }
    $proc = Get-CimInstance Win32_Process -Filter "ProcessId=$ownerPid" -ErrorAction SilentlyContinue
    if ($proc -and $proc.CommandLine -like '*edu.arizona.ece696.echo*') { return $ownerPid }
    return $null
}

# Force-kill a process and its children (handles the launcher-stub re-parenting).
function Stop-Tree([int]$procId) {
    if ($procId) { & taskkill /T /F /PID $procId 2>&1 | Out-Null }
}

# Wait until nothing is listening on $p (used between cells so the next server can bind).
function Wait-ForPortFree([int]$p, [int]$timeoutSec = 15) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        if ($null -eq (Get-PortOwnerPid $p)) { return $true }
        Start-Sleep -Milliseconds 200
    }
    return $false
}

# Starts a server and returns the PID of OUR echo server now listening on $Port.
function Start-Server([string]$class, [string[]]$serverArgs, [string]$logTag) {
    # Pre-flight: the port must be free, and if a stale echo server of ours is
    # holding it, reclaim it. Never touch a foreign process on the port.
    $existing = Get-PortOwnerPid $Port
    if ($existing) {
        if (Get-EchoServerPid $Port) {
            Write-Warning "Port $Port held by a stale echo server (PID $existing); stopping it."
            Stop-Tree $existing
            if (-not (Wait-ForPortFree $Port)) { throw "Could not free port $Port." }
        } else {
            throw "Port $Port is already in use by a non-echo process (PID $existing). Stop it or re-run with -Port <other>."
        }
    }

    # NOTE: Start-Process -ArgumentList joins the array with spaces and does NOT
    # quote items, so the classpath (which may contain spaces, e.g. this repo's
    # "Codeing Assignments" folder) must be wrapped in double quotes or java will
    # mis-parse it and report "Could not find or load main class".
    $allArgs = @('-cp', ('"' + $Classes + '"'), $class) + $serverArgs
    $srvErr = Join-Path $ResultsDir ("server_{0}.err.log" -f $logTag)
    Start-Process -FilePath 'java' -ArgumentList $allArgs -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $ResultsDir ("server_{0}.out.log" -f $logTag)) `
        -RedirectStandardError $srvErr | Out-Null

    if (-not (Wait-ForPort $Port)) {
        $err = (Get-Content $srvErr -ErrorAction SilentlyContinue) -join "`n"
        throw "Server $class did not start listening on port $Port.`nServer error output:`n$err"
    }
    $serverPid = Get-EchoServerPid $Port
    if ($null -eq $serverPid) {
        throw "Something is listening on port $Port but it is not our echo server; aborting to avoid load-testing the wrong process."
    }
    return $serverPid
}

# --- Main sweep ----------------------------------------------------------------
foreach ($run in $Runs) {
    $key        = $run.Key
    $class      = $run.Class
    $serverArgs = $run.Args

    foreach ($c in $Clients) {
        Write-Host ("=== {0}  clients={1} ===" -f $key, $c) -ForegroundColor Green
        $serverPid = Start-Server $class $serverArgs ("{0}_{1}" -f $key, $c)
        try {
            $jtl = Join-Path $ResultsDir ("{0}_{1}.jtl" -f $key, $c)
            $log = Join-Path $ResultsDir ("jmeter_{0}_{1}.log" -f $key, $c)
            # Build the args as an explicit, double-quoted array so PowerShell
            # expands $Port/$c/etc. Passing bare -Jport=$Port tokens can send the
            # literal text "$Port" to JMeter, which then reads 0 threads.
            $jmArgs = @(
                '-n', '-t', $Jmx,
                "-Jhost=localhost", "-Jport=$Port", "-Jclients=$c",
                "-Jduration=$Duration", "-Jrampup=$RampUp",
                '-l', $jtl, '-j', $log
            )
            # JMeter prints harmless JVM deprecation warnings to stderr. Under the
            # script's 'Stop' preference those can be promoted to terminating errors
            # if the caller captures output, so relax the preference just here and
            # judge success by the exit code instead.
            $prevEAP = $ErrorActionPreference
            $ErrorActionPreference = 'Continue'
            & $JMeter @jmArgs
            $jmExit = $LASTEXITCODE
            $ErrorActionPreference = $prevEAP
            if ($jmExit -ne 0) { Write-Warning "JMeter exited with code $jmExit for $key/$c" }
        }
        finally {
            # Stop our echo server (by its verified PID) and wait for the port to
            # be released before the next cell, so the next server can bind cleanly.
            Stop-Tree $serverPid
            Wait-ForPortFree $Port | Out-Null
        }
    }
}

# --- Generate the comparison charts --------------------------------------------
Write-Host 'Generating comparison charts...' -ForegroundColor Cyan
$chartMain = 'edu.arizona.ece696.echo.ChartGenerator'

if (Test-Command 'mvn') {
    & mvn -q compile exec:java "-Dexec.mainClass=$chartMain" "-Dexec.arguments=$ResultsDir"
}
else {
    # No Maven: try to run ChartGenerator with a JFreeChart jar from the local
    # Maven cache (populated once you build the project in Eclipse).
    $jfree = Get-ChildItem -Path (Join-Path $HOME '.m2/repository') -Recurse -Filter 'jfreechart-*.jar' -ErrorAction SilentlyContinue |
             Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1
    if ($jfree) {
        $cp = "$Classes;$($jfree.FullName)"
        & java -cp $cp $chartMain $ResultsDir
    }
    else {
        Write-Warning 'JFreeChart not found and Maven is unavailable, so charts were not generated automatically.'
        Write-Host   'The .jtl results are in the results/ folder. To create the graphs, either:' -ForegroundColor Yellow
        Write-Host   '  - build the project once in Eclipse, then run ChartGenerator (Run As > Java Application, argument "results"), or'
        Write-Host   '  - install Maven and run:  mvn compile exec:java -Dexec.mainClass=edu.arizona.ece696.echo.ChartGenerator -Dexec.arguments=results'
    }
}

Write-Host 'Done.' -ForegroundColor Green
