package sopt.thingkerton.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.profile.entity.ProfileInfo;

@Schema(description = "프로필 저장 요청. 최초 설정과 수정 모두 전체 값을 보냅니다.")
public record ProfileUpdateRequest(
        @Schema(description = "닉네임. 앞뒤 공백 제거 후 2~12자", example = "하나", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "닉네임을 입력해 주세요.")
        String nickname,

        @Schema(description = "사용 언어. 퀘스트 안내 문구가 이 언어로 나옵니다", example = "ko",
                allowableValues = {"ko", "en", "vi", "zh", "ja"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "언어를 선택해 주세요.")
        String locale,

        @Schema(description = "생년월일 (YYYY-MM-DD)", example = "2003-04-12", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "생년월일을 입력해 주세요.")
        LocalDate birthDate,

        @Schema(description = "한국 체류 시작일 (YYYY-MM-DD)", example = "2026-03-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "체류 시작일을 입력해 주세요.")
        LocalDate stayStartDate,

        @Schema(description = "귀국 예정일 (YYYY-MM-DD). stayStartDate와 같거나 이후", example = "2027-02-28",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "체류 종료일을 입력해 주세요.")
        LocalDate stayEndDate,

        @Schema(description = "관심사 1개 이상. 퀘스트 추천에 우선 반영되며 다른 분야도 섞여 추천됩니다", example = "[\"KPOP\", \"MOVIE\", \"FOOD\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "관심사를 1개 이상 선택해 주세요.")
        List<Interest> interests
) {

    public ProfileInfo toInfo() {
        EnumSet<Interest> interestSet = EnumSet.noneOf(Interest.class);
        interests.stream().filter(java.util.Objects::nonNull).forEach(interestSet::add);
        return new ProfileInfo(nickname, AppLocale.from(locale), birthDate, stayStartDate, stayEndDate, interestSet);
    }
}
