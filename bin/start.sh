#!/bin/bash

# Get PORT from environment variable or default to 8080
PORT=${PORT:-8080}

# Build the application if not already built
if [ ! -f target/fresh_app-1.0.0.war ]; then
    echo "Building application..."
    mvn clean package -DskipTests
fi

# Use embedded Tomcat to run the WAR
# Download Tomcat if not present
if [ ! -d tomcat ]; then
    echo "Downloading Tomcat..."
    mkdir -p tomcat
    cd tomcat
    wget https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.5/bin/apache-tomcat-10.1.5.tar.gz
    tar xzf apache-tomcat-10.1.5.tar.gz
    mv apache-tomcat-10.1.5/* .
    rm apache-tomcat-10.1.5.tar.gz
    rm -rf apache-tomcat-10.1.5
    cd ..
fi

# Deploy WAR to Tomcat
echo "Deploying WAR to Tomcat..."
cp target/fresh_app-1.0.0.war tomcat/webapps/ROOT.war

# Set Tomcat port
export CATALINA_OPTS="-Dserver.port=$PORT"

# Start Tomcat
echo "Starting Tomcat on port $PORT..."
exec tomcat/bin/catalina.sh run
