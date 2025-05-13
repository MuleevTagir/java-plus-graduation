package ru.practicum.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.dto.StatsResponseDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.Response;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    final StatsRepository statsRepository;

    @Transactional
    public StatsRequestDto save(StatsRequestDto requestDto) {
        log.info("Save request to {}", requestDto);
        try {
            var savedRequest = statsRepository.save(Mapper.toRequest(requestDto));
            return Mapper.toRequestDto(savedRequest);
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public List<StatsResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Время окончания позже начала");
        }

        List<Response> statistic;
        if ((uris == null) || (uris.isEmpty())) {
            if (unique) {
                statistic = statsRepository.findAllUnique(start, end);
            } else {
                statistic = statsRepository.findAll(start, end);
            }
        } else {
            if (unique) {
                statistic = statsRepository.findUrisUnique(start, end, uris);
            } else {
                statistic = statsRepository.findUris(start, end, uris);
            }
        }
        return statistic.stream()
                .map(Mapper::toResponseDto)
                .collect(Collectors.toList());
    }
}