package ru.practicum.stats.aggregator.handler;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.exception.IncorrectActionTypeException;
import ru.practicum.stats.aggregator.kafka.SimilarityProducer;

import java.time.Instant;
import java.util.*;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActionHandler {
    final SimilarityProducer producer;
    final Map<Long, Map<Long, Double>> usersFeedback;
    final Map<EventPair, Double> eventsMinWeightSum;
    final Map<Long, Double> eventWeightSum;
    final Map<EventPair, Double> eventsSimilarity;
    final Map<Long, Double> sqrtCache;


    @Autowired
    public UserActionHandler(SimilarityProducer producer) {
        this.producer = producer;
        usersFeedback = new HashMap<>();
        eventsMinWeightSum = new HashMap<>();
        eventWeightSum = new HashMap<>();
        eventsSimilarity = new HashMap<>();
        sqrtCache = new HashMap<>();
    }

    public void handle(UserActionAvro avro) throws IncorrectActionTypeException {
        Long userId = avro.getUserId();
        Long eventId = avro.getEventId();
        Double weight = convertActionToWeight(avro.getActionType());

        Map<Long, Double> userRatings = usersFeedback.computeIfAbsent(eventId, k -> new HashMap<>());
        Double oldWeight = userRatings.getOrDefault(userId, 0.0);

        if (oldWeight < weight) {
            userRatings.put(userId, weight);
            determineSimilarity(eventId, userId, oldWeight, weight, avro.getTimestamp());
        }
    }

    public void flush() {
        producer.flush();
    }

    public void close() {
        producer.close();
    }

    private void determineSimilarity(Long eventId, Long userId, Double oldWeight, Double newWeight, Instant timestamp) {
        double newSum = eventWeightSum.getOrDefault(eventId, 0.0) - oldWeight + newWeight;
        eventWeightSum.put(eventId, newSum);
        sqrtCache.remove(eventId);

        for (Map.Entry<Long, Map<Long, Double>> entry : usersFeedback.entrySet()) {
            Long otherEventId = entry.getKey();
            Map<Long, Double> feedback = entry.getValue();
            if (!feedback.containsKey(userId) || Objects.equals(otherEventId, eventId)) continue;

            double convergenceWeight = feedback.get(userId);
            EventPair pair = EventPair.of(eventId, otherEventId);

            double oldMinSum = eventsMinWeightSum.getOrDefault(pair, 0.0);
            double newMinSum = oldMinSum - Math.min(oldWeight, convergenceWeight) + Math.min(newWeight, convergenceWeight);
            eventsMinWeightSum.put(pair, newMinSum);

            double similarity = calculateSimilarity(pair, newMinSum);

            eventsSimilarity.put(pair, similarity);

            EventSimilarityAvro message = EventSimilarityAvro.newBuilder()
                    .setEventA(pair.first())
                    .setEventB(pair.second())
                    .setScore(similarity)
                    .setTimestamp(timestamp)
                    .build();
            producer.sendMessage(message);
        }
    }

    private double calculateSimilarity(EventPair pair, double commonSum) {
        double sqrtA = getSqrtSum(pair.first());
        double sqrtB = getSqrtSum(pair.second());

        double similarity = commonSum / (sqrtA * sqrtB);
        log.info("Определено сходство событий {} и {}: {}", pair.first(), pair.second(), similarity);
        return similarity;
    }

    private double getSqrtSum(Long eventId) {
        return sqrtCache.computeIfAbsent(eventId, id -> Math.sqrt(eventWeightSum.getOrDefault(id, 0.0)));
    }

    private Double convertActionToWeight(ActionTypeAvro action) throws IncorrectActionTypeException {
        switch (action) {
            case VIEW -> {
                return  0.4;
            }
            case REGISTER -> {
                return 0.8;
            }
            case LIKE -> {
                return 1.0;
            }
            default -> {
                log.warn("Неверный тип действия пользователя: {}", action);
                throw new IncorrectActionTypeException("Неверный тип действия пользователя: " + action);
            }
        }
    }

    record EventPair(Long first, Long second) {
        public static EventPair of(Long a, Long b) {
            return a < b ? new EventPair(a, b) : new EventPair(b, a);
        }
    }
}
