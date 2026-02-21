# Cloudinary Setup for BBJ Digital Church Manager

This project now supports Cloudinary for hosting profile pictures.

## 1) Create a Cloudinary account
- Sign up at https://cloudinary.com/
- In the Cloudinary console, locate your **Cloud name**, **API Key**, and **API Secret** under Dashboard > Account Details.

## 2) Set environment variables (recommended)
Run in PowerShell (replace placeholders):

```powershell
[Environment]::SetEnvironmentVariable("CLOUDINARY_URL", "cloudinary://<API_KEY>:<API_SECRET>@<CLOUD_NAME>", "User")
# Or set components individually
[Environment]::SetEnvironmentVariable("CLOUDINARY_CLOUD_NAME", "<CLOUD_NAME>", "User")
[Environment]::SetEnvironmentVariable("CLOUDINARY_API_KEY", "<API_KEY>", "User")
[Environment]::SetEnvironmentVariable("CLOUDINARY_API_SECRET", "<API_SECRET>", "User")
```

After setting these, restart Tomcat so environment variables are picked up by the JVM.

## 3) Rebuild and deploy

```powershell
cd c:\Users\Buckman\Desktop\BBJDigitalChurchManager\fresh_app
mvn clean package -DskipTests
# Deploy the generated WAR to Tomcat webapps and restart Tomcat
```

## 4) Test
- Log into the app
- Go to Profile and upload an image
- Verify it appears and check Cloudinary Media Library for the uploaded image

## Notes
- Cloudinary free plan has limits (usage, transformations, storage). Good for hobby projects.
- Images are delivered via CDN and returned URLs are safe to use in `<img>` tags.
