package sopt.thingkerton.support;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import sopt.thingkerton.domain.location.code.LocationErrorCode;
import sopt.thingkerton.domain.location.model.Coordinate;
import sopt.thingkerton.domain.location.service.port.GeocodingPort;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * 가짜 네이버 Geocoding. 주소에 "없는"이 들어 있으면 ADDRESS_NOT_FOUND, 그 외에는 고정 좌표를 돌려준다.
 */
public class FakeGeocodingPort implements GeocodingPort {

    public static final Coordinate COORDINATE = new Coordinate(37.571234, 126.961234);

    private final List<String> addresses = new CopyOnWriteArrayList<>();
    private volatile long delayMillis;

    @Override
    public Coordinate geocode(String address) {
        addresses.add(address);
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        if (address.contains("없는")) {
            throw new BusinessException(LocationErrorCode.ADDRESS_NOT_FOUND);
        }
        return COORDINATE;
    }

    public List<String> addresses() {
        return addresses;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void reset() {
        addresses.clear();
        delayMillis = 0;
    }
}
