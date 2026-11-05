package sopt.thingkerton.domain.dev;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sopt.thingkerton.domain.dev.controller.DevResetController;
import sopt.thingkerton.support.FakeAiConfig;
import sopt.thingkerton.support.FixedClockConfig;

/**
 * 기본 설정(APP_DEV_RESET_ENABLED 미설정)에서는 초기화 API가 노출되지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockConfig.class, FakeAiConfig.class})
class DevResetDisabledTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationContext context;

    @Test
    void 설정이_꺼져_있으면_초기화_API는_404() throws Exception {
        assertThat(context.getBeanNamesForType(DevResetController.class)).isEmpty();
        mockMvc.perform(delete("/api/dev/daily-quests/today").header("X-User-Key", "user-1"))
                .andExpect(status().isNotFound());
    }
}
