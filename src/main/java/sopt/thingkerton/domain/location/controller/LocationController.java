package sopt.thingkerton.domain.location.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sopt.thingkerton.domain.location.code.LocationSuccessCode;
import sopt.thingkerton.domain.location.controller.dto.LocationResponse;
import sopt.thingkerton.domain.location.service.LocationService;
import sopt.thingkerton.global.dto.CommonResponse;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController implements LocationApi {
    private final LocationService locationService;

    @GetMapping("/geocode")
    @Override
    public CommonResponse<LocationResponse> geocode(@RequestParam String address) {
        return CommonResponse.success(LocationSuccessCode.LOCATION_SEARCH_SUCCESS,
                LocationResponse.from(address, locationService.geocode(address)));
    }
}
