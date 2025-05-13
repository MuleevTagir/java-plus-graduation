package ru.practicum.client;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.dto.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.client.util.JsonFormatPattern.JSON_FORMAT_PATTERN_FOR_TIME;

@RestController
public interface StatsInterface {
    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<StatsResponseDto> getStats(@DateTimeFormat(pattern = JSON_FORMAT_PATTERN_FOR_TIME) @RequestParam(value = "start") LocalDateTime start,
                                           @DateTimeFormat(pattern = JSON_FORMAT_PATTERN_FOR_TIME) @RequestParam(value = "end") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(required = false, defaultValue = "false") Boolean unique);

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatsRequestDto save(@RequestBody @Valid StatsRequestDto statsRequestDto);
}
