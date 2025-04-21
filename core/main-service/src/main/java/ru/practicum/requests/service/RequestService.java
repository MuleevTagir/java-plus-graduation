package ru.practicum.requests.service;

import ru.practicum.errors.exceptions.DataConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.errors.exceptions.ValidationException;
import ru.practicum.requests.dto.RequestDto;

import java.util.List;

public interface RequestService {
    RequestDto addRequest(Long userId, Long eventId) throws DataConflictException, NotFoundException;

    List<RequestDto> getUserRequests(Long userId) throws NotFoundException;

    List<RequestDto> getRequestsByEventId(Long userId, Long eventId) throws ValidationException, NotFoundException;

    RequestDto updateRequest(Long userId,
                             Long eventId,
                             RequestDto request) throws DataConflictException, ValidationException, NotFoundException;

    RequestDto cancelRequest(Long userId, Long requestId) throws NotFoundException, ValidationException;
}