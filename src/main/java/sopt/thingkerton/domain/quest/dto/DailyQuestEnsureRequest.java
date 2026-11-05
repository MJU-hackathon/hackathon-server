package sopt.thingkerton.domain.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "오늘 퀘스트 생성 요청. 그날 첫 호출일 때만 좌표·언어가 쓰이고, 이후 호출에서는 무시됩니다.")
public record DailyQuestEnsureRequest(
        @Schema(description = "기기 현재 위도 (-90~90). 소수점 6자리로 반올림해 저장", example = "37.5718",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "위도를 입력해 주세요.")
        @DecimalMin(value = "-90", message = "위도는 -90~90이어야 합니다.")
        @DecimalMax(value = "90", message = "위도는 -90~90이어야 합니다.")
        BigDecimal latitude,

        @Schema(description = "기기 현재 경도 (-180~180). 소수점 6자리로 반올림해 저장", example = "126.9620",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "경도를 입력해 주세요.")
        @DecimalMin(value = "-180", message = "경도는 -180~180이어야 합니다.")
        @DecimalMax(value = "180", message = "경도는 -180~180이어야 합니다.")
        BigDecimal longitude,

        @Schema(description = "퀘스트 문구 언어. 생략하면 프로필 언어", example = "ko",
                allowableValues = {"ko", "en", "vi", "zh", "ja"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String locale
) {

    private static final int COORDINATE_SCALE = 6;

    public BigDecimal scaledLatitude() {
        return latitude.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal scaledLongitude() {
        return longitude.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
    }
}
