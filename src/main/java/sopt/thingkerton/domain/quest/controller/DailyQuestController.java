package sopt.thingkerton.domain.quest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.quest.code.QuestSuccessCode;
import sopt.thingkerton.domain.quest.dto.DailyQuestEnsureRequest;
import sopt.thingkerton.domain.quest.dto.QuestCardResponse;
import sopt.thingkerton.domain.quest.service.DailyQuestService;
import sopt.thingkerton.domain.quest.service.LocalizedQuest;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "DailyQuest", description = "오늘의 퀘스트 (WF-02)")
@RestController
@RequestMapping("/api/daily-quests")
@RequiredArgsConstructor
public class DailyQuestController {

    private final DailyQuestService dailyQuestService;

    @Operation(summary = "오늘 묶음 생성 또는 기존 묶음 반환",
            description = "그날 첫 호출이면 요청 좌표 반경 3km 안 퀘스트 3개를 AI로 생성합니다. "
                    + "이후 호출은 좌표·관심사가 달라도 같은 묶음의 남은 퀘스트를 반환합니다. "
                    + "AI 생성 실패 시 데모 퀘스트 3개로 대체합니다.")
    @PostMapping("/ensure")
    public CommonResponse<List<QuestCardResponse>> ensure(
            @UserKey String userKey,
            @Valid @RequestBody DailyQuestEnsureRequest request) {
        List<LocalizedQuest> quests = dailyQuestService.ensure(userKey, request.scaledLatitude(),
                request.scaledLongitude(), AppLocale.fromNullable(request.locale()));
        return CommonResponse.success(QuestSuccessCode.ENSURE_DAILY_QUESTS, QuestCardResponse.listOf(quests));
    }

    @Operation(summary = "오늘 남은 퀘스트 조회",
            description = "오늘 묶음이 없으면 404 DAILY_SET_NOT_FOUND(앱은 ensure 호출), 모두 처리했으면 빈 배열입니다.")
    @GetMapping("/today")
    public CommonResponse<List<QuestCardResponse>> getToday(
            @UserKey String userKey,
            @Parameter(description = "ko / en / vi / zh / ja. 생략 시 프로필 언어")
            @RequestParam(required = false) String locale) {
        List<LocalizedQuest> quests = dailyQuestService.getToday(userKey, AppLocale.fromNullable(locale));
        return CommonResponse.success(QuestSuccessCode.GET_TODAY_QUESTS, QuestCardResponse.listOf(quests));
    }
}
