package sopt.thingkerton.domain.profile.service;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.thingkerton.domain.profile.code.ProfileErrorCode;
import sopt.thingkerton.domain.profile.entity.Profile;
import sopt.thingkerton.domain.profile.entity.ProfileInfo;
import sopt.thingkerton.domain.profile.repository.ProfileRepository;
import sopt.thingkerton.global.exception.BusinessException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 12;

    private final ProfileRepository profileRepository;
    private final Clock clock;

    /**
     * 관심사까지 로딩한 프로필. 없으면 {@code 404 PROFILE_NOT_FOUND}.
     */
    public Profile getProfile(String userKey) {
        return profileRepository.findWithInterestsByUserKey(userKey)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));
    }

    public ProfileSummary getSummary(String userKey) {
        return summarize(getProfile(userKey));
    }

    /**
     * 최초 설정과 수정을 함께 처리한다. 관심사 변경은 다음 날 묶음부터, 언어 변경은 즉시 반영된다
     * (오늘 묶음은 생성 시점 스냅샷을 쓰므로 여기서 따로 처리할 것이 없다).
     */
    @Transactional
    public ProfileSummary saveProfile(String userKey, ProfileInfo info) {
        ProfileInfo validated = validate(info);
        Profile profile = profileRepository.findWithInterestsByUserKey(userKey)
                .map(existing -> {
                    existing.update(validated);
                    return existing;
                })
                .orElseGet(() -> profileRepository.save(Profile.create(userKey, validated)));
        return summarize(profile);
    }

    private ProfileSummary summarize(Profile profile) {
        LocalDate today = LocalDate.now(clock);
        return new ProfileSummary(profile, profile.dDay(today), profile.stayProgressPercent(today));
    }

    private ProfileInfo validate(ProfileInfo info) {
        String nickname = info.nickname().strip();
        int length = nickname.codePointCount(0, nickname.length());
        if (length < NICKNAME_MIN_LENGTH || length > NICKNAME_MAX_LENGTH) {
            throw new BusinessException(ProfileErrorCode.INVALID_NICKNAME);
        }
        if (info.stayEndDate().isBefore(info.stayStartDate())) {
            throw new BusinessException(ProfileErrorCode.INVALID_STAY_PERIOD);
        }
        if (info.interests().isEmpty()) {
            throw new BusinessException(ProfileErrorCode.INTEREST_REQUIRED);
        }
        return new ProfileInfo(nickname, info.locale(), info.birthDate(), info.stayStartDate(),
                info.stayEndDate(), info.interests());
    }
}
