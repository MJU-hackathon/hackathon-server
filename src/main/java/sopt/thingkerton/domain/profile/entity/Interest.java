package sopt.thingkerton.domain.profile.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관심사. 퀘스트 카테고리와 같은 값을 쓴다.
 */
@Schema(description = "관심사·퀘스트 카테고리. 앱에서 언어별 라벨로 표시 (KPOP=KPOP, DRAMA=드라마, MOVIE=영화, BEAUTY=뷰티, "
        + "FOOD=음식, WEBTOON=웹툰, GAME=게임, FASHION=패션, TOURISM=관광)")
public enum Interest {
    KPOP, DRAMA, MOVIE, BEAUTY, FOOD, WEBTOON, GAME, FASHION, TOURISM
}
