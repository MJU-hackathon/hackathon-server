package sopt.thingkerton.domain.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.thingkerton.domain.diary.code.DiarySuccessCode;
import sopt.thingkerton.domain.diary.dto.DiaryCreateRequest;
import sopt.thingkerton.domain.diary.dto.DiaryListResponse;
import sopt.thingkerton.domain.diary.dto.DiaryResponse;
import sopt.thingkerton.domain.diary.service.DiaryService;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Diary", description = "일기 저장(WF-04)·목록(WF-05)")
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(summary = "일기 저장",
            description = "사진을 S3에 저장하고 일기 생성과 퀘스트 RECORDED 전환을 함께 처리합니다. "
                    + "같은 draftId로 다시 오면 기존 일기를 반환합니다. "
                    + "종료된 퀘스트라도 draftStartedAt이 그 묶음 날짜 안이면 저장합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<DiaryResponse> create(@UserKey String userKey, @ModelAttribute DiaryCreateRequest request) {
        return CommonResponse.success(DiarySuccessCode.CREATE_DIARY,
                DiaryResponse.from(diaryService.create(userKey, request.toCommand())));
    }

    @Operation(summary = "일기 목록", description = "월별 묶음, 최신순. totalCount가 0이면 빈 상태(WF-05b)입니다.")
    @GetMapping
    public CommonResponse<DiaryListResponse> getDiaries(
            @UserKey String userKey,
            @Parameter(description = "ko / en / vi / zh / ja. 생략 시 프로필 언어")
            @RequestParam(required = false) String locale) {
        return CommonResponse.success(DiarySuccessCode.GET_DIARIES,
                DiaryListResponse.from(diaryService.getDiaries(userKey, AppLocale.fromNullable(locale))));
    }
}
