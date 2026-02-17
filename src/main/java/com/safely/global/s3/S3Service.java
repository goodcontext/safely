package com.safely.global.s3;

import com.safely.global.exception.BusinessException;
import com.safely.global.exception.ErrorCode;
import com.safely.global.exception.upload.FileUploadException;
import com.safely.global.exception.upload.InvalidFileExtensionException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    public String upload(MultipartFile file, String dirName) {
        if (file.isEmpty()) {
            log.warn("[!] 파일 업로드 실패: 파일이 비어있음.");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 파일명 가져오기 & Null 안전 처리
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unknown-file"; // 파일명이 없으면 기본값 부여
        }

        // 중복 방지를 위한 UUID 생성
        String uuid = UUID.randomUUID().toString();

        String key = dirName + "/" + uuid + "-" + originalFilename;

        // 확장자 검사 로직 (보안을 위해 필요함.)
        String extension = getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("[!] 파일 업로드 거부: 지원하지 않는 확장자. File={}, Ext={}", originalFilename, extension);
            throw new InvalidFileExtensionException();
        }

        // S3 업로드 및 URL 반환
        try (InputStream inputStream = file.getInputStream()) {
            var resource = s3Template.upload(bucketName, key, inputStream);
            log.info("[+] S3 파일 업로드 성공: Key={}", key);
            return resource.getURL().toString();
        } catch (IOException e) {
            log.error("[-] S3 업로드 중 치명적 오류 발생. Key={}, Cause={}", key, e.getMessage());
            throw new FileUploadException();
        }
    }

    public void deleteObject(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            String splitStr = ".com/";
            int index = imageUrl.indexOf(splitStr);
            if (index != -1) {
                String key = imageUrl.substring(index + splitStr.length());
                s3Template.deleteObject(bucketName, key);
                log.info("[-] S3 이미지 삭제 성공: Key={}", key);
            }
        } catch (Exception e) {
            log.error("[-] S3 이미지 삭제 실패. URL={}, Cause={}", imageUrl, e.getMessage());
        }
    }

    private String getExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex);
    }
}
