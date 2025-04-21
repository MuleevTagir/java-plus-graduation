package ru.practicum.service;

import ru.practicum.HitRequestDto;
import ru.practicum.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;


public interface StatService {
    StatsResponseDto save(HitRequestDto body);

    List<StatsResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
