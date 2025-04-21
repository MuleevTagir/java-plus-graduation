package ru.practicum.stat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.HitRequestDto;
import ru.practicum.StatsResponseDto;
import ru.practicum.client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private static final String APP_NAME = "ewm-service";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private final StatsClient statsClient;

    @Override
    public void createStats(String uri, String ip) {
        log.info("Creating stats for URI: {}, IP: {}", uri, ip);

        HitRequestDto stats = HitRequestDto.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(formatter))
                .build();

        try {
            Object result = statsClient.hit(stats);
            log.info("Stats created successfully. Result: {}", result);
        } catch (Exception e) {
            log.error("Error creating stats: ", e);
        }
    }

    @Override
    public List<StatsResponseDto> getStats(List<Long> eventsId, boolean unique) {
        log.info("Getting stats for events: {}, unique: {}", eventsId, unique);

        LocalDateTime start = LocalDateTime.now().minusYears(20);
        LocalDateTime end = LocalDateTime.now().plusYears(20);


        List<String> uris = eventsId.stream()
                .map(id -> String.format("/events/%d", id))
                .toList();

        try {
            List<StatsResponseDto> stats = statsClient.getStats(start, end, uris, unique);
            log.info("Retrieved {} stats entries", stats.size());
            return stats;
        } catch (Exception e) {
            log.error("Error getting stats: ", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<Long, Long> getView(List<Long> eventsId, boolean unique) {
        log.info("Getting views for events: {}, unique: {}", eventsId, unique);

        Map<Long, Long> views = new HashMap<>();

        List<StatsResponseDto> stats = getStats(eventsId, unique);

        for (StatsResponseDto stat : stats) {
            String uriPath = stat.getUri();
            if (uriPath.startsWith("/events/")) {
                try {
                    Long id = Long.valueOf(uriPath.substring("/events/".length()));
                    Long hits = stat.getHits();
                    views.put(id, hits);
                } catch (NumberFormatException e) {
                    log.warn("Invalid event ID in URI: {}", uriPath);
                }
            }
        }

        log.info("Processed views: {}", views);
        return views;
    }
}