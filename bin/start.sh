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

# Export DATABASE environment variables so Java can access them
export MYSQL_HOST="${MYSQL_HOST:-localhost}"
export MYSQL_PORT="${MYSQL_PORT:-3306}"
export MYSQL_DATABASE="${MYSQL_DATABASE:-railway}"
export MYSQL_USER="${MYSQL_USER:-root}"
export MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"

echo "Database configuration:"
echo "MYSQL_HOST=$MYSQL_HOST"
echo "MYSQL_PORT=$MYSQL_PORT"
echo "MYSQL_DATABASE=$MYSQL_DATABASE"
echo "MYSQL_USER=$MYSQL_USER"

# Set memory settings (good for Railway free plan)
export JAVA_OPTS="-Xms128m -Xmx256m"

# Extra stability options
export CATALINA_OPTS="-Djava.net.preferIPv4Stack=true"

echo "Starting Tomcat..."

# Start Tomcat in foreground (required by Railway)
exec tomcat/bin/catalina.sh run
