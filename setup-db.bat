@echo off
setlocal enabledelayedexpansion

REM MySQL Setup Script (uses MySQL Server 9.6 on port 1532)
@echo off
setlocal enabledelayedexpansion

echo Running database schema update (port 1532)...
cd /d "C:\Program Files\MySQL\MySQL Server 9.6\bin"

REM Use root password configured for this environment
"mysql.exe" -u root -p"fire@1532" -P 1532 < "C:\Users\Buckman\Desktop\BBJDigitalChurchManager\fresh_app\database\church_schema.sql"

if errorlevel 1 (
    echo Error updating database
    pause
    exit /b 1
)

echo ✓ Database schema updated successfully!
echo.
echo Admin credentials:
echo   Email: admin@bbj.com
echo   Password: admin123
echo.
pause
