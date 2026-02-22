# Migration from Railway to TiDB Cloud + Render

## What Changed?

| Component | Before (Railway) | Now (TiDB Cloud + Render) |
|-----------|-----------------|------------------------|
| **Hosting** | Railway | Render |
| **Database** | MySQL on Railway | TiDB Cloud (Serverless) |
| **Port** | 3306 | **4000** (important!) |
| **Configuration Files** | .railway.toml, railway.json | render.yaml |
| **Environment Variables** | Railway-specific | Standard MySQL format |
| **Cost** | Railway pricing | Render free/paid + TiDB free tier |

## For Your Java App

**Good news**: Your Java application requires **zero code changes**!

The existing `DatabaseConnection.java` already:
- ✅ Reads from environment variables
- ✅ Supports MySQL 8.0 compatible databases
- ✅ Works with TiDB Cloud (on port 4000)
- ✅ Works with Render

## Migration Checklist

- [ ] Delete Railway configuration files (.railway.toml, railway.json)
- [ ] Update `.env` with TiDB Cloud credentials
- [ ] Create TiDB Cloud account
- [ ] Create Serverless cluster on TiDB Cloud
- [ ] Import database schema to TiDB Cloud
- [ ] Test local connection
- [ ] Push to GitHub
- [ ] Create Render account
- [ ] Deploy to Render
- [ ] Set environment variables in Render dashboard
- [ ] Test production application

## Key Differences to Remember

### Database Port
```
Before:  MYSQLPORT=3306 (Railway)
Now:     MYSQLPORT=4000 (TiDB Cloud)
```

### Database Host Format
```
Before:  MYSQLHOST=bbj-church-manager.railway.internal
Now:     MYSQLHOST=abc123def.c.tidbcloud.com
```

### Deployment Platform
```
Before:  Railway (integrated database + hosting)
After:   TiDB Cloud (database) + Render (hosting)
```

## Files You Should Delete

From `fresh_app/`:
- `.railway.toml`
- `railway.json`
- `RAILWAY_DEPLOYMENT.md`

Use: `CLEANUP_RAILWAY.md` for deletion instructions

## Files You'll Use Now

- `TIDB_CLOUD_SETUP.md` - TiDB Cloud setup
- `RENDER_DEPLOYMENT.md` - Render deployment
- `.env` - Updated with TiDB Cloud format
- `render.yaml` - Render configuration
- `setup-render.bat` - Setup wizard for Windows

## Environment Variable Reference

Your `.env` should look like:

```dotenv
# TiDB Cloud (from their console)
MYSQLHOST=xxx.c.tidbcloud.com
MYSQLPORT=4000
MYSQLDATABASE=church_manager
MYSQLUSER=root
MYSQLPASSWORD=your_password

# Cloudinary (unchanged)
CLOUDINARY_URL=cloudinary://Ui_G_Oy7fIXJYiG_uM7f564AFPo:768536761182458@dg3kvkfou
```

## Backup Your Data

Before switching, backup Railway database:

```bash
# If you had a MySQL backup from Railway
# Store it safely before deleting Railway

# You can re-import to TiDB Cloud later if needed
```

## Testing the Migration

1. **Locally**: `mvn clean package -DskipTests` should build successfully
2. **Database**: Connect directly to TiDB Cloud with MySQL CLI
3. **Render**: Deploy and check logs for connection errors
4. **Application**: Test login and core features

## Support Resources

- **TiDB Cloud**: https://docs.tidbcloud.com
- **Render**: https://render.com/docs
- **Troubleshooting**: See individual setup guides

## Rollback Warning

Once you delete Railway configuration:
- You can't easily go back to Railway
- Keep git history: `git log` shows previous commits
- Your data is separate, so you can migrate back if needed

## Questions?

See the detailed guides:
- `TIDB_CLOUD_SETUP.md` - Database setup
- `RENDER_DEPLOYMENT.md` - Hosting setup
- Documentation links in those files
