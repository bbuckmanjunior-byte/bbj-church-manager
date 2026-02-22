#!/bin/bash
# Render Deployment Script for FreeSQLDatabase

echo "======================================"
echo "BBJ Church Manager - Render Deploy"
echo "======================================"

# Check if environment variables are set
if [ -z "$MYSQLHOST" ] || [ "$MYSQLHOST" = "your_host.freesqldatabase.com" ]; then
    echo "❌ ERROR: MYSQLHOST environment variable not set"
    echo "Please set the following environment variables in Render dashboard:"
    echo "  - MYSQLHOST"
    echo "  - MYSQLPORT"
    echo "  - MYSQLDATABASE"
    echo "  - MYSQLUSER"
    echo "  - MYSQLPASSWORD"
    exit 1
fi

echo "✅ Environment variables detected"
echo "📦 Building application..."

# Maven build (skipping tests for speed)
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "🚀 Starting Tomcat server..."
else
    echo "❌ Build failed"
    exit 1
fi
