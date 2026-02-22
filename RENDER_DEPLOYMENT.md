# Render + TiDB Cloud Deployment Guide

## Prerequisites
- Render account: https://render.com
- TiDB Cloud account: https://tidbcloud.com
- GitHub repository with this project pushed

## Step 1: Set Up TiDB Cloud Database

1. Go to https://tidbcloud.com and sign up/log in
2. Create a new TiDB Serverless cluster (free tier available):
   - Click "Create Cluster"
   - Choose "Serverless"
   - Select your region
   - Note your connection details from the connection modal

3. Your connection details:
   - **Host**: `your_cluster_id.c.tidbcloud.com`
   - **Port**: `4000` (TiDB Cloud standard port)
   - **Database**: Create a new database (e.g., `church_manager`)
   - **Username**: Shown in connection string
   - **Password**: Set during cluster creation

4. Import your schema:
   - Use MySQL client: `mysql -h your_host -P 4000 -u your_user -p your_db < database/church_schema.sql`
   - Or use TiDB Cloud Web Console to run SQL directly
   - Run any migration scripts (e.g., `database/upgrade_add_tokens.sql`)

## Step 2: Deploy to Render

1. **Create a Web Service on Render:**
   - Go to https://dashboard.render.com
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Select the branch to deploy

2. **Configure the Service:**
   - **Name**: `bbj-church-manager`
   - **Environment**: `Docker`
   - **Plan**: Choose your plan (Standard for better performance, or Free)
   - **Dockerfile Path**: `./Dockerfile` (relative to repo root, under `fresh_app/`)

3. **Set Environment Variables:**
   - Go to "Environment" tab
   - Add the following variables (from your TiDB Cloud console):
     ```
     MYSQLHOST = your_cluster_id.c.tidbcloud.com
     MYSQLPORT = 4000
     MYSQLDATABASE = church_manager
     MYSQLUSER = your_username
     MYSQLPASSWORD = your_password
     CLOUDINARY_URL = cloudinary://Ui_G_Oy7fIXJYiG_uM7f564AFPo:768536761182458@dg3kvkfou
     ```

4. **Configure Build & Deployment:**
   - **Build Command**: (leave empty, Dockerfile handles it)
   - **Start Command**: (leave empty, Dockerfile handles it)
   - **Root Directory**: `fresh_app` (important!)

5. **Deploy:**
   - Click "Create Web Service"
   - Render will automatically build and deploy

## Step 3: Verify Deployment

1. Wait for the deployment to complete (usually 5-10 minutes)
2. Render will provide you with a URL: `https://bbj-church-manager-xxxxx.onrender.com`
3. Test the application:
   - Navigate to your URL
   - Try logging in
   - Check the logs for any errors: Logs tab in Render dashboard

## Important Notes

### TiDB Cloud Features
- **Serverless Tier**: Free tier with auto-scaling up to 500 RequestUnits/month
- **Compatibility**: MySQL 8.0 compatible, so no code changes needed
- **Performance**: Good performance for production workloads
- **Port 4000**: TiDB Cloud uses port 4000 instead of standard MySQL port 3306

### Render Specifics
- **Cold Starts**: Free tier instances spin down after 15 minutes of inactivity
- **Performance**: Standard plan is recommended for production apps
- **Root Directory**: Make sure to set the root directory to `fresh_app` where the Dockerfile is located
- **Build Time**: First deployment may take 5-10 minutes

### Connection Issues
If you see "Connection refused" errors:
1. Verify all TiDB Cloud credentials are correct (including port 4000)
2. Check that your TiDB Cloud cluster is active
3. Ensure your database schema is properly imported
4. Check Render logs for detailed error messages
5. Verify firewall rules allow connections from Render

## Troubleshooting

### Database Connection Failed
```
Check Render logs for: "❌ Database connection failed"
```
- Verify MYSQLHOST, MYSQLPORT (4000), MYSQLUSER, MYSQLPASSWORD are correct
- Check TiDB Cloud console to confirm cluster is running
- Verify the database name exists in TiDB Cloud
- Ensure you imported the schema correctly

### Build Failures
- Check the "Build" tab in Render dashboard
- Ensure Maven dependencies are downloading correctly
- Verify Java version compatibility (using Java 11 in Dockerfile)

### Application Not Starting
- Check "Logs" tab in Render for startup errors
- Verify all environment variables are set correctly
- Ensure the Dockerfile path is `fresh_app/Dockerfile`

## Updating Your Application

1. Make changes to your code locally
2. Push to GitHub: `git push origin main`
3. Render automatically rebuilds and redeploys
4. Monitor deployment in Render dashboard

## Database Backups

For production use, backup your TiDB Cloud data:
1. Use TiDB Cloud backup feature (available in console)
2. Or use MySQL command-line tools: `mysqldump -h your_host -P 4000 -u root -p your_db > backup.sql`
3. Store backups securely in cloud storage

## Security Recommendations

1. Never commit environment variables to GitHub
2. Use Render's environment variable feature for sensitive data
3. Use strong passwords for your TiDB Cloud account
4. Consider using TiDB Cloud firewall rules to restrict access
5. Regularly review and rotate Cloudinary API keys
6. Enable TiDB Cloud security features (HTTPS only, IP whitelist if available)

## Need Help?

- Render Documentation: https://render.com/docs
- TiDB Cloud Documentation: https://docs.tidbcloud.com
- TiDB Community: https://tidb.community
- Check application logs in Render dashboard
