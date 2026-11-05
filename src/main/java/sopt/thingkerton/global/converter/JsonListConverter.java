package sopt.thingkerton.global.converter;

import jakarta.persistence.AttributeConverter;
import java.util.List;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * 문자열 목록을 JSON 배열 문자열로 저장한다. (예: 퀘스트 단계, 관심사 스냅샷)
 * <p>
 * DB 컬럼은 text. MySQL JSON 타입 대신 text를 쓰는 이유는 테스트용 H2와 동작을 맞추기 위해서다.
 */
public abstract class JsonListConverter<T> implements AttributeConverter<List<T>, String> {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    protected abstract String toElement(T value);

    protected abstract T fromElement(String value);

    @Override
    public String convertToDatabaseColumn(List<T> attribute) {
        List<String> values = attribute == null ? List.of() : attribute.stream().map(this::toElement).toList();
        return MAPPER.writeValueAsString(values);
    }

    @Override
    public List<T> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        List<String> values = MAPPER.readValue(dbData, new TypeReference<List<String>>() {
        });
        return values.stream().map(this::fromElement).toList();
    }
}
