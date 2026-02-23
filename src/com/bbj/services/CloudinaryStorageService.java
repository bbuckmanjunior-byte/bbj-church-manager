package com.bbj.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

public class CloudinaryStorageService {
    private static Cloudinary cloudinary = null;

    private static synchronized Cloudinary getCloudinary() {
        if (cloudinary == null) {
            String cloudinaryUrl = System.getenv("CLOUDINARY_URL");
            if (StringUtils.isBlank(cloudinaryUrl)) {
                // Allow user to set components individually (optional)
                String cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
                String apiKey = System.getenv("CLOUDINARY_API_KEY");
                String apiSecret = System.getenv("CLOUDINARY_API_SECRET");
                if (!StringUtils.isBlank(cloudName) && !StringUtils.isBlank(apiKey) && !StringUtils.isBlank(apiSecret)) {
                    cloudinary = new Cloudinary(ObjectUtils.asMap(
                            "cloud_name", cloudName,
                            "api_key", apiKey,
                            "api_secret", apiSecret
                    ));
                } else {
                    cloudinary = new Cloudinary();
                }
            } else {
                cloudinary = new Cloudinary(cloudinaryUrl);
            }
        }
        return cloudinary;
    }

    public static String uploadProfilePicture(InputStream input, String originalFileName, int userId) throws IOException {
        Cloudinary c = getCloudinary();

        byte[] bytes = input.readAllBytes();
        if (bytes.length > 10 * 1024 * 1024) {
            throw new IOException("File size exceeds 10MB limit");
        }

        String publicId = "profile-pictures/" + userId + "_" + UUID.randomUUID();

        Map<?,?> result = c.uploader().upload(bytes, ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "image",
                "overwrite", true
        ));

        Object secureUrl = result.get("secure_url");
        if (secureUrl != null) return secureUrl.toString();
        Object url = result.get("url");
        if (url != null) return url.toString();
        throw new IOException("Cloudinary upload returned no URL");
    }
}
