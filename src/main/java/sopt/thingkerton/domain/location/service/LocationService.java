package sopt.thingkerton.domain.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sopt.thingkerton.domain.location.code.LocationErrorCode;
import sopt.thingkerton.domain.location.model.Coordinate;
import sopt.thingkerton.domain.location.service.port.GeocodingPort;
import sopt.thingkerton.global.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final GeocodingPort geocodingPort;

    public Coordinate geocode(String address) {
        if (address == null || address.isBlank()) {
            throw new BusinessException(LocationErrorCode.INVALID_ADDRESS);
        }
        return geocodingPort.geocode(address);
    }
}
