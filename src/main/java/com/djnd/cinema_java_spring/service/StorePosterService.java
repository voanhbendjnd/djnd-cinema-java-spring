package com.djnd.cinema_java_spring.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StorePosterService {
    final Cloudinary cloudinary;
    @Data
    public static class UploadResult{
        private String secureUrl;
        private String publicId;
    }
    private boolean allowedTypeFile(String file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        var lastFileOrigin = file.toLowerCase();
        if (lastFileOrigin == null)
            return false;
        var allowed = List.of(".jpg", "jpeg", "png", ".mp3", ".m4a", ".flac", ".wav");
        return allowed.stream().anyMatch(lastFileOrigin::endsWith);
    }
    public UploadResult uploadToCloudinary(MultipartFile file, String folder) throws IOException {
        String originalName = file.getOriginalFilename();
        if (!this.allowedTypeFile(originalName)) {
            throw new IOException("File Invalid");
        }
        if (originalName == null)
            originalName = "filename";

        // String cleanName = StringUtils.cleanPath(originalName);
        String publicId = "t-" + System.currentTimeMillis();

        Map params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "auto");

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        UploadResult result = new UploadResult();
        result.setSecureUrl(uploadResult.get("secure_url").toString());
        result.setPublicId(publicId);

        return result;
    }
    public String moveCloudinaryFile(String currentPublicId, String targetFolder) throws Exception {
        String fileName = currentPublicId.substring(currentPublicId.lastIndexOf("/") + 1);
        String newPublicId = targetFolder + "/" + fileName;
        if(currentPublicId.equals(newPublicId)){
            var resource = cloudinary.api().resource(newPublicId, ObjectUtils.asMap("resource_type", "video"));
            return resource.get("secure_url").toString();
        }
        cloudinary.uploader().rename(currentPublicId, newPublicId ,ObjectUtils.asMap("resource_type", "video"));
        var resource = cloudinary.api().resource(newPublicId, ObjectUtils.asMap("resource_type", "video"));
        return resource.get("secure_url").toString();
    }


}
