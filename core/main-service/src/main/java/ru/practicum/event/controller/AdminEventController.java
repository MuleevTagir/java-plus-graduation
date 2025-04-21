package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventAdminParams;
import ru.practicum.event.dto.EventLongDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.formatter.Formatter;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/events")
@Slf4j
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventLongDto> getAllEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String rangeEnd,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "10") Integer size) {

        int page = from / size;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        LocalDateTime start = (rangeStart != null) ? LocalDateTime.parse(rangeStart, Formatter.getFormatter())
                : LocalDateTime.now();
        LocalDateTime end = (rangeEnd != null) ? LocalDateTime.parse(rangeEnd, Formatter.getFormatter())
                : LocalDateTime.now().plusYears(20);

        EventAdminParams eventAdminParams = EventAdminParams.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(start)
                .rangeEnd(end)
                .from(from)
                .size(size)
                .pageable(PageRequest.of(page, size, sort))
                .build();
        log.info("Calling the GET request to /admin/events endpoint");
        return eventService.getAllEventsByAdmin(eventAdminParams);
    }

    @PatchMapping(value = "/{eventId}")
    public EventLongDto updateEventByAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Calling the PATCH request to /admin/events/{} endpoint", eventId);
        return eventService.updateEventByAdmin(eventId,
                updateEventAdminRequest);
    }
}