package com.workforcex.backend.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageServiceTest {

    @Mock private S3Client s3Client;
    @Mock private S3Presigner presigner;
    @Mock private PresignedGetObjectRequest presignedRequest;

    private SupabaseStorageService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new SupabaseStorageService(
                "http://localhost:54321/storage/v1/s3",
                "us-east-1",
                "test-access-key",
                "test-secret-key",
                "verification-documents",
                900L
        );
        ReflectionTestUtils.setField(service, "s3Client", s3Client);
        ReflectionTestUtils.setField(service, "presigner", presigner);
    }

    @Test
    void store_uploadsFileAndReturnsStorageKeyPrefixedByUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "aadhaar card.pdf", "application/pdf", "content".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String storageKey = service.store(file, userId);

        assertThat(storageKey).startsWith(userId + "/");
        assertThat(storageKey).endsWith("_aadhaar_card.pdf");
    }

    @Test
    void store_sanitizesUnsafeCharactersInFilename() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../etc/passwd.pdf", "application/pdf", "content".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String storageKey = service.store(file, userId);

        assertThat(storageKey).doesNotContain("/../").doesNotContain("etc/passwd");
    }

    @Test
    void store_missingFilename_fallsBackToGenericName() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", null, null, "content".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String storageKey = service.store(file, userId);

        assertThat(storageKey).endsWith("_file");
    }

    @Test
    void resolveUrl_returnsPresignedUrlFromPresigner() throws Exception {
        URL fakeUrl = URI.create("https://storage.example.com/signed?sig=abc").toURL();
        when(presignedRequest.url()).thenReturn(fakeUrl);
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        String url = service.resolveUrl(UUID.randomUUID(), "some/storage/key");

        assertThat(url).isEqualTo("https://storage.example.com/signed?sig=abc");
    }
}
