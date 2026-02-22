#!/bin/bash
set -e

echo "===== Railway Tomcat Startup ====="

# Use Railway PORT or default to 8080
PORT=${PORT:-8080}
echo "Using PORT: $PORT"

# Check if WAR exists
if [ ! -f target/fresh_app-1.0.0.war ]; then
    echo "ERROR: WAR file not found!"
    echo "Make sure Maven build ran successfully."
    exit 1
fi

echo "WAR file found."

# Install Tomcat if not already installed
if [ ! -d tomcat ]; then
    echo "Installing Apache Tomcat..."
    mkdir tomcat
    cd tomcat
    wget -q https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.85/bin/apache-tomcat-9.0.85.tar.gz
    tar xzf apache-tomcat-9.0.85.tar.gz --strip-components=1
    rm apache-tomcat-9.0.85.tar.gz
    cd ..
    echo "Tomcat installed."
fi

# Clean old deployment
echo "Cleaning old deployment..."
rm -rf tomcat/webapps/ROOT
mkdir -p tomcat/webapps/ROOT

# Deploy WAR
echo "Deploying WAR..."
cd target
unzip -oq fresh_app-1.0.0.war -d ../tomcat/webapps/ROOT
cd ..

echo "WAR deployed."

# Configure Railway PORT in Tomcat
echo "Configuring Tomcat port..."
sed -i "s/port=\"8080\"/port=\"$PORT\"/g" tomcat/conf/server.xml

echo "Tomcat configured to run on port $PORT"

# DEBUG: Print all environment variables that might be used
echo "===== Environment Variables ====="
echo "MYSQL_HOST: ${MYSQL_HOST:-NOT SET}"
echo "MYSQL_PORT: ${MYSQL_PORT:-NOT SET}"
echo "MYSQL_DATABASE: ${MYSQL_DATABASE:-NOT SET}"
echo "MYSQL_USER: ${MYSQL_USER:-NOT SET}"
echo "MYSQL_PASSWORD: ${MYSQL_PASSWORD:+SET}"
echo "DB_HOST: ${DB_HOST:-NOT SET}"
echo "DB_PORT: ${DB_PORT:-NOT SET}"
echo "DB_NAME: ${DB_NAME:-NOT SET}"
echo "DB_USER: ${DB_USER:-NOT SET}"
echo "RAILWAY_PRIVATE_DOMAIN: ${RAILWAY_PRIVATE_DOMAIN:-NOT SET}"
echo "RAILWAY_STATIC_URL: ${RAILWAY_STATIC_URL:-NOT SET}"
echo "===== End Environment ====="

# Set memory settings (good for Railway free plan)
export JAVA_OPTS="-Xms128m -Xmx256m"

# Extra stability options
export CATALINA_OPTS="-Djava.net.preferIPv4Stack=true"

echo "Starting Tomcat..."

# Start Tomcat in foreground (required by Railway)
exec tomcat/bin/catalina.sh run
