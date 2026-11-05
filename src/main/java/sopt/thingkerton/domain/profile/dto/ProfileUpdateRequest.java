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

public record ProfileUpdateRequest(
        @Schema(description = "닉네임 2~12자", example = "하나")
        @NotNull(message = "닉네임을 입력해 주세요.")
        String nickname,

        @Schema(description = "ko / en / vi / zh / ja", example = "vi")
        @NotBlank(message = "언어를 선택해 주세요.")
        String locale,

        @Schema(example = "2003-04-12")
        @NotNull(message = "생년월일을 입력해 주세요.")
        LocalDate birthDate,

        @Schema(example = "2026-03-01")
        @NotNull(message = "체류 시작일을 입력해 주세요.")
        LocalDate stayStartDate,

        @Schema(description = "stayStartDate 이후, 같은 날 허용", example = "2027-02-28")
        @NotNull(message = "체류 종료일을 입력해 주세요.")
        LocalDate stayEndDate,

        @Schema(description = "1개 이상", example = "[\"KPOP\", \"MOVIE\", \"FOOD\"]")
        @NotNull(message = "관심사를 1개 이상 선택해 주세요.")
        List<Interest> interests
) {

    public ProfileInfo toInfo() {
        EnumSet<Interest> interestSet = EnumSet.noneOf(Interest.class);
        interests.stream().filter(java.util.Objects::nonNull).forEach(interestSet::add);
        return new ProfileInfo(nickname, AppLocale.from(locale), birthDate, stayStartDate, stayEndDate, interestSet);
    }
}
