package sopt.thingkerton.domain.profile.entity;

import java.time.LocalDate;
import java.util.Set;

/**
 * 프로필 생성·수정에 쓰는 값 묶음. 검증은 Service에서 마친 뒤 넘긴다.
 */
public record ProfileInfo(
        String nickname,
        AppLocale locale,
        LocalDate birthDate,
        LocalDate stayStartDate,
        LocalDate stayEndDate,
        Set<Interest> interests
) {
}
