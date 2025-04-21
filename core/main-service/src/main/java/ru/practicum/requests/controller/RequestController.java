package ru.practicum.requests.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.errors.exceptions.DataConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.errors.exceptions.ValidationException;
import ru.practicum.requests.dto.RequestDto;
import ru.practicum.requests.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class RequestController {

    final RequestService requestService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto addEventRequest(@PathVariable Long userId,
                                      @RequestParam Long eventId) throws DataConflictException, NotFoundException {
        return requestService.addRequest(userId, eventId);
    }

    @GetMapping("/requests")
    public List<RequestDto> getUserRequests(@PathVariable Long userId) throws NotFoundException {
        return requestService.getUserRequests(userId);
    }


    @GetMapping("/events/{eventId}/requests")
    public List<RequestDto> getRequestsByEventId(@PathVariable Long userId,
                                                 @PathVariable Long eventId) throws ValidationException, NotFoundException {
        return requestService.getRequestsByEventId(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public RequestDto updateRequest(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody RequestDto request) throws ValidationException, DataConflictException, NotFoundException {
        return requestService.updateRequest(userId, eventId, request);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable Long userId,
                                    @PathVariable Long requestId) throws ValidationException, NotFoundException {
        return requestService.cancelRequest(userId, requestId);
    }
}