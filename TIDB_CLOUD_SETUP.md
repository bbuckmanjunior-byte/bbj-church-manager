# TiDB Cloud Setup Guide

## What is TiDB Cloud?

TiDB Cloud is a fully managed MySQL-compatible database service. Key features:
- **Compatibility**: 100% MySQL 8.0 compatible (your app needs zero changes!)
- **Serverless**: Pay only for what you use
- **Free Tier**: 500 Request Units/month free, perfect for testing
- **High Availability**: Automatic replication and backups
- **Port**: 4000 (different from standard MySQL port 3306)

## Step 1: Create a TiDB Cloud Account

1. Go to https://tidbcloud.com
2. Click "Start Free"
3. Sign up with email or GitHub
4. Verify your email
5. Set up your organization and initial cluster

## Step 2: Create a Serverless Cluster

1. In TiDB Cloud Console, click "Create Cluster"
2. Choose **Serverless** tier (free tier available)
3. Select your region:
   - Choose closest to your users
   - For US-based users: `us-west-2` or `us-east-1`
   - For EU: `eu-west-1`
4. Click "Create"
5. Wait 1-2 minutes for cluster creation

## Step 3: Get Connection Details

1. After cluster is created, click "Connect"
2. You'll see a connection modal with:
   - **Host**: `xxxxx.c.tidbcloud.com`
   - **Port**: `4000`
   - **Username**: Usually `root`
   - **Password**: You'll set this or it's auto-generated

### Example Connection Details:
```
Host: abc123def.c.tidbcloud.com
Port: 4000
Database: church_manager (you'll create this)
Username: root
Password: your_secure_password
```

## Step 4: Create Database & Import Schema

### Using MySQL CLI (Recommended)

```bash
# Connect to TiDB Cloud
mysql -h abc123def.c.tidbcloud.com -P 4000 -u root -p

# In the MySQL prompt:
# Create database
CREATE DATABASE church_manager;
USE church_manager;

# Exit MySQL
exit

# Import schema from file
mysql -h abc123def.c.tidbcloud.com -P 4000 -u root -p church_manager < database/church_schema.sql

# Import any upgrades
mysql -h abc123def.c.tidbcloud.com -P 4000 -u root -p church_manager < database/upgrade_add_tokens.sql
```

### Using TiDB Cloud Web Console

1. Go to your cluster's detail page
2. Click "SQL Editor" or "Web Console"
3. Copy-paste the contents of `database/church_schema.sql`
4. Run the SQL
5. Repeat for `database/upgrade_add_tokens.sql`

## Step 5: Update Environment Variables

Edit `.env` with your actual values:

```dotenv
MYSQLHOST=abc123def.c.tidbcloud.com
MYSQLPORT=4000
MYSQLDATABASE=church_manager
MYSQLUSER=root
MYSQLPASSWORD=your_secure_password
CLOUDINARY_URL=cloudinary://Ui_G_Oy7fIXJYiG_uM7f564AFPo:768536761182458@dg3kvkfou
```

### For Render Deployment
Set the same variables in Render's Environment tab:
- MYSQLHOST
- MYSQLPORT
- MYSQLDATABASE
- MYSQLUSER
- MYSQLPASSWORD
- CLOUDINARY_URL

## Step 6: Test Connection Locally

```bash
cd fresh_app
mvn clean package -DskipTests
# The build should succeed if .env is correct
```

## Important Notes

### TiDB Cloud Specifics
- **Port 4000**: Not 3306! This is TiDB's standard port
- **Connection Limit**: Free tier has connection limits
- **Request Units**: Serverless charges by request units, not CPU time
- **Backups**: Automatic daily backups included

### Connection Troubleshooting
If you get connection errors:

1. **"Connection refused"**
   - Check port is 4000 (not 3306)
   - Verify host is correct: `xxx.c.tidbcloud.com`
   - Check cluster is active in TiDB Cloud console

2. **"Access denied for user"**
   - Verify username and password in connection string
   - Check case sensitivity of username
   - Ensure password doesn't have special characters that need escaping

3. **"Unknown database"**
   - Verify database name matches what you created
   - Check you imported schema correctly
   - Try connecting directly via CLI to verify

### TLS / CA certificate
- If you see certificate validation errors when using `sslMode=VERIFY_IDENTITY`, your JVM may not trust the Let's Encrypt root that issued the TiDB Cloud certificate.
- You can download the ISRG Root X1 certificate from https://letsencrypt.org/certs/isrgrootx1.pem and import it into a Java truststore.

Import example (create a new truststore):
```bash
keytool -importcert -alias isrgrootx1 -file isrgrootx1.pem \
   -keystore /path/to/truststore.jks -storepass changeit -noprompt

# Start Java with the custom truststore:
export JAVA_OPTS="-Djavax.net.ssl.trustStore=/path/to/truststore.jks -Djavax.net.ssl.trustStorePassword=changeit"
```

Note: The `database/isrgrootx1.pem` file has been removed from the repository to avoid tracking CA files. Download the up-to-date PEM from Let's Encrypt when needed.

### Performance Optimization
- TiDB automatically optimizes queries
- For large datasets, consider upgrading from Serverless to Dedicated
- Free tier has ~1.6GB storage limit

## Free Tier Limits

- **Storage**: 1.6 GB
- **Monthly RUs**: 500 Request Units (resets monthly)
- **Connections**: Up to 1000 concurrent connections
- **Annual Free**: $110 credit if signed up with valid payment method

## Upgrading Later

If you outgrow free tier:
1. TiDB Cloud → Cluster Settings
2. Choose "Dedicated" tier
3. Select desired resources
4. Connection string remains the same!

## Useful Links

- TiDB Cloud Console: https://tidbcloud.com/console
- Documentation: https://docs.tidbcloud.com
- Community: https://tidb.community
- Billing Calculator: https://tidbcloud.com/pricing

## Next Steps

1. ✅ Create TiDB Cloud cluster
2. ✅ Import your database schema
3. ✅ Update `.env` file
4. ✅ Deploy to Render (see RENDER_DEPLOYMENT.md)
5. ✅ Test your application!
