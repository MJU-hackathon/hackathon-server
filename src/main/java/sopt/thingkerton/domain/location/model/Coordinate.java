package sopt.thingkerton.domain.location.model;

import sopt.thingkerton.domain.location.code.LocationErrorCode;
import sopt.thingkerton.global.exception.BusinessException;

public record Coordinate(
        double latitude,
        double longitude
) {
    public Coordinate {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new BusinessException(LocationErrorCode.INVALID_COORDINATE);
        }
    }
}
