package ru.practicum.application.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.application.api.dto.event.EventFullDto;
import ru.practicum.application.api.dto.event.EventShortDto;
import ru.practicum.application.api.exception.NotFoundException;
import ru.practicum.application.api.exception.ValidationException;
import ru.practicum.application.event.service.EventService;
import ru.practicum.application.event.api.EventInterface;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventController implements EventInterface {

    final EventService eventService;

    @Override
    public EventFullDto getEventById(Long id,
                                     HttpServletRequest request) throws NotFoundException {
        return eventService.getEventById(id, request.getRequestURI(), request.getRemoteAddr());
    }

    @Override
    public List<EventShortDto> getFilteredEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd,
            Boolean available,
            String sort,
            Integer from,
            Integer count,
            HttpServletRequest request
    ) throws ValidationException {
        return eventService.getFilteredEvents(text, categories, paid, rangeStart, rangeEnd, available, sort, from, count,
                request.getRequestURI(), request.getRemoteAddr());
    }
}