package com.example.backend.common.validation;

import com.example.backend.common.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ImageDataUrlValidatorTest {

    @Test
    void imageDataUrlShouldAllowSupportedBase64Images() {
        String png = "data:image/png;base64,iVBORw0KGgo=";
        Assertions.assertEquals(png, ImageDataUrlValidator.requireValidImageDataUrl(png, "图片"));
    }

    @Test
    void imageReferenceShouldAllowLocalUploadedImages() {
        String jpg = "/uploads/123e4567-e89b-12d3-a456-426614174000.jpg";
        Assertions.assertEquals(jpg, ImageDataUrlValidator.requireValidImageReference(jpg, "图片"));
    }

    @Test
    void uploadedImagePathShouldOnlyAllowLocalUploads() {
        String avatar = "/uploads/avatar-1.webp";
        Assertions.assertEquals(avatar, ImageDataUrlValidator.requireValidUploadedImagePath(avatar, "头像"));
        Assertions.assertThrows(BusinessException.class,
                () -> ImageDataUrlValidator.requireValidUploadedImagePath("data:image/png;base64,iVBORw0KGgo=", "头像"));
    }

    @Test
    void imageDataUrlShouldRejectUnsupportedOrBrokenPayload() {
        Assertions.assertThrows(BusinessException.class,
                () -> ImageDataUrlValidator.requireValidImageDataUrl("https://example.com/a.png", "图片"));
        Assertions.assertThrows(BusinessException.class,
                () -> ImageDataUrlValidator.requireValidImageDataUrl("data:image/svg+xml;base64,PHN2Zz4=", "图片"));
        Assertions.assertThrows(BusinessException.class,
                () -> ImageDataUrlValidator.requireValidImageDataUrl("data:image/png;base64,not base64", "图片"));
        Assertions.assertThrows(BusinessException.class,
                () -> ImageDataUrlValidator.requireValidImageReference("https://example.com/a.png", "图片"));
    }
}
