package sopt.thingkerton.domain.profile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import sopt.thingkerton.support.FixedClockConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FixedClockConfig.class)
class ProfileApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void X_User_Key가_없으면_401() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("USER_KEY_REQUIRED"));
    }

    @Test
    void 프로필이_없으면_404_PROFILE_NOT_FOUND() throws Exception {
        mockMvc.perform(get("/api/profile").header("X-User-Key", newUserKey()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROFILE_NOT_FOUND"));
    }

    @Test
    void 로그인_없이_프로필을_설정하고_조회한다() throws Exception {
        String userKey = newUserKey();

        putProfile(userKey, body("하나", "vi", "2026-03-01", "2027-02-28", "[\"KPOP\",\"FOOD\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("UPDATE_PROFILE"))
                .andExpect(jsonPath("$.data.nickname").value("하나"))
                .andExpect(jsonPath("$.data.locale").value("vi"));

        // 오늘 2026-11-06(Asia/Seoul) → 귀국까지 114일, 364일 중 250일 경과
        mockMvc.perform(get("/api/profile").header("X-User-Key", userKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_PROFILE"))
                .andExpect(jsonPath("$.data.interests.length()").value(2))
                .andExpect(jsonPath("$.data.dDay").value(114))
                .andExpect(jsonPath("$.data.stayProgressPercent").value(68));
    }

    @Test
    void 같은_사용자가_다시_저장하면_수정된다() throws Exception {
        String userKey = newUserKey();
        putProfile(userKey, body("하나", "vi", "2026-03-01", "2027-02-28", "[\"KPOP\"]")).andExpect(status().isOk());

        putProfile(userKey, body("두리", "en", "2026-03-01", "2027-02-28", "[\"MOVIE\",\"GAME\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("두리"))
                .andExpect(jsonPath("$.data.locale").value("en"))
                .andExpect(jsonPath("$.data.interests[0]").value("MOVIE"))
                .andExpect(jsonPath("$.data.interests[1]").value("GAME"));
    }

    @Test
    void 닉네임_1자와_13자는_거부하고_2자와_12자는_허용한다() throws Exception {
        putProfile(newUserKey(), body("가", "ko", "2026-03-01", "2027-02-28", "[\"FOOD\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_NICKNAME"));
        putProfile(newUserKey(), body("가".repeat(13), "ko", "2026-03-01", "2027-02-28", "[\"FOOD\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_NICKNAME"));
        putProfile(newUserKey(), body("가나", "ko", "2026-03-01", "2027-02-28", "[\"FOOD\"]"))
                .andExpect(status().isOk());
        putProfile(newUserKey(), body("가".repeat(12), "ko", "2026-03-01", "2027-02-28", "[\"FOOD\"]"))
                .andExpect(status().isOk());
    }

    @Test
    void 체류_종료일이_시작일보다_앞이면_거부하고_같은_날은_허용한다() throws Exception {
        putProfile(newUserKey(), body("하나", "ko", "2026-03-01", "2026-02-28", "[\"FOOD\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STAY_PERIOD"));
        putProfile(newUserKey(), body("하나", "ko", "2026-03-01", "2026-03-01", "[\"FOOD\"]"))
                .andExpect(status().isOk());
    }

    @Test
    void 관심사_0개와_지원하지_않는_언어는_거부한다() throws Exception {
        putProfile(newUserKey(), body("하나", "ko", "2026-03-01", "2027-02-28", "[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INTEREST_REQUIRED"));
        putProfile(newUserKey(), body("하나", "fr", "2026-03-01", "2027-02-28", "[\"FOOD\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_LOCALE"));
        putProfile(newUserKey(), body("하나", "ko", "2026-03-01", "2027-02-28", "[\"SPORTS\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    private ResultActions putProfile(String userKey, String body) throws Exception {
        return mockMvc.perform(put("/api/profile")
                .header("X-User-Key", userKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String body(String nickname, String locale, String start, String end, String interests) {
        return """
                {"nickname":"%s","locale":"%s","birthDate":"2003-04-12",
                 "stayStartDate":"%s","stayEndDate":"%s","interests":%s}
                """.formatted(nickname, locale, start, end, interests);
    }

    private String newUserKey() {
        return UUID.randomUUID().toString();
    }
}
