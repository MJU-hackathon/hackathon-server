package sopt.thingkerton.domain.location.service.port;

import sopt.thingkerton.domain.location.model.Coordinate;

public interface GeocodingPort {
    Coordinate geocode(String address);
}
