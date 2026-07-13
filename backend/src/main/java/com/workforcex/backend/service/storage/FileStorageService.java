package com.workforcex.backend.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Abstraction over where uploaded verification documents (Aadhaar, PAN,
 * passport, photos, etc.) actually live.
 *
 * Implementations:
 *  - {@link LocalFileStorageService}: writes to local disk. Used for the
 *    "dev" profile only - fine for local testing, NOT suitable for
 *    production on platforms with ephemeral filesystems (e.g. Railway),
 *    since files are lost on every redeploy/restart.
 *  - {@link SupabaseStorageService}: stores in a private Supabase Storage
 *    bucket and returns short-lived signed URLs. Used for "prod".
 *
 * Callers should persist only the storageKey returned by store(), never a
 * raw URL - the real access URL is resolved fresh on every read via
 * resolveUrl(), so it can be a short-lived signed URL without going stale.
 */
public interface FileStorageService {

    /**
     * Saves the file and returns an opaque storage key to persist on the
     * Document entity (e.g. a relative path or an object-storage key).
     */
    String store(MultipartFile file, UUID userId) throws Exception;

    /**
     * Resolves a document into a URL the client can use right now to view
     * the file. May be a signed, time-limited URL - do not cache/persist it.
     *
     * @param documentId the Document's own id (useful for implementations
     *                   that route through an app-controlled endpoint rather
     *                   than exposing storageKey directly)
     * @param storageKey the opaque key returned by store()
     */
    String resolveUrl(UUID documentId, String storageKey);
}
