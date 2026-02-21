# BBJ Digital Church Manager - Setup and Run Script

Write-Host "=== BBJ Digital Church Manager Setup ===" -ForegroundColor Green

# Configuration
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 9.6\bin\mysql.exe"
$mysqlDumpPath = "C:\Program Files\MySQL\MySQL Server 9.6\bin\mysqld.exe"
$tomcatPath = "C:\apache-tomcat-9.0"
$projectPath = Split-Path -Parent $PSScriptRoot
$dbScript = "$projectPath\fresh_app\database\church_schema.sql"

# Step 1: Start MySQL Server
Write-Host "`n1. Starting MySQL Server..." -ForegroundColor Cyan
if (Get-Process mysqld -ErrorAction SilentlyContinue) {
    Write-Host "MySQL is already running." -ForegroundColor Yellow
} else {
    Write-Host "Starting MySQL daemon..."
    $mysqldProc = Start-Process $mysqlDumpPath -WindowStyle Hidden -PassThru
    Start-Sleep -Seconds 3
    Write-Host "MySQL started (PID: $($mysqldProc.Id))" -ForegroundColor Green
}

# Step 2: Wait for MySQL to be ready
Write-Host "`n2. Waiting for MySQL to be ready..." -ForegroundColor Cyan
$attempts = 0
$ready = $false
while ($attempts -lt 10 -and -not $ready) {
    try {
        & $mysqlPath -u root -pfire@1532 -P 1532 -e "SELECT 1" 2>$null | Out-Null
        $ready = $true
        Write-Host "MySQL is ready!" -ForegroundColor Green
    } catch {
        $attempts++
        if ($attempts -lt 10) {
            Write-Host "Waiting for MySQL... ($attempts/10)" -ForegroundColor Yellow
            Start-Sleep -Seconds 1
        }
    }
}

if (-not $ready) {
    Write-Host "ERROR: MySQL did not start within 10 seconds" -ForegroundColor Red
    Write-Host "Please ensure MySQL is properly installed and no other MySQL instances are running"
    exit 1
}

# Step 3: Setup Database
Write-Host "`n3. Setting up database..." -ForegroundColor Cyan
$sqlContent = Get-Content $dbScript -Raw
& $mysqlPath -u root -pfire@1532 -P 1532 -e "$sqlContent" 2>&1 | Where-Object {$_ -notmatch "Warning|Using a password"}
Write-Host "Database setup completed!" -ForegroundColor Green

# Step 4: Stop Tomcat
Write-Host "`n4. Stopping Tomcat..." -ForegroundColor Cyan
$tomcatProcesses = Get-Process java -ErrorAction SilentlyContinue
if ($tomcatProcesses) {
    Write-Host "Stopping Tomcat processes..." -ForegroundColor Yellow
    Stop-Process -Name java -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "Tomcat stopped." -ForegroundColor Green
} else {
    Write-Host "Tomcat is not running." -ForegroundColor Yellow
}

# Step 5: Build the project with Maven
Write-Host "`n5. Building project with Maven..." -ForegroundColor Cyan
$mavenHome = "C:\apache-maven-3.9.12"
$mavenExe = "$mavenHome\bin\mvn.cmd"

if (-not (Test-Path $mavenExe)) {
    Write-Host "ERROR: Maven not found at $mavenExe" -ForegroundColor Red
    Write-Host "Please ensure Maven is installed." -ForegroundColor Red
    exit 1
}

$env:MAVEN_HOME = $mavenHome
Push-Location $projectPath\fresh_app
& $mavenExe clean package -q 2>&1 | Out-Null
Pop-Location

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful!" -ForegroundColor Green
    $warFile = "$projectPath\fresh_app\target\fresh_app-1.0.0.war"
    if (Test-Path $warFile) {
        $warSize = [math]::Round((Get-Item $warFile).Length / 1MB, 2)
        Write-Host "WAR file created: fresh_app-1.0.0.war ($warSize MB)" -ForegroundColor Green
    }
} else {
    Write-Host "ERROR: Build failed. Please check Maven installation." -ForegroundColor Red
    exit 1
}

# Step 6: Deploy to Tomcat
Write-Host "`n6. Deploying to Tomcat..." -ForegroundColor Cyan
$tomcatWebapps = "$tomcatPath\webapps"
if (-not (Test-Path $tomcatWebapps)) {
    Write-Host "ERROR: Tomcat not found at $tomcatPath" -ForegroundColor Red
    exit 1
}

$warFile = "$projectPath\fresh_app\target\fresh_app-1.0.0.war"
$deployTarget = "$tomcatWebapps\fresh_app.war"

if (Test-Path $warFile) {
    Copy-Item $warFile $deployTarget -Force
    Write-Host "Application deployed to: $deployTarget" -ForegroundColor Green
} else {
    Write-Host "ERROR: WAR file not found at $warFile" -ForegroundColor Red
    exit 1
}

# Step 7: Start Tomcat
Write-Host "`n7. Starting Tomcat..." -ForegroundColor Cyan
$startupScript = "$tomcatPath\bin\startup.bat"
if (Test-Path $startupScript) {
    Start-Process $startupScript -WindowStyle Hidden
    Start-Sleep -Seconds 3
    Write-Host "Tomcat started successfully." -ForegroundColor Green
} else {
    Write-Host "ERROR: Tomcat startup script not found at $startupScript" -ForegroundColor Red
    exit 1
}

# Step 8: Summary
Write-Host "`n8. Setup Complete!" -ForegroundColor Green
Write-Host "Deployment Summary:" -ForegroundColor Cyan
Write-Host "  - MySQL: Running on port 1532" -ForegroundColor Yellow
Write-Host "  - Tomcat: Running (started at $(Get-Date -Format 'HH:mm:ss'))" -ForegroundColor Yellow
Write-Host "  - Application: http://localhost:8081/fresh_app" -ForegroundColor Yellow
Write-Host "  - Admin Dashboard: http://localhost:8081/fresh_app/admin-dashboard.html" -ForegroundColor Yellow
Write-Host "`n=== Setup Complete ===" -ForegroundColor Green
