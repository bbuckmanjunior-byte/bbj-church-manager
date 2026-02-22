# Railway Deployment Guide

This application is now configured for deployment to Railway as a servlet-based WAR application.

## Build Configuration
- **Framework**: Servlet-based Java Web Application
- **Build Tool**: Maven 3.x
- **Packaging**: WAR (Web Archive)
- **Java Target**: Java 1.8+
- **WAR File**: `target/fresh_app-1.0.0.war`

## How Railway Deploys This Application

1. **Build Phase**:
   - Railway detects `pom.xml` and runs Maven
   - Command: `mvn clean package -DskipTests`
   - Output: `target/fresh_app-1.0.0.war`

2. **Startup Phase**:
   - Railway executes the Procfile: `web: bash bin/start.sh`
   - The `bin/start.sh` script:
     - Downloads and configures Apache Tomcat
     - Deploys the WAR file to Tomcat
     - Starts Tomcat on the PORT environment variable
     - Runs in the foreground so Railway can monitor it

## Configuration Steps for Railway

### 1. Environment Variables (Required)
Set these in Railway project settings:

```
# Database Connection
DB_HOST=your-railway-mysql-host
DB_PORT=3306
DB_NAME=your_database_name
DB_USER=your_db_user
DB_PASSWORD=your_db_password

# Application
JAVA_OPTS=-Xmx512m (adjust as needed)
PORT=8080 (Railway sets this automatically)
```

### 2. MySQL Database
- Create a MySQL service in Railway
- Create database schema using: `database/church_schema.sql`
- Run upgrades if needed: `database/upgrade_add_tokens.sql`

### 3. Application Configuration
Update your application's `DatabaseConnection.java` to use environment variables:
- `System.getenv("DB_HOST")`
- `System.getenv("DB_PORT")`
- `System.getenv("DB_NAME")`
- etc.

## Deployment Process

1. **Push to GitHub**:
   ```bash
   git add -A
   git commit -m "Your changes"
   git push
   ```

2. **Deploy on Railway**:
   - Connect your GitHub repository to Railway
   - Railway will automatically:
     - Build using Maven
     - Package as WAR
     - Deploy using the Procfile
     - Start the application

## Monitoring & Logs
- View logs in Railway dashboard
- Check for errors in application startup
- Verify database connectivity
- Test endpoints after deployment

## Troubleshooting

### Build Fails
- Check Maven logs for compilation errors
- Verify all dependencies are available
- Ensure web.xml is at `WebContent/WEB-INF/web.xml`

### Application Won't Start
- Check Tomcat startup logs
- Verify PORT environment variable is set
- Ensure WAR file was built successfully

### Database Connection Issues
- Verify DB environment variables are set correctly
- Test connection string format: `jdbc:mysql://host:port/database`
- Check MySQL is accessible from Railway environment

## Project Structure
```
fresh_app/
├── pom.xml                          # Maven configuration
├── Procfile                         # Railway startup configuration
├── bin/
│   └── start.sh                    # Startup script (runs Tomcat)
├── src/
│   └── com/bbj/               # Application source code
├── WebContent/
│   ├── WEB-INF/
│   │   └── web.xml            # Servlet configuration
│   └── ...jsp & html files    # Web assets
└── database/
    ├── church_schema.sql       # Database schema
    └── upgrade_*.sql           # Database upgrades
```

## Next Steps

1. Update `DatabaseConnection.java` to read environment variables for database config
2. Commit and push all changes to GitHub
3. Set environment variables in Railway dashboard
4. Connect GitHub repo to Railway project
5. Monitor the build and deployment in Railway logs
6. Test the application endpoints

---

**Note**: This configuration uses servlet-based architecture without Spring Boot for simplicity and compatibility. The application runs in Apache Tomcat, which is configured via the shipped Procfile and start script.
