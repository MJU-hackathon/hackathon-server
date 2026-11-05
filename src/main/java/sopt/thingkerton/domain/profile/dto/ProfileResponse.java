package sopt.thingkerton.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.profile.entity.Profile;
import sopt.thingkerton.domain.profile.service.ProfileSummary;

@Schema(description = "프로필과 홈 상단 표시용 체류 정보")
public record ProfileResponse(
        @Schema(description = "닉네임", example = "하나")
        String nickname,

        @Schema(description = "사용 언어", example = "ko", type = "string", allowableValues = {"ko", "en", "vi", "zh", "ja"})
        AppLocale locale,

        @Schema(description = "생년월일", example = "2003-04-12")
        LocalDate birthDate,

        @Schema(description = "한국 체류 시작일", example = "2026-03-01")
        LocalDate stayStartDate,

        @Schema(description = "귀국 예정일", example = "2027-02-28")
        LocalDate stayEndDate,

        @Schema(description = "관심사 (enum 선언 순서로 정렬)", example = "[\"KPOP\", \"MOVIE\", \"FOOD\"]")
        List<Interest> interests,

        @Schema(description = "귀국까지 남은 일수. Asia/Seoul 오늘 기준, 귀국 당일과 지난 경우 0 → 홈 `D-114`", example = "114")
        long dDay,

        @Schema(description = "체류 기간 경과율 0~100 (내림) → 홈 진행 바", example = "68", minimum = "0", maximum = "100")
        int stayProgressPercent
) {

    public static ProfileResponse from(ProfileSummary summary) {
        Profile profile = summary.profile();
        return new ProfileResponse(
                profile.getNickname(),
                profile.getLocale(),
                profile.getBirthDate(),
                profile.getStayStartDate(),
                profile.getStayEndDate(),
                profile.getInterests().stream().sorted().toList(),
                summary.dDay(),
                summary.stayProgressPercent());
    }
}
