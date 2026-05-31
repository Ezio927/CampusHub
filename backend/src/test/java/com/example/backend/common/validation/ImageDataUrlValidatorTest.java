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
    void imageDataUrlShouldRejectUnsupportedOrBrokenPayload() {
        Assertions.assertThrows(BusinessException.class,
                () -> ImageDataUrlValidator.requireValidImageDataUrl("https://example.com/a.png", "图片"));
        Assertions.assertThrows(BusinessException.class,
                () -> ImageDataUrlValidator.requireValidImageDataUrl("data:image/svg+xml;base64,PHN2Zz4=", "图片"));
        Assertions.assertThrows(BusinessException.class,
                () -> ImageDataUrlValidator.requireValidImageDataUrl("data:image/png;base64,not base64", "图片"));
    }
}
