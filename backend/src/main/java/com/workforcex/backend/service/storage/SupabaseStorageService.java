package com.workforcex.backend.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * Production storage: private Supabase Storage bucket + short-lived signed
 * URLs. Chosen because this project already runs its production Postgres on
 * Supabase, so this reuses the same project/account and dashboard rather
 * than introducing a second cloud vendor. The Storage API is S3-compatible
 * behind the scenes, so this can be swapped for AWS S3 / Cloudflare R2 later
 * with the same interface if needed.
 *
 * Required env vars (see application-prod.properties):
 *   SUPABASE_STORAGE_URL          e.g. https://<project-ref>.supabase.co
 *   SUPABASE_SERVICE_ROLE_KEY     service_role key (NOT the anon/public key -
 *                                 this must stay server-side only)
 *   SUPABASE_STORAGE_BUCKET       a PRIVATE bucket, e.g. "verification-documents"
 *
 * The bucket must be created as private in the Supabase dashboard (Storage ->
 * New bucket -> uncheck "Public bucket"), since these are identity documents.
 */
@Service
@Profile("prod")
public class SupabaseStorageService implements FileStorageService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String baseUrl;
    private final String serviceKey;
    private final String bucket;
    private final long signedUrlExpirySeconds;

    public SupabaseStorageService(
            @Value("${app.storage.supabase.url}") String baseUrl,
            @Value("${app.storage.supabase.service-key}") String serviceKey,
            @Value("${app.storage.supabase.bucket:verification-documents}") String bucket,
            @Value("${app.storage.supabase.signed-url-expiry-seconds:900}") long signedUrlExpirySeconds
    ) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.signedUrlExpirySeconds = signedUrlExpirySeconds;
    }

    @Override
    public String store(MultipartFile file, UUID userId) throws Exception {
        String safeOriginalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("[^A-Za-z0-9._-]", "_")
                : "file";
        String storageKey = userId + "/" + UUID.randomUUID() + "_" + safeOriginalName;

        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

        HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);
        String uploadUrl = baseUrl + "/storage/v1/object/" + bucket + "/" + storageKey;
        restTemplate.exchange(uploadUrl, HttpMethod.POST, request, String.class);

        return storageKey;
    }

    @Override
    public String resolveUrl(UUID documentId, String storageKey) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("expiresIn", signedUrlExpirySeconds), headers);
        String signUrl = baseUrl + "/storage/v1/object/sign/" + bucket + "/" + storageKey;

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(signUrl, request, Map.class);
        String signedPath = response != null ? (String) response.get("signedURL") : null;
        if (signedPath == null) {
            throw new IllegalStateException("Failed to create signed URL for document " + documentId);
        }
        return baseUrl + "/storage/v1" + signedPath;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        return headers;
    }
}
