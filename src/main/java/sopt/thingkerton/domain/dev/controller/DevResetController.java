package sopt.thingkerton.domain.dev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sopt.thingkerton.domain.dev.code.DevSuccessCode;
import sopt.thingkerton.domain.dev.dto.DevResetResponse;
import sopt.thingkerton.domain.dev.service.DevResetService;
import sopt.thingkerton.global.auth.UserKey;
import sopt.thingkerton.global.dto.CommonResponse;

/**
 * 개발·QA용 초기화 API. {@code app.dev.reset-enabled=true}가 아니면 빈으로 등록되지 않아 404가 된다.
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.dev.reset-enabled", havingValue = "true")
public class DevResetController implements DevResetApi {

    private final DevResetService devResetService;

    @DeleteMapping("/daily-quests/today")
    @Override
    public CommonResponse<DevResetResponse> resetToday(@UserKey String userKey) {
        int deletedQuestCount = devResetService.resetToday(userKey);
        return CommonResponse.success(DevSuccessCode.RESET_TODAY_QUESTS, DevResetResponse.from(deletedQuestCount));
    }
}
