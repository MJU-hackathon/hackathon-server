package sopt.thingkerton.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.domain.image.dto.ImageUploadResponse;
import sopt.thingkerton.global.infra.s3.S3Uploader;
import sopt.thingkerton.global.infra.s3.UploadedFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final String UPLOAD_DIR = "uploads";

    private final S3Uploader s3Uploader;

    public ImageUploadResponse upload(MultipartFile file) {
        UploadedFile uploadedFile = s3Uploader.uploadImage(file, UPLOAD_DIR);
        return ImageUploadResponse.from(uploadedFile.url());
    }
}
