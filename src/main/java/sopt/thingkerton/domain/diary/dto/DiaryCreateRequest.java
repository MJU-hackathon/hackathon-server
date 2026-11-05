package sopt.thingkerton.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.domain.diary.code.DiaryErrorCode;
import sopt.thingkerton.domain.diary.entity.Outcome;
import sopt.thingkerton.domain.diary.service.DiaryCreateCommand;
import sopt.thingkerton.global.code.GlobalErrorCode;
import sopt.thingkerton.global.exception.BusinessException;
import sopt.thingkerton.global.time.SeoulTime;

/**
 * multipart/form-data 일기 저장 요청. 누락 시 명세서의 에러 코드로 응답하기 위해 모든 파트를 선택값으로 받고
 * {@link #toCommand()}에서 검증한다.
 */
public record DiaryCreateRequest(
        @Schema(description = "사진 1장 (jpeg/png/heic, 최대 10MB)", type = "string", format = "binary")
        List<MultipartFile> photo,

        @Schema(example = "1201")
        Long questId,

        @Schema(description = "EXPERIENCED / NOT_EXPERIENCED", example = "EXPERIENCED")
        String outcome,

        @Schema(description = "최대 50자, 공란 허용", example = "시장이 문을 닫아 다음에 다시 오기로 했다.")
        String memo,

        @Schema(description = "촬영 시작 시 앱이 만든 UUID (중복 제출 방지)", example = "5f1c2a9e-8f3b-4e4f-9d61-2b7c0e9a1d10")
        String draftId,

        @Schema(description = "촬영 시작 시각 ISO 8601", example = "2026-11-05T23:58:00+09:00")
        String draftStartedAt
) {

    private static final int DRAFT_ID_MAX_LENGTH = 64;

    public DiaryCreateCommand toCommand() {
        List<MultipartFile> photos = photo == null ? List.of()
                : photo.stream().filter(file -> file != null && !file.isEmpty()).toList();
        if (photos.size() != 1) {
            throw new BusinessException(DiaryErrorCode.PHOTO_REQUIRED);
        }
        if (!StringUtils.hasText(outcome)) {
            throw new BusinessException(DiaryErrorCode.OUTCOME_REQUIRED);
        }
        if (questId == null || !StringUtils.hasText(draftId) || draftId.length() > DRAFT_ID_MAX_LENGTH) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        return new DiaryCreateCommand(questId, photos.getFirst(), parseOutcome(), StringUtils.hasText(memo) ? memo : null,
                draftId.strip(), parseDraftStartedAt());
    }

    private Outcome parseOutcome() {
        try {
            return Outcome.valueOf(outcome.strip());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 오프셋이 있으면 Asia/Seoul로 변환하고, 없으면 Asia/Seoul 시각으로 본다.
     */
    private LocalDateTime parseDraftStartedAt() {
        if (!StringUtils.hasText(draftStartedAt)) {
            throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
        }
        String value = draftStartedAt.strip();
        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(SeoulTime.ZONE).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException exception) {
                throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
            }
        }
    }
}
