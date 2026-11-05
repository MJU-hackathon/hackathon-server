package sopt.thingkerton.domain.quest.service;

import java.util.List;
import org.springframework.stereotype.Component;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;

/**
 * AI 생성 실패 시 대신 내려주는 데모 퀘스트 3개.
 * <p>
 * TODO: 팀이 전달하는 데모 퀘스트로 교체. 지금은 서대문구 임시 값이며 한국어로만 작성돼 있다
 * (다른 언어 요청 시 조회 시점에 번역된다).
 */
@Component
public class DemoQuestProvider {

    public static final AppLocale DEMO_LOCALE = AppLocale.KO;

    private static final List<GeneratedQuest> DEMO_QUESTS = List.of(
            new GeneratedQuest(
                    new PlaceActivityKey("yeongcheon-market", "order-market-food"),
                    Interest.FOOD, 40, "영천시장",
                    "시장에서 만나는 첫 번째 한입",
                    "가까운 전통시장에서 한국 음식을 경험해 보세요",
                    "서대문 영천시장에서 한국 시장 음식을 직접 골라 주문해 보는 퀘스트예요.",
                    List.of("시장에서 먹어보고 싶은 음식 찾기", "한국어로 주문해 보기", "음식이나 시장 풍경 사진 찍기")),
            new GeneratedQuest(
                    new PlaceActivityKey("independence-gate", "visit-landmark"),
                    Interest.TOURISM, 30, "독립문",
                    "독립문 앞, 서울의 한 장면",
                    "동네의 대표 명소를 둘러보세요",
                    "서대문독립공원의 독립문을 둘러보며 서울의 역사를 느껴 보는 퀘스트예요.",
                    List.of("독립문까지 걸어가기", "안내판에서 독립문 이야기 읽어 보기", "독립문과 함께 사진 찍기")),
            new GeneratedQuest(
                    new PlaceActivityKey("ansan-jarak-gil", "walk-forest-trail"),
                    Interest.TOURISM, 60, "안산자락길",
                    "숲길 따라 걷는 서대문",
                    "도심 가까이에서 산책하기 좋은 길이에요",
                    "안산자락길 무장애 숲길을 걸으며 서울 도심 풍경을 내려다보는 퀘스트예요.",
                    List.of("자락길 입구 찾기", "전망 데크까지 걸어 보기", "숲길이나 전망 사진 찍기"))
    );

    public List<GeneratedQuest> getDemoQuests() {
        return DEMO_QUESTS;
    }
}
