#!/bin/bash
set -e

echo "Installing Tomcat 8.5..."
cd /tmp
curl -s -O https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.85/bin/apache-tomcat-8.5.85.tar.gz
tar xzf apache-tomcat-8.5.85.tar.gz
mv apache-tomcat-8.5.85 /opt/tomcat
rm apache-tomcat-8.5.85.tar.gz

# Copy WAR file to webapps with ROOT name
echo "Deploying WAR file..."
mkdir -p /opt/tomcat/webapps
cp target/fresh_app-1.0.0.war /opt/tomcat/webapps/ROOT.war

# Ensure webapps/ROOT doesn't exist (Tomcat needs this for ROOT.war)
rm -rf /opt/tomcat/webapps/ROOT

# Set Tomcat port to Railway's PORT environment variable (defaults to 8080)
export CATALINA_OPTS="${CATALINA_OPTS:-} -Dserver.port=${PORT:-8080}"

echo "Starting Tomcat on port ${PORT:-8080}..."
exec /opt/tomcat/bin/catalina.sh run

