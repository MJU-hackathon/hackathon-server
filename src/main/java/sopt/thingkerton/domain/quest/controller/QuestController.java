package sopt.thingkerton.domain.quest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.quest.code.QuestSuccessCode;
import sopt.thingkerton.domain.quest.dto.QuestDetailResponse;
import sopt.thingkerton.domain.quest.dto.QuestPassResponse;
import sopt.thingkerton.domain.quest.service.QuestService;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.dto.CommonResponse;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController implements QuestApi {

    private final QuestService questService;

    @GetMapping("/{questId}")
    @Override
    public CommonResponse<QuestDetailResponse> getDetail(
            @UserKey String userKey,
            @PathVariable Long questId,
            @RequestParam(required = false) String locale) {
        return CommonResponse.success(QuestSuccessCode.GET_QUEST_DETAIL,
                QuestDetailResponse.from(questService.getDetail(userKey, questId, AppLocale.fromNullable(locale))));
    }

    @PostMapping("/{questId}/pass")
    @Override
    public CommonResponse<QuestPassResponse> pass(@UserKey String userKey, @PathVariable Long questId) {
        return CommonResponse.success(QuestSuccessCode.PASS_QUEST,
                QuestPassResponse.from(questService.pass(userKey, questId)));
    }
}
