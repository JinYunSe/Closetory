package com.ssafy.closetory.service.s3;

import com.ssafy.closetory.exception.s3.S3UploadException;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@RequiredArgsConstructor
public class S3ImageServiceImpl implements S3ImageService {

  private final S3Client s3;

  @Value("${app.s3.bucket}")
  private String bucket;

  @Value("${app.s3.prefix}")
  private String prefix;

  @Value("${app.s3.public-base-url}")
  private String publicBaseUrl;

  @Override
  public String upload(MultipartFile file) {
    try {
      String safeName = (file.getOriginalFilename() == null) ? "file" : file.getOriginalFilename();
      String key = prefix + UUID.randomUUID() + "-" + safeName;

      PutObjectRequest req =
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType(file.getContentType())
              .build();

      s3.putObject(req, RequestBody.fromBytes(file.getBytes()));

      return publicBaseUrl + "/" + key;

    } catch (IOException e) {
      throw new S3UploadException("S3 업로드에 실패했습니다.", e);
    }
  }

  // ai 생성 사진 (byte) 변환용
  public String upload(byte[] imageBytes, String fileName) {
    String key = prefix + UUID.randomUUID() + "-" + fileName;

    PutObjectRequest req =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType("image/png") // PNG 고정
            .build();

    s3.putObject(req, RequestBody.fromBytes(imageBytes));

    return publicBaseUrl + "/" + key;
  }

  @Override
  public void deleteByUrl(String url) {
    String key = url.replace(publicBaseUrl + "/", "");
    s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }
}
