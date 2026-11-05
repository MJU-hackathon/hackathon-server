package sopt.thingkerton.domain.profile.dto;

import java.time.LocalDate;
import java.util.List;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.profile.entity.Profile;
import sopt.thingkerton.domain.profile.service.ProfileSummary;

public record ProfileResponse(
        String nickname,
        AppLocale locale,
        LocalDate birthDate,
        LocalDate stayStartDate,
        LocalDate stayEndDate,
        List<Interest> interests,
        long dDay,
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
