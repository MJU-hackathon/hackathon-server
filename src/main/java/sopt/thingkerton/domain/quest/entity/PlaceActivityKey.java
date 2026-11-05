package sopt.thingkerton.domain.quest.entity;

import java.util.Locale;

/**
 * 같은 경험(장소 + 활동)을 묶는 정규화 키 쌍. 이미 경험한 조합을 다음 생성에서 제외할 때 쓴다.
 */
public record PlaceActivityKey(String placeKey, String activityKey) {

    public PlaceActivityKey {
        placeKey = normalize(placeKey);
        activityKey = normalize(activityKey);
    }

    /**
     * 소문자로 바꾸고 공백·밑줄은 하이픈으로, 문자·숫자·하이픈 외 문자는 제거한다. (예: "Yeongcheon Market" → "yeongcheon-market")
     */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String slug = value.strip().toLowerCase(Locale.ROOT)
                .replaceAll("[\\s_]+", "-")
                .replaceAll("[^\\p{L}\\p{N}-]", "")
                .replaceAll("-{2,}", "-");
        return slug.replaceAll("^-|-$", "");
    }
}
