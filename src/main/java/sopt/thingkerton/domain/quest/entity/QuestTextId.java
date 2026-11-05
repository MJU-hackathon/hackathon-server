package sopt.thingkerton.domain.quest.entity;

import java.io.Serializable;

public record QuestTextId(Long questId, String locale) implements Serializable {
}
