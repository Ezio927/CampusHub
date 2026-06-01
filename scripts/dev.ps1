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

    $Deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $Deadline) {
        $Status = & docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $ContainerName 2>$null
        if ($LASTEXITCODE -eq 0 -and $Status.Trim() -in @('healthy', 'running')) {
            Write-Host "$Label is ready." -ForegroundColor Green
            return
        }

        Start-Sleep -Seconds 2
    }

    throw "$Label did not become ready within $TimeoutSeconds seconds."
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
    Push-Location $BackendDir
    try {
        & .\mvnw.cmd '-Dspring-boot.run.profiles=docker' 'spring-boot:run'
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

Write-Host 'Starting backend in a new PowerShell window...' -ForegroundColor Cyan
Start-NewWindow -WorkingDirectory $Root -Command '.\scripts\dev.ps1 -BackendOnly'

if ($SkipFrontendInstall) {
    $FrontendCommand = 'npm run dev'
}
else {
    $FrontendCommand = @'
if (-not (Test-Path -LiteralPath 'node_modules')) {
    npm install
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
npm run dev
'@
}

Write-Host 'Starting frontend in a new PowerShell window...' -ForegroundColor Cyan
Start-NewWindow -WorkingDirectory $FrontendDir -Command $FrontendCommand

Write-Host ''
Write-Host 'All services have been started.' -ForegroundColor Green
Write-Host 'Backend:  http://localhost:8080' -ForegroundColor Green
Write-Host 'Frontend: http://localhost:5173' -ForegroundColor Green
