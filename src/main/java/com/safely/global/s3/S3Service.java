package com.safely.global.s3;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    // 허용할 확장자 목록
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    public String upload(MultipartFile file, String dirName) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 1. 파일명 가져오기 & Null 안전 처리
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unknown-file"; // 파일명이 없으면 기본값 부여
        }

        // 2. 중복 방지를 위한 UUID 생성
        String uuid = UUID.randomUUID().toString();

        String key = dirName + "/" + uuid + "-" + originalFilename;

        // 3. 확장자 검사 로직 (보안을 위해 필요함.)
        String extension = getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
        }

        // 4. S3 업로드 및 URL 반환
        try (InputStream inputStream = file.getInputStream()) {
            // upload()가 반환하는 S3Resource를 바로 사용 (download 불필요)
            var resource = s3Template.upload(bucketName, key, inputStream);
            return resource.getURL().toString();
        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
        }
    }

    // 이미지 삭제 (회원 탈퇴, 이미지 변경 시 사용)
    public void deleteObject(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            // URL에서 Key 추출 로직 (구현 방식에 따라 다를 수 있음, 간단하게 파일명만 추출하거나 전체 키 필요)
            // 여기서는 전체 URL이 아니라 DB에 저장된 방식에 따라 처리가 필요하지만,
            // S3Template은 'key'를 원하므로 URL 파싱이 필요할 수 있습니다.
            // 일단은 예외 처리 없이 넘어가거나, 필요 시 구현합니다.

            // 예: https://bucket.s3.ap-northeast-2.amazonaws.com/profile/abc.jpg -> profile/abc.jpg 추출
            String splitStr = ".com/";
            int index = imageUrl.indexOf(splitStr);
            if (index != -1) {
                String key = imageUrl.substring(index + splitStr.length());
                s3Template.deleteObject(bucketName, key);
                log.info("S3 이미지 삭제 성공: {}", key);
            }
        } catch (Exception e) {
            log.error("S3 이미지 삭제 실패: {}", e.getMessage());
        }
    }

    // 확장자 추출
    private String getExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex);
    }
}
