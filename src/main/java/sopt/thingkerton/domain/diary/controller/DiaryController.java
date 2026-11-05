package sopt.thingkerton.domain.diary.controller;

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

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController implements DiaryApi {

    private final DiaryService diaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public CommonResponse<DiaryResponse> create(@UserKey String userKey, @ModelAttribute DiaryCreateRequest request) {
        return CommonResponse.success(DiarySuccessCode.CREATE_DIARY,
                DiaryResponse.from(diaryService.create(userKey, request.toCommand())));
    }

    @GetMapping
    @Override
    public CommonResponse<DiaryListResponse> getDiaries(
            @UserKey String userKey,
            @RequestParam(required = false) String locale) {
        return CommonResponse.success(DiarySuccessCode.GET_DIARIES,
                DiaryListResponse.from(diaryService.getDiaries(userKey, AppLocale.fromNullable(locale))));
    }
}
