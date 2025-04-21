package ru.practicum.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.errors.exceptions.DataConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.errors.exceptions.ValidationException;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.requests.dto.RequestDto;
import ru.practicum.requests.mapper.RequestMapper;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.requests.enums.RequestStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    final EventRepository eventRepository;
    final RequestMapper requestMapper;
    final UserRepository userRepository;
    final RequestRepository requestRepository;

    @Override
    @Transactional
    public RequestDto addRequest(Long userId, Long eventId) throws DataConflictException, NotFoundException {
        User user = userRepository.getUserById(userId);
        Event event = getEventById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException("Создатель события не может подать заявку на участие");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("Событие не опубликовано");
        }
        List<Request> requests = getRequestsByEventId(event.getId());
        if (participationLimitIsFull(event)) {
            throw new DataConflictException("Превышен лимит заявок на участие в событии");
        }
        for (Request request : requests) {
            if (request.getRequester().getId().equals(userId)) {
                throw new DataConflictException("Повторная заявка на участие в событии");
            }
        }

        Request newRequest = createNewRequest(user, event);
        return requestMapper.mapRequest(requestRepository.save(newRequest));
    }

    @Override
    public List<RequestDto> getUserRequests(Long userId) throws NotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден userId=" + userId);
        }
        return requestRepository.findByUserId(userId).stream()
                .map(requestMapper::mapRequest)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getRequestsByEventId(Long userId, Long eventId) throws ValidationException, NotFoundException {
        List<Request> requests = getRequests(userId, eventId);
        return requests.stream()
                .map(requestMapper::mapRequest)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RequestDto updateRequest(Long userId,
                                    Long eventId,
                                    RequestDto updateRequest) throws DataConflictException, ValidationException, NotFoundException {
        Event event = getEventById(eventId);
        List<Request> requests = getRequestsByEventId(eventId);
        long confirmedRequestsCounter = requests.stream().filter(r -> r.getStatus().equals(CONFIRMED_REQUEST)).count();

        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();

        List<Request> result = new ArrayList<>();

        List<Request> pending = requests.stream()
                .filter(p -> p.getStatus().equals(PENDING_REQUEST)).collect(Collectors.toList());


        for (Request request : requests) {
            if (request.getStatus().equals(CONFIRMED_REQUEST) ||
                    request.getStatus().equals(REJECTED_REQUEST) ||
                    request.getStatus().equals(PENDING_REQUEST)) {

                if (updateRequest.getStatus().equals(CONFIRMED_REQUEST) && event.getParticipantLimit() != 0) {
                    if (event.getParticipantLimit() < confirmedRequestsCounter) {

                        pending.stream().peek(p -> p.setStatus(REJECTED_REQUEST)).collect(Collectors.toList());

                        throw new DataConflictException("Превышено число возможных заявок на участие");
                    }
                }

                if (updateRequest.getStatus().equals(REJECTED_REQUEST) && request.getStatus().equals(CONFIRMED_REQUEST)) {
                    throw new DataConflictException("Нельзя отменить подтверждённую заявку");
                }

                request.setStatus(updateRequest.getStatus());
                RequestDto participationRequestDto = requestMapper.mapRequest(request);

                if ("CONFIRMED".equals(participationRequestDto.getStatus())) {
                    confirmedRequests.add(participationRequestDto);
                } else if ("REJECTED".equals(participationRequestDto.getStatus())) {
                    rejectedRequests.add(participationRequestDto);
                }

                result.add(request);
                confirmedRequestsCounter++;

            } else {
                throw new ValidationException("Неверный статус заявки");
            }
        }

        event.setConfirmedRequests(confirmedRequestsCounter);
        eventRepository.save(event);
        requestRepository.saveAll(pending);

        requestRepository.saveAll(result);

        return requestMapper.mapRequestWithConfirmedAndRejected(confirmedRequests, rejectedRequests);
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) throws NotFoundException, ValidationException {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден userId=" + userId);
        }

        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запрос не существует")
        );
        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidationException("Создатель заявки не userId=" + userId);
        }
        request.setStatus(CANCELED_REQUEST);
        return requestMapper.mapRequest(requestRepository.save(request));
    }

    private Request createNewRequest(User user, Event event) {
        Request newRequest = new Request();
        newRequest.setRequester(user);
        newRequest.setCreated(LocalDateTime.now());
        if (event.getParticipantLimit() == 0) {
            newRequest.setStatus(CONFIRMED_REQUEST);
        } else {
            newRequest.setStatus(PENDING_REQUEST);
        }
        newRequest.setEvent(event);
        if (!event.getRequestModeration()) {
            newRequest.setStatus(ACCEPTED_REQUEST);
        }
        return newRequest;
    }

    private boolean participationLimitIsFull(Event event) throws DataConflictException {
        Long confirmedRequestsCounter = requestRepository.countByEventAndStatuses(event.getId(), List.of("CONFIRMED", "ACCEPTED"));
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequestsCounter) {
            throw new DataConflictException("Превышено число заявок на участие");
        }
        return false;
    }

    private List<Request> getRequests(Long userId, Long eventId) throws ValidationException, NotFoundException {
        User user = userRepository.getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("Пользователь не инициатор события c id=" + eventId);
        }
        return requestRepository.findByEventInitiatorId(userId);
    }

    private List<Request> getRequestsByEventId(Long eventId) throws NotFoundException {
        if (eventRepository.existsById(eventId)) {
            return requestRepository.findByEventId(eventId);
        } else {
            throw new NotFoundException("Событие не найдено eventId=" + eventId);
        }
    }

    private Event getEventById(Long eventId) throws NotFoundException {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не найдено eventId=" + eventId));
    }
}