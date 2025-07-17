package com.hamss2.KINO.api.image.config;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GcsUploader {
    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final Storage storage;

    public String uploadFile(MultipartFile file) {
        try {
            String fileName = "profile/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);

        } catch (IOException e) {
            throw new RuntimeException("GCS 업로드 실패", e);
        }
    }

    public void deleteFile(String imageUrl) {
        try {
            // imageUrl이 null이거나 비어있는 경우 처리
            if (imageUrl == null || imageUrl.isEmpty()) {
                System.out.println("삭제할 이미지 URL이 없습니다.");
                return;
            }

            // 이미지 URL에서 버킷 이름과 객체 이름을 파싱 (ex: https://storage.googleapis.com/{bucket}/{object})
            int bucketIndex = imageUrl.indexOf(bucketName);
            if (bucketIndex == -1) {
                System.out.println("올바르지 않은 이미지 URL 형식입니다: " + imageUrl);
                return;
            }

            int objectStartIndex = bucketIndex + bucketName.length() + 1;
            if (objectStartIndex >= imageUrl.length()) {
                System.out.println("객체 이름을 추출할 수 없습니다: " + imageUrl);
                return;
            }

            String objectName = imageUrl.substring(objectStartIndex);
            
            BlobId blobId = BlobId.of(bucketName, objectName);
            boolean deleted = storage.delete(blobId);
            if (!deleted) {
                System.out.println("삭제 실패 또는 파일 없음: " + objectName);
            }
        } catch (Exception e) {
            System.out.println("파일 삭제 중 오류 발생: " + e.getMessage());
        }
    }
}
