package com.workforcex.backend.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Dev-only local disk storage.
 *
 * NOTE: this is intentionally simple and is NOT suitable for production on
 * Railway (or any platform with an ephemeral/non-persistent filesystem) -
 * files written here disappear on every redeploy/restart/scale event. Use
 * SupabaseStorageService (or another object-storage-backed implementation)
 * for the "prod" profile.
 */
@Service
@Profile("!prod")
public class LocalFileStorageService implements FileStorageService {

    private final Path baseDir;

    public LocalFileStorageService(@Value("${app.storage.local.base-dir:./data/uploads}") String baseDirProperty) {
        this.baseDir = Paths.get(baseDirProperty).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, UUID userId) throws IOException {
        String safeOriginalName = Path.of(file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "file").getFileName().toString();
        String storageKey = userId + "/" + UUID.randomUUID() + "_" + safeOriginalName;

        Path target = baseDir.resolve(storageKey).normalize();
        if (!target.startsWith(baseDir)) {
            // Guards against path traversal via a crafted filename.
            throw new IllegalArgumentException("Invalid file name");
        }

        Files.createDirectories(target.getParent());
        file.transferTo(target);
        return storageKey;
    }

    @Override
    public String resolveUrl(UUID documentId, String storageKey) {
        // Streamed by VerificationController#getDocumentFile, which checks
        // the requester is the owning worker or an employer before serving
        // bytes. This is a relative path - the Android client already knows
        // the API base URL.
        String path = "/api/verification/documents/" + documentId + "/raw";
        String token = currentBearerToken();
        if (token != null) {
            // Lets Android open this URL directly via an external ACTION_VIEW
            // intent (browser/PDF viewer), which can't attach an
            // Authorization header. See JwtAuthFilter's narrow fallback for
            // this exact endpoint. Not used in prod (Supabase signed URLs
            // carry their own auth in the URL already).
            path += "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        }
        return path;
    }

    private String currentBearerToken() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            return null;
        }
        String header = servletAttrs.getRequest().getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public Path resolvePath(String storageKey) {
        return baseDir.resolve(storageKey).normalize();
    }
}
