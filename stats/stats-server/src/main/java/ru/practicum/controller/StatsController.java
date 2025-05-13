package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatsInterface;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.dto.StatsResponseDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
public class StatsController implements StatsInterface {

    final StatsService statsService;

    @Autowired
    public StatsController(@Qualifier("statsServiceImpl") StatsService statsService) {
        this.statsService = statsService;
    }

    @Override
    public List<StatsResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Получен запрос сбора статистики StartDate: {}, EndDate: {}, Uris: {}, Unique: {}", start, end, uris, unique);
        return statsService.getStats(start, end, uris, unique);
    }

    @Override
    public StatsRequestDto save(StatsRequestDto statsRequestDto) {
        log.info("Получен запрос на добавление статистики: StatsRequestDto: {}", statsRequestDto);
        return statsService.save(statsRequestDto);
    }
}