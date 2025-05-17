package ru.practicum.stats.analyzer.test;

import io.grpc.stub.StreamObserver;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.stats.analyzer.model.EventSimilarity;
import ru.practicum.stats.analyzer.model.UserAction;
import ru.practicum.stats.analyzer.repository.SimilarityRepository;
import ru.practicum.stats.analyzer.repository.UserActionRepository;
import ru.practicum.stats.analyzer.service.RecommendationsService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationsServiceTest {
    @Mock
    private UserActionRepository actionRepository;

    @Mock
    private SimilarityRepository similarityRepository;

    @Mock
    private StreamObserver<RecommendedEventProto> responseObserver;

    @InjectMocks
    private RecommendationsService recommendationService; // Замените на ваш класс сервиса

    @Test
    void shouldCalculateValidPredictedScore() {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(1L)
                .setMaxResult(3)
                .build();

        List<UserAction> userActions = List.of(
                new UserAction(1L, 101L, 1.0, Instant.now()),
                new UserAction(1L, 102L, 0.8, Instant.now().minusSeconds(5)),
                new UserAction(1L, 103L, 0.4, Instant.now().minusSeconds(10))
        );

        List<EventSimilarity> similarities = List.of(
                new EventSimilarity(101L, 201L, 0.9),
                new EventSimilarity(102L, 202L, 1.0),
                new EventSimilarity(103L, 203L, 0.5)
        );

        when(actionRepository.findRecentEventIdsByUserId(1L, 3))
                .thenReturn(List.of(101L, 102L, 103L));
        when(actionRepository.findAllInteractionsByUser(1L))
                .thenReturn(userActions);
        when(similarityRepository.findSimilarUnseenEvents(any(), any(), anyInt()))
                .thenReturn(similarities.stream()
                        .map(es -> List.of(es.getFirst(), es.getSecond()))
                        .flatMap(List::stream)
                        .distinct()
                        .map(id -> new EventSimilarity(id, id + 100, 0.5))
                        .collect(Collectors.toList()));
        when(similarityRepository.findTopKSimilarUserEvents(any(), any(), anyInt()))
                .thenReturn(similarities);

        List<RecommendedEventProto> results = new ArrayList<>();
        doAnswer(invocationOnMock -> {
            results.add(invocationOnMock.getArgument(0));
            return null;
        }).when(responseObserver).onNext(any());

        recommendationService.getRecommendationsForUser(request, responseObserver);

        for (RecommendedEventProto proto: results) {
            EventSimilarity similarity = similarities.stream()
                    .filter(e -> e.getFirst() == proto.getEventId() || e.getSecond() == proto.getEventId())
                    .findFirst().get();
            Long neighborId = similarity.getFirst() == proto.getEventId() ?
                    similarity.getSecond() : similarity.getFirst();
            UserAction neighborAction =  userActions.stream()
                    .filter(e -> e.getEventId() == neighborId)
                    .findFirst().get();
            double predicatedScore = (neighborAction.getScore() * similarity.getScore()) / similarity.getScore();

            assertEquals(predicatedScore, proto.getScore());
        }
    }
}
