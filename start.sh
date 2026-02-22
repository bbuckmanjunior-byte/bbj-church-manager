#!/bin/bash

# Download Tomcat 8.5 if not already installed
if [ ! -d "/opt/tomcat" ]; then
  echo "Installing Tomcat..."
  cd /opt
  curl -O https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.85/bin/apache-tomcat-8.5.85.tar.gz
  tar xzf apache-tomcat-8.5.85.tar.gz
  mv apache-tomcat-8.5.85 tomcat
  rm apache-tomcat-8.5.85.tar.gz
fi

# Copy WAR to Tomcat
cp target/fresh_app-1.0.0.war /opt/tomcat/webapps/ROOT.war

# Set PORT environment variable (Railway provides PORT)
export CATALINA_OPTS="$CATALINA_OPTS -Dserver.port=${PORT:-8080}"

# Start Tomcat
/opt/tomcat/bin/catalina.sh run
