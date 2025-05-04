package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventPublicParams;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.formatter.Formatter;
import ru.practicum.stat.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/events")
@Slf4j
public class PublicEventController {

    private final EventService eventService;
    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getAllEvents(
            @RequestParam(defaultValue = "") String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
            @Positive @RequestParam(required = false, defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        LocalDateTime start = (rangeStart != null) ? LocalDateTime.parse(rangeStart, Formatter.getFormatter())
                : LocalDateTime.now();
        LocalDateTime end = (rangeEnd != null) ? LocalDateTime.parse(rangeEnd, Formatter.getFormatter())
                : LocalDateTime.now().plusYears(20);

        EventPublicParams eventPublicParams = EventPublicParams.builder()
                .state(EventState.PUBLISHED)
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(start)
                .rangeEnd(end)
                .onlyAvailable(onlyAvailable)
                .from(from)
                .size(size)
                .sort(sort)
                .build();
        statsService.createStats(request.getRequestURI(), request.getRemoteAddr());
        List<EventShortDto> events = eventService.getPublicEvents(eventPublicParams);
        log.info("Calling the GET request to /events endpoint");
        return ResponseEntity.status(HttpStatus.OK)
                .body(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEventDtoById(@PathVariable Long id,
                                                        HttpServletRequest httpServletRequest) {
        log.info("Calling the GET request to /events/{} endpoint", id);
        statsService.createStats(httpServletRequest.getRequestURI(), httpServletRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.OK).body(eventService.getEventDtoByIdWithHit(id, httpServletRequest));
    }
}
