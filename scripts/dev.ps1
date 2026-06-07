param(
    [switch]$BackendOnly,
    [switch]$SkipFrontendInstall
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$BackendDir = Join-Path $Root 'backend'
$FrontendDir = Join-Path $Root 'frontend'

function Assert-Command {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [string]$Hint
    )

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "$Name is not available. $Hint"
    }
}

function Wait-ForContainerHealthy {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ContainerName,
        [Parameter(Mandatory = $true)]
        [string]$Label,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        $status = & docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $ContainerName 2>$null
        if ($LASTEXITCODE -eq 0 -and $status.Trim() -in @('healthy', 'running')) {
            Write-Host "$Label is ready." -ForegroundColor Green
            return
        }
        Start-Sleep -Seconds 2
    }

    throw "$Label did not become ready within $TimeoutSeconds seconds."
}

function Get-PortListenerProcessIds {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    $ids = New-Object 'System.Collections.Generic.HashSet[int]'
    Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | ForEach-Object {
        if ($_.OwningProcess -gt 0) {
            [void]$ids.Add($_.OwningProcess)
        }
    }
    return @($ids)
}

function Stop-PortListener {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [Parameter(Mandatory = $true)]
        [string]$Label
    )

    $processIds = @(Get-PortListenerProcessIds -Port $Port)
    if ($processIds.Count -eq 0) {
        return
    }

    $names = $processIds | ForEach-Object {
        (Get-Process -Id $_ -ErrorAction SilentlyContinue).ProcessName
    } | Where-Object { $_ } | Select-Object -Unique

    Write-Host "Releasing port ${Port} used by ${Label} (PID: $($processIds -join ', '); process: $($names -join '/'))..." -ForegroundColor Yellow
    foreach ($procId in $processIds) {
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
    }
    Start-Sleep -Seconds 1

    if ((Get-PortListenerProcessIds -Port $Port).Count -gt 0) {
        throw "Port ${Port} is still in use, so ${Label} cannot be started. Please stop the process manually and try again."
    }
}

function Start-NewWindow {
    param(
        [Parameter(Mandatory = $true)]
        [string]$WorkingDirectory,
        [Parameter(Mandatory = $true)]
        [string]$Command
    )

    Start-Process -FilePath 'powershell.exe' -WorkingDirectory $WorkingDirectory -ArgumentList @(
        '-NoExit',
        '-Command',
        $Command
    ) | Out-Null
}

function Start-Backend {
    Stop-PortListener -Port 8080 -Label 'backend'
    Push-Location $BackendDir
    try {
        if (Test-Path -LiteralPath (Join-Path $BackendDir 'mvnw.cmd')) {
            & .\mvnw.cmd '-Dspring-boot.run.profiles=docker' 'spring-boot:run'
            if ($LASTEXITCODE -eq 0) {
                return
            }
            Write-Host 'Maven wrapper failed, falling back to system Maven...' -ForegroundColor Yellow
        }

        Assert-Command -Name 'mvn' -Hint 'Install Maven or fix backend/mvnw.cmd.'
        & mvn '-Dspring-boot.run.profiles=docker' 'spring-boot:run'
    }
    finally {
        Pop-Location
    }
}

if ($BackendOnly) {
    if (-not (Test-Path -LiteralPath (Join-Path $BackendDir 'mvnw.cmd'))) {
        throw "backend/mvnw.cmd was not found."
    }

    Start-Backend
    return
}

Assert-Command -Name 'docker' -Hint 'Install Docker Desktop and make sure it is running.'
Assert-Command -Name 'npm' -Hint 'Install Node.js 20+ and make sure npm is on PATH.'

if (-not (Test-Path -LiteralPath (Join-Path $BackendDir 'mvnw.cmd'))) {
    throw "backend/mvnw.cmd was not found."
}

Write-Host 'Starting database and Redis with Docker Compose...' -ForegroundColor Cyan
Push-Location $Root
try {
    & docker compose up -d
    if ($LASTEXITCODE -ne 0) {
        throw 'docker compose up -d failed.'
    }
}
finally {
    Pop-Location
}

Wait-ForContainerHealthy -ContainerName 'campushub-mysql' -Label 'MySQL'
Wait-ForContainerHealthy -ContainerName 'campushub-redis' -Label 'Redis'

Stop-PortListener -Port 8080 -Label 'backend'
Write-Host 'Starting backend in a new PowerShell window...' -ForegroundColor Cyan
Start-NewWindow -WorkingDirectory $Root -Command '.\scripts\dev.ps1 -BackendOnly'

if ($SkipFrontendInstall) {
    $frontendCommand = 'npm run dev'
}
else {
    $frontendCommand = @'
if (-not (Test-Path -LiteralPath "node_modules")) {
    npm install
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
npm run dev
'@
}

Stop-PortListener -Port 5173 -Label 'frontend'
Write-Host 'Starting frontend in a new PowerShell window...' -ForegroundColor Cyan
Start-NewWindow -WorkingDirectory $FrontendDir -Command $frontendCommand

Write-Host ''
Write-Host 'All services have been started.' -ForegroundColor Green
Write-Host 'Backend:  http://localhost:8080' -ForegroundColor Green
Write-Host 'Frontend: http://localhost:5173' -ForegroundColor Green
