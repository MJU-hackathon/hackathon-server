package sopt.thingkerton.domain.profile.service;

import sopt.thingkerton.domain.profile.entity.Profile;

/**
 * 프로필과 오늘(Asia/Seoul) 기준으로 계산한 체류 정보.
 */
public record ProfileSummary(
        Profile profile,
        long dDay,
        int stayProgressPercent
) {
}
