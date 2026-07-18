package com.workforcex.backend.service.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    private Path tempDir;
    private LocalFileStorageService service;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("workforcex-storage-test");
        service = new LocalFileStorageService(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        RequestContextHolder.resetRequestAttributes();
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    void store_savesFileUnderUserIdDirectory() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "aadhaar.pdf", "application/pdf", "content".getBytes());

        String storageKey = service.store(file, userId);

        assertThat(storageKey).startsWith(userId + "/").endsWith("_aadhaar.pdf");
        assertThat(Files.exists(service.resolvePath(storageKey))).isTrue();
    }

    @Test
    void store_missingOriginalFilename_fallsBackToGenericName() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", null, "application/pdf", "content".getBytes());

        String storageKey = service.store(file, userId);

        assertThat(storageKey).endsWith("_file");
    }

    @Test
    void resolveUrl_noRequestContext_returnsPathWithoutTokenParam() {
        UUID documentId = UUID.randomUUID();

        String url = service.resolveUrl(documentId, "some/key");

        assertThat(url).isEqualTo("/api/verification/documents/" + documentId + "/raw");
    }

    @Test
    void resolveUrl_withBearerTokenOnRequest_appendsEncodedTokenParam() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc.def.ghi");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UUID documentId = UUID.randomUUID();
        String url = service.resolveUrl(documentId, "some/key");

        assertThat(url).isEqualTo("/api/verification/documents/" + documentId + "/raw?token=abc.def.ghi");
    }

    @Test
    void resolveUrl_nonBearerAuthorizationHeader_noTokenParamAppended() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UUID documentId = UUID.randomUUID();
        String url = service.resolveUrl(documentId, "some/key");

        assertThat(url).isEqualTo("/api/verification/documents/" + documentId + "/raw");
    }

    @Test
    void resolvePath_returnsPathUnderBaseDir() {
        Path path = service.resolvePath("abc/def.pdf");

        assertThat(path).isEqualTo(tempDir.toAbsolutePath().normalize().resolve("abc/def.pdf").normalize());
    }
}
