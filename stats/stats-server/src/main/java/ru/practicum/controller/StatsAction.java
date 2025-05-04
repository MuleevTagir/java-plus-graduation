package ru.practicum.controller;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitRequestDto;
import ru.practicum.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.controller.JsonFormatPattern.JSON_FORMAT_PATTERN_FOR_TIME;

@FeignClient(name = "stats-server")
public interface StatsAction {

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<StatsResponseDto> getStats(@DateTimeFormat(pattern = JSON_FORMAT_PATTERN_FOR_TIME) @RequestParam(value = "start") LocalDateTime start,
                                           @DateTimeFormat(pattern = JSON_FORMAT_PATTERN_FOR_TIME) @RequestParam(value = "end") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(required = false, defaultValue = "false") Boolean unique);

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatsResponseDto hit(@RequestBody @Valid HitRequestDto statsRequestDto);
}
