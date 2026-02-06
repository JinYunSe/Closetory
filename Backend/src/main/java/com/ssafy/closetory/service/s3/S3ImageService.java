package com.ssafy.closetory.service.s3;

import org.springframework.web.multipart.MultipartFile;

public interface S3ImageService {
  String upload(MultipartFile file);

  String upload(byte[] imageBytes, String fileName);

  void deleteByUrl(String url);
}
