@echo off
cd /d "c:\Users\Buckman\Desktop\BBJDigitalChurchManager\fresh_app"
echo Cleaning and building...
call mvn clean package -DskipTests -q
if exist "target\fresh_app-1.0.0.war" (
    echo Deploying WAR...
    copy /Y "target\fresh_app-1.0.0.war" "C:\apache-tomcat-9.0\webapps\fresh_app.war"
    echo Restarting Tomcat...
    call "C:\apache-tomcat-9.0\bin\shutdown.bat"
    timeout /t 2 /nobreak
    call "C:\apache-tomcat-9.0\bin\startup.bat"
    echo Done! Open http://localhost:8081/fresh_app/
) else (
    echo ERROR: WAR file not found
)
