#!/bin/bash
set -e

# Get PORT from environment variable or default to 8080
PORT=${PORT:-8080}

# Verify WAR was built
if [ ! -f target/fresh_app-1.0.0.war ]; then
    echo "ERROR: WAR file not found. Build may have failed."
    exit 1
fi

# Download and setup Tomcat if not present
if [ ! -d tomcat ]; then
    echo "Setting up Apache Tomcat..."
    mkdir -p tomcat
    cd tomcat
    wget -q https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.85/bin/apache-tomcat-9.0.85.tar.gz
    tar xzf apache-tomcat-9.0.85.tar.gz --strip-components=1
    rm apache-tomcat-9.0.85.tar.gz
    rm -rf webapps/ROOT
    cd ..
    echo "Tomcat setup complete"
fi

# Deploy WAR to Tomcat
echo "Deploying application..."
mkdir -p tomcat/webapps/ROOT
cd target
unzip -q fresh_app-1.0.0.war -d ../tomcat/webapps/ROOT
cd ..

# Set Tomcat port and start
echo "Starting application on port $PORT..."
export CATALINA_OPTS="-Djava.net.preferIPv4Stack=true"
export JPDA_ADDRESS=$PORT
export SERVER_PORT=$PORT

# Start Tomcat in foreground
exec tomcat/bin/catalina.sh run
