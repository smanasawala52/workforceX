package com.workforcex.backend.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

/**
 * Production storage: a private Supabase Storage bucket, accessed through
 * Supabase's S3-compatible protocol (https://supabase.com/docs/guides/storage/s3).
 * Chosen because this project already runs its production Postgres on
 * Supabase, so this reuses the same project/account and dashboard rather
 * than introducing a second cloud vendor, while still using the standard,
 * well-tested AWS S3 SDK (including its presigned-URL support) instead of
 * hand-rolled HTTP calls.
 *
 * Setup (Supabase dashboard -> Project Settings -> Storage -> S3 Connection):
 *   1. Create a PRIVATE bucket (e.g. "verification-documents") - these are
 *      identity documents, so it must NOT be a public bucket.
 *   2. Enable "Connect via S3 protocol" and generate an access key pair.
 *   3. Copy the endpoint, region, access key ID, and secret access key shown
 *      there into the env vars below.
 *
 * Required env vars (see application-prod.properties):
 *   SUPABASE_S3_ENDPOINT           e.g. https://<project-ref>.storage.supabase.co/storage/v1/s3
 *   SUPABASE_S3_REGION             region shown on the S3 Connection page
 *   SUPABASE_S3_ACCESS_KEY_ID      generated on the S3 Connection page
 *   SUPABASE_S3_SECRET_ACCESS_KEY  generated on the S3 Connection page (server-side only)
 *   SUPABASE_STORAGE_BUCKET        the private bucket name
 */
@Service
@Profile("prod")
public class SupabaseStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;
    private final Duration signedUrlExpiry;

    public SupabaseStorageService(
            @Value("${app.storage.supabase.endpoint}") String endpoint,
            @Value("${app.storage.supabase.region}") String region,
            @Value("${app.storage.supabase.access-key-id}") String accessKeyId,
            @Value("${app.storage.supabase.secret-access-key}") String secretAccessKey,
            @Value("${app.storage.supabase.bucket:verification-documents}") String bucket,
            @Value("${app.storage.supabase.signed-url-expiry-seconds:900}") long signedUrlExpirySeconds
    ) {
        this.bucket = bucket;
        this.signedUrlExpiry = Duration.ofSeconds(signedUrlExpirySeconds);

        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        // Supabase's S3-compatible endpoint requires path-style access
        // (bucket in the path, not as a subdomain) - same as the
        // `forcePathStyle: true` option in the JS/AWS SDK examples.
        S3Configuration pathStyleConfig = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .serviceConfiguration(pathStyleConfig)
                .build();

        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .serviceConfiguration(pathStyleConfig)
                .build();
    }

    @Override
    public String store(MultipartFile file, UUID userId) throws Exception {
        String safeOriginalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("[^A-Za-z0-9._-]", "_")
                : "file";
        String storageKey = userId + "/" + UUID.randomUUID() + "_" + safeOriginalName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
        return storageKey;
    }

    @Override
    public String resolveUrl(UUID documentId, String storageKey) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(signedUrlExpiry)
                .getObjectRequest(getRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}
