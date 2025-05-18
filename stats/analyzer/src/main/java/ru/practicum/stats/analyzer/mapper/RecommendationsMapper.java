package ru.practicum.stats.analyzer.mapper;

import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.stats.analyzer.model.EventSimilarity;

public class RecommendationsMapper {
    public static RecommendedEventProto mapSimilarityToRecommendation(Long eventId, Double score) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(score)
                .build();
    }
}
