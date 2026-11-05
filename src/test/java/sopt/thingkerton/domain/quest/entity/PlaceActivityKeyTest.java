package sopt.thingkerton.domain.quest.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PlaceActivityKeyTest {

    @Test
    void 표현이_달라도_같은_키로_정규화한다() {
        assertThat(new PlaceActivityKey(" Yeongcheon  Market ", "Order_Market-Food!"))
                .isEqualTo(new PlaceActivityKey("yeongcheon-market", "order-market-food"));
    }

    @Test
    void 앞뒤_하이픈과_연속_하이픈을_정리한다() {
        assertThat(PlaceActivityKey.normalize("--독립문 -- 공원--")).isEqualTo("독립문-공원");
        assertThat(PlaceActivityKey.normalize(null)).isEmpty();
    }
}
