package sopt.thingkerton.domain.quest.controller;

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

@RestController
@RequestMapping("/api/daily-quests")
@RequiredArgsConstructor
public class DailyQuestController implements DailyQuestApi {

    private final DailyQuestService dailyQuestService;

    @PostMapping("/ensure")
    @Override
    public CommonResponse<List<QuestCardResponse>> ensure(
            @UserKey String userKey,
            @Valid @RequestBody DailyQuestEnsureRequest request) {
        List<LocalizedQuest> quests = dailyQuestService.ensure(userKey, request.scaledLatitude(),
                request.scaledLongitude(), AppLocale.fromNullable(request.locale()));
        return CommonResponse.success(QuestSuccessCode.ENSURE_DAILY_QUESTS, QuestCardResponse.listOf(quests));
    }

    @GetMapping("/today")
    @Override
    public CommonResponse<List<QuestCardResponse>> getToday(
            @UserKey String userKey,
            @RequestParam(required = false) String locale) {
        List<LocalizedQuest> quests = dailyQuestService.getToday(userKey, AppLocale.fromNullable(locale));
        return CommonResponse.success(QuestSuccessCode.GET_TODAY_QUESTS, QuestCardResponse.listOf(quests));
    }
}
