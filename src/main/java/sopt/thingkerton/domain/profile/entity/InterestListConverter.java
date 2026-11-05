package sopt.thingkerton.domain.profile.entity;

import jakarta.persistence.Converter;
import sopt.thingkerton.global.converter.JsonListConverter;

@Converter
public class InterestListConverter extends JsonListConverter<Interest> {

    @Override
    protected String toElement(Interest value) {
        return value.name();
    }

    @Override
    protected Interest fromElement(String value) {
        return Interest.valueOf(value);
    }
}
