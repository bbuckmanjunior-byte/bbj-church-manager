@echo off
REM Render Deployment Setup Script for Windows

echo ======================================
echo BBJ Church Manager - Render Setup
echo ======================================
echo.
echo This script prepares your application for Render deployment.
echo.

echo Step 1: Update .env file with your TiDB Cloud credentials
echo ==========================================================
echo.
echo Go to https://tidbcloud.com
echo Create or access your TiDB Serverless cluster and note:
echo   - Host (e.g., xxx.c.tidbcloud.com)
echo   - Port (4000 for TiDB Cloud)
echo   - Database name
echo   - Username
echo   - Password
echo.
pause

echo.
echo Step 2: Create GitHub Repository
echo ================================
echo 1. Push this code to GitHub:
echo    git init
echo    git add .
echo    git commit -m "Initial commit"
echo    git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
echo    git branch -M main
echo    git push -u origin main
echo.
pause

echo.
echo Step 3: Deploy to Render
echo ======================
echo 1. Go to https://dashboard.render.com
echo 2. Click "New +" ^> "Web Service"
echo 3. Connect your GitHub repository
echo 4. Configure:
echo    - Name: bbj-church-manager
echo    - Environment: Docker
echo    - Dockerfile Path: fresh_app/Dockerfile
echo    - Root Directory: fresh_app
echo 5. Add Environment Variables (from TiDB Cloud console):
echo    - MYSQLHOST (your_cluster_id.c.tidbcloud.com)
echo    - MYSQLPORT (4000)
echo    - MYSQLDATABASE
echo    - MYSQLUSER
echo    - MYSQLPASSWORD
echo    - CLOUDINARY_URL (already set)
echo.
pause

echo.
echo Step 4: Import Database Schema
echo ===============================
echo 1. Access your TiDB Cloud console
echo 2. Run the SQL from database/church_schema.sql using Web Console
echo 3. Or use MySQL CLI:
echo    mysql -h your_host -P 4000 -u your_user -p your_db ^< database\church_schema.sql
echo 4. Run any upgrade scripts (upgrade_add_tokens.sql)
echo.
pause

echo Setup complete! Your app should now be deploying on Render.
echo Check https://dashboard.render.com for deployment status.
echo.
pause
