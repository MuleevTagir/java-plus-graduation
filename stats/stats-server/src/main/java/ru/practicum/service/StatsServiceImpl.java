package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitRequestDto;
import ru.practicum.StatsResponseDto;
import ru.practicum.errors.exceptions.InvalidRequestException;
import ru.practicum.mappers.StatsMapper;
import ru.practicum.model.Hit;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatsServiceImpl implements StatService {
    private final StatRepository repository;
    private final StatsMapper mapper;

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);


    @Override
    public StatsResponseDto save(HitRequestDto body) {
        LocalDateTime timestamp = getDateTime(body.getTimestamp());
        Hit hit = new Hit();
        hit.setApp(body.getApp());
        hit.setUri(body.getUri());
        hit.setTimestamp(timestamp);
        hit.setIp(body.getIp());

        return mapper.toDto(repository.save(hit));
    }

    @Override
    public List<StatsResponseDto> getStats(LocalDateTime start,
                                           LocalDateTime end,
                                           List<String> uris,
                                           boolean unique) {
        List<StatsResponseDto> hits;
        if (start.isAfter(end)) {
            throw new InvalidRequestException("Start is after end");
        }
        log.info("Uris = {}", uris);
        if (uris != null && !uris.isEmpty()) {
            if (unique) {
                hits = repository.getStatsByUriWithUniqueIps(uris, start, end);
            } else {
                hits = repository.getStatsByUri(uris, start, end);
            }
        } else {
            if (unique) {
                hits = repository.getStatsByTimeWithUniqueIps(start, end);
            } else {
                hits = repository.findAllTimestampBetweenStartAndEnd(start, end);
            }
        }

        return hits;
    }

    private LocalDateTime getDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(date, formatter);
    }

}
