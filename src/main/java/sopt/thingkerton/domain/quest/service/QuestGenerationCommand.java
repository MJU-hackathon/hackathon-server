package sopt.thingkerton.domain.quest.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;

/**
 * 퀘스트 생성 입력. 체류 기간은 의도적으로 넣지 않는다.
 */
public record QuestGenerationCommand(
        BigDecimal latitude,
        BigDecimal longitude,
        List<Interest> interests,
        Set<PlaceActivityKey> excludedKeys,
        AppLocale locale
) {
}
