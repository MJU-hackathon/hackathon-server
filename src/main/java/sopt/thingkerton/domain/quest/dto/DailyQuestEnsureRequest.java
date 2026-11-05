package sopt.thingkerton.domain.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record DailyQuestEnsureRequest(
        @Schema(description = "기기 현재 위도", example = "37.5718")
        @NotNull(message = "위도를 입력해 주세요.")
        @DecimalMin(value = "-90", message = "위도는 -90~90이어야 합니다.")
        @DecimalMax(value = "90", message = "위도는 -90~90이어야 합니다.")
        BigDecimal latitude,

        @Schema(description = "기기 현재 경도", example = "126.9620")
        @NotNull(message = "경도를 입력해 주세요.")
        @DecimalMin(value = "-180", message = "경도는 -180~180이어야 합니다.")
        @DecimalMax(value = "180", message = "경도는 -180~180이어야 합니다.")
        BigDecimal longitude,

        @Schema(description = "ko / en / vi / zh / ja. 생략 시 프로필 언어", example = "ko")
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
