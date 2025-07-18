package com.hamss2.KINO.api.image.controller;

import com.hamss2.KINO.api.image.config.GcsUploader;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {
    private final GcsUploader gcsUploader;

    @PostMapping(value = "/img", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> img(
            @RequestPart(value = "file", required = true) MultipartFile file) {
        String newUrl = gcsUploader.uploadFile(file);
        return ApiResponse.success(SuccessStatus.STORE_IMG_SUCCESS, newUrl);
    }
}
