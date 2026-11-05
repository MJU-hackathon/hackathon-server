package sopt.thingkerton.global.converter;

import jakarta.persistence.Converter;

@Converter
public class StringListConverter extends JsonListConverter<String> {

    @Override
    protected String toElement(String value) {
        return value;
    }

    @Override
    protected String fromElement(String value) {
        return value;
    }
}
