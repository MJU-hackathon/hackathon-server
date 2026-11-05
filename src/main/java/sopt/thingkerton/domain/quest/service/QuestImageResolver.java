package sopt.thingkerton.domain.quest.service;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.global.infra.s3.S3Uploader;

/**
 * 퀘스트 대표 이미지 참조(image_ref) ↔ URL.
 * <ul>
 *   <li>AI 생성 이미지: S3 키 {@code quest-images/{UUID}.png}. S3 URL로 변환</li>
 *   <li>기본 이미지(데모 퀘스트·생성 실패): 카테고리 파일명 {@code food.jpg}. {@code image-base-url} 뒤에 붙인다</li>
 * </ul>
 * TODO: 팀이 전달하는 기본 이미지·매핑 기준(카테고리·활동)으로 교체. 지금은 카테고리별 임시 파일명이다.
 */
@Component
public class QuestImageResolver {

    /** AI 생성 이미지를 저장하는 S3 디렉터리 */
    public static final String GENERATED_DIR = "quest-images";

    private final String baseUrl;
    private final S3Uploader s3Uploader;

    public QuestImageResolver(@Value("${app.quest.image-base-url}") String baseUrl, S3Uploader s3Uploader) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.s3Uploader = s3Uploader;
    }

    /**
     * 카테고리 기본 이미지 참조.
     */
    public String resolveRef(Interest category, String activityKey) {
        return category.name().toLowerCase(Locale.ROOT) + ".jpg";
    }

    public boolean isGenerated(String imageRef) {
        return imageRef.startsWith(GENERATED_DIR + "/");
    }

    public String toUrl(String imageRef) {
        return isGenerated(imageRef) ? s3Uploader.getUrl(imageRef) : baseUrl + "/" + imageRef;
    }
}
