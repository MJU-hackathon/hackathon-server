package sopt.thingkerton.domain.quest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Quest", description = "퀘스트 상세·패스 (WF-03)")
@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

    @Operation(summary = "퀘스트 상세", description = "같은 장소·활동을 요청 언어로 설명합니다. 본인 묶음의 퀘스트만 조회할 수 있습니다. "
            + "address·latitude·longitude는 퀘스트 생성 시 AI가 만든 주소를 네이버 Geocoding으로 변환한 값이며, 변환에 실패하면 null입니다.")
    @GetMapping("/{questId}")
    public CommonResponse<QuestDetailResponse> getDetail(
            @UserKey String userKey,
            @PathVariable Long questId,
            @Parameter(description = "ko / en / vi / zh / ja. 생략 시 프로필 언어")
            @RequestParam(required = false) String locale) {
        return CommonResponse.success(QuestSuccessCode.GET_QUEST_DETAIL,
                QuestDetailResponse.from(questService.getDetail(userKey, questId, AppLocale.fromNullable(locale))));
    }

    @Operation(summary = "퀘스트 패스", description = "홈에서 제거합니다. 새 퀘스트를 보충하지 않고 일기도 만들지 않습니다.")
    @PostMapping("/{questId}/pass")
    public CommonResponse<QuestPassResponse> pass(@UserKey String userKey, @PathVariable Long questId) {
        return CommonResponse.success(QuestSuccessCode.PASS_QUEST,
                QuestPassResponse.from(questService.pass(userKey, questId)));
    }
}
