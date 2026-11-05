package sopt.thingkerton.domain.diary.service;

import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.domain.diary.entity.Outcome;

/**
 * @param memo           공란이면 null
 * @param draftStartedAt Asia/Seoul 기준 촬영 시작 시각
 */
public record DiaryCreateCommand(
        Long questId,
        MultipartFile photo,
        Outcome outcome,
        String memo,
        String draftId,
        LocalDateTime draftStartedAt
) {
}
