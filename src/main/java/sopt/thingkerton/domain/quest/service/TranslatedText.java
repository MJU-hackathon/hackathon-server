package sopt.thingkerton.domain.quest.service;

import java.util.List;

public record TranslatedText(
        String title,
        String reason,
        String description,
        List<String> steps
) {
}
