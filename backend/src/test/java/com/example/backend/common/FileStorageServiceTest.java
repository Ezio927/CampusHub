package com.example.backend.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

class FileStorageServiceTest {

    @TempDir
    Path uploadDir;

    @Test
    void storeShouldAcceptJpegByMagicBytesEvenWhenMimeIsNotStandard() {
        FileStorageService service = new FileStorageService(uploadDir.toString());
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "application/octet-stream",
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10});

        String url = service.store(file);

        Assertions.assertTrue(url.startsWith("/uploads/"));
        Assertions.assertTrue(url.endsWith(".jpg"));
        Assertions.assertTrue(Files.exists(uploadDir.resolve(url.substring("/uploads/".length()))));
    }

    @Test
    void storeShouldRejectTextDisguisedAsJpeg() {
        FileStorageService service = new FileStorageService(uploadDir.toString());
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "not image".getBytes());

        Assertions.assertThrows(BusinessException.class, () -> service.store(file));
    }
}
