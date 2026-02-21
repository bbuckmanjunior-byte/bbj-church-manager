@echo off
REM BBJ Digital Church Manager - Setup and Deployment Script
REM This script sets up the project, creates a WAR file, and deploys to Tomcat

setlocal enabledelayedexpansion

echo ===== BBJ Digital Church Manager - Setup and Deploy =====
echo.

REM Configuration
set TOMCAT_PATH=C:\Program Files\Apache Software Foundation\Tomcat 9.0_Buckman
set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 9.6\bin\mysql.exe
set PROJECT_DIR=%CD%
set WEB_INF_LIB=%PROJECT_DIR%\WebContent\WEB-INF\lib
set SERVLET_JAR=%WEB_INF_LIB%\servlet-api-2.5.jar
set MYSQL_JAR=%WEB_INF_LIB%\mysql-connector-java-8.0.33.jar

echo 1. Checking dependencies...
if not exist "%WEB_INF_LIB%" (
    echo Creating lib directory...
    mkdir "%WEB_INF_LIB%"
)

REM Check if servlet JAR exists, if not, we'll download it
if not exist "%SERVLET_JAR%" (
    echo Downloading servlet API JAR...
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/javax/servlet/servlet-api/2.5/servlet-api-2.5.jar', '%SERVLET_JAR%')}" 2>nul
    if errorlevel 1 (
        echo Warning: Could not download servlet JAR. Please add it manually to %WEB_INF_LIB%
    ) else (
        echo servlet-api-2.5.jar downloaded successfully.
    )
)

if not exist "%MYSQL_JAR%" (
    echo Downloading MySQL connector JAR...
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar', '%MYSQL_JAR%')}" 2>nul
    if errorlevel 1 (
        echo Warning: Could not download MySQL JAR. Please add it manually to %WEB_INF_LIB%
    ) else (
        echo mysql-connector-java-8.0.33.jar downloaded successfully.
    )
)

echo.
echo 2. Compiling Java source code...
set CLASSPATH=%SERVLET_JAR%;%MYSQL_JAR%;%TOMCAT_PATH%\lib\*

javac -cp "%CLASSPATH%" -d bin ^
    src\com\bbj\db\DatabaseConnection.java ^
    src\com\bbj\models\User.java ^
    src\com\bbj\servlets\LoginServlet.java ^
    src\com\bbj\servlets\LogoutServlet.java ^
    src\com\bbj\servlets\AnnouncementServlet.java ^
    src\com\bbj\servlets\EventServlet.java ^
    src\com\bbj\servlets\MemberServlet.java ^
    src\com\bbj\servlets\SermonServlet.java

if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)
echo Compilation successful!

echo.
echo 3. Creating WAR file...
jar cf BBJChurchManager.war -C WebContent . -C bin .
if errorlevel 1 (
    echo ERROR: WAR creation failed!
    pause
    exit /b 1
)
echo WAR file created: BBJChurchManager.war

echo.
echo 4. Deploying to Tomcat...
if not exist "%TOMCAT_PATH%\webapps" (
    echo ERROR: Tomcat not found at %TOMCAT_PATH%
    pause
    exit /b 1
)

copy BBJChurchManager.war "%TOMCAT_PATH%\webapps\fresh_app.war" >nul
echo Application deployed to Tomcat!

echo.
echo ===== Setup Complete! =====
echo.
echo Next steps:
echo 1. Start Tomcat: %TOMCAT_PATH%\bin\catalina.bat run
echo 2. Access the app at: http://localhost:8080/fresh_app
echo 3. Login with: admin / admin123
echo.
pause
