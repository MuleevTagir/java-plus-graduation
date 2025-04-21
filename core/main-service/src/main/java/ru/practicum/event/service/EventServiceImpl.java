package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.service.CategoryService;
import ru.practicum.errors.exceptions.DataConflictException;
import ru.practicum.errors.exceptions.InvalidRequestException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.enums.EventSort;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.model.enums.StateActionForAdmin;
import ru.practicum.event.model.enums.StateActionForUser;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.stat.service.StatsService;
import ru.practicum.users.model.User;
import ru.practicum.users.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@NoArgsConstructor(force = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventMapper eventMapper;
    private final StatsService statsService;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserService userService,
                            CategoryService categoryService, EventMapper eventMapper, StatsService statsService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.eventMapper = eventMapper;
        this.statsService = statsService;
    }

    public List<EventShortDto> getAllEventOfUser(Long userId, Integer from, Integer size) {
        List<EventShortDto> eventsOfUser;
        userService.findUserById(userId);
        List<Event> events = eventRepository.findEventsOfUser(userId, PageRequest.of(from / size, size));
        eventsOfUser = events.stream().map(eventMapper::toEventShortDto).collect(Collectors.toList());
        log.info("Получение всех событий пользователя с ID = {}", userId);
        return eventsOfUser;
    }

    @Transactional
    public EventLongDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userService.findUserById(userId);
        Category category = categoryService.getCategoryByIdNotMapping(newEventDto.getCategory());
        Event event = eventMapper.toEvent(newEventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new InvalidRequestException("The date and time for which the event is scheduled cannot be earlier " +
                    "than two hours from the current moment");
        }
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setPublishedOn(LocalDateTime.now());
        event = eventRepository.save(event);
        EventLongDto eventFullDto = eventMapper.toLongDto(event);
        log.info("Событию присвоен ID = {}, и оно успешно добавлено", event.getId());
        return eventFullDto;
    }

    public EventFullDto getEventOfUserById(Long userId, Long eventId) {
        userService.findUserById(userId);
        Optional<Event> optEventSaved = eventRepository.findByIdAndInitiatorId(eventId, userId);
        EventFullDto eventFullDto;
        if (optEventSaved.isPresent()) {
            eventFullDto = eventMapper.toEventFullDto(optEventSaved.get());
        } else {
            throw new NotFoundException("The required object was not found.");
        }

        Map<Long, Long> views = statsService.getView(List.of(eventFullDto).stream()
                .map(EventFullDto::getId)
                .collect(Collectors.toList()), true);

        eventFullDto.setViews(views.get(eventFullDto.getId()));


        log.info("Выполнен поиск события с ID = {}", eventId);
        return eventFullDto;
    }

    @Transactional
    public EventLongDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        userService.findUserById(userId);
        Optional<Event> optEventSaved = eventRepository.findByIdAndInitiatorId(eventId, userId);
        Event eventSaved;
        if (optEventSaved.isPresent()) {
            eventSaved = optEventSaved.get();
        } else {
            throw new NotFoundException("Event with ID = " + eventId + " was not found");
        }

        if (eventSaved.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("It is not possible to make changes to an already published event.");
        }

        if (updateEvent.getEventDate() != null) {
            if (LocalDateTime.parse(updateEvent.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .isBefore(LocalDateTime.now().plusHours(2))) {
                throw new InvalidRequestException("The start date of the event to be modified must be no earlier " +
                        "than two hours from the date of publication.");
            } else {
                eventSaved.setEventDate(LocalDateTime.parse(updateEvent.getEventDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        }

        if (updateEvent.getStateAction() != null) {
            updateStateOfEventByUser(updateEvent.getStateAction(), eventSaved);
        }

        if (updateEvent.getAnnotation() != null) {
            eventSaved.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryService.getCategoryByIdNotMapping(updateEvent.getCategory());
            eventSaved.setCategory(category);
        }
        checkParams(eventSaved, updateEvent.getDescription(), updateEvent.getLocation(),
                updateEvent.getParticipantLimit(), eventSaved.getParticipantLimit(),
                updateEvent.getPaid(), updateEvent.getRequestModeration(),
                updateEvent.getTitle());

        Event eventUpdate = eventRepository.save(eventSaved);

        EventLongDto eventFullDto = eventMapper.toLongDto(eventUpdate);
        log.info("Событие ID = {} пользователя ID = {} успешно обновлено", eventId, userId);
        return eventFullDto;
    }

    @Override
    public List<ParticipationRequestDto> getRequestEventByUser(Long userId, Long eventId) {
        return List.of();
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestEventStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        return null;
    }

    private void checkParams(Event eventSaved, String description, Location location, Integer participantLimit,
                             Integer participantLimit2, Boolean paid, Boolean requestModeration, String title
    ) {
        if (description != null) {
            eventSaved.setDescription(description);
        }
        if (location != null) {
            eventSaved.setLat(location.getLat());
            eventSaved.setLon(location.getLon());
        }
        if (participantLimit != null) {
            eventSaved.setParticipantLimit(participantLimit2);
        }
        if (paid != null) {
            eventSaved.setPaid(paid);
        }
        if (requestModeration != null) {
            eventSaved.setRequestModeration(requestModeration);
        }
        if (title != null) {
            eventSaved.setTitle(title);
        }
    }

    @Override
    public List<EventLongDto> getAllEventsByAdmin(EventAdminParams param) {
        log.info("Запрос от администратора на получение событий");

        List<Event> events = eventRepository.searchEventsForAdmin(param);
        Map<Long, Long> view = getView(events, false);
        return events.stream()
                .map(e -> eventMapper.toLongDto(e, view.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventLongDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        Optional<Event> optEventSaved = eventRepository.findById(eventId);
        Event eventSaved;
        if (optEventSaved.isPresent()) {
            eventSaved = optEventSaved.get();
        } else {
            throw new NotFoundException("Event with ID = " + eventId + " was not found");
        }
        if (updateEvent.getEventDate() != null) {
            updateEventData(updateEvent.getEventDate(), eventSaved);
        }
        if (updateEvent.getStateAction() != null) {
            updateStateOfEventByAdmin(updateEvent.getStateAction(), eventSaved);
        }
        if (updateEvent.getAnnotation() != null) {
            eventSaved.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryService.getCategoryByIdNotMapping(updateEvent.getCategory());
            eventSaved.setCategory(category);
        }
        checkParams(eventSaved, updateEvent.getDescription(), updateEvent.getLocation(), updateEvent.getParticipantLimit(), updateEvent.getParticipantLimit(), updateEvent.getPaid(), updateEvent.getRequestModeration(), updateEvent.getTitle());

        eventSaved = eventRepository.save(eventSaved);

        EventLongDto eventFullDto = eventMapper.toLongDto(eventSaved);

        log.info("Событие ID = {} успешно обновлено от имени администратора", eventId);
        return eventFullDto;
    }


    @Override
    public List<EventShortDto> getPublicEvents(EventPublicParams param) {
        log.info("Запрос получить опубликованные события");

        if (param.getRangeStart().isAfter(param.getRangeEnd())) {
            log.error("NotValid. При поиске опубликованных событий rangeStart после rangeEnd.");
            throw new InvalidRequestException("The start of the range must be before the end of the range.");
        }

        List<Event> events = eventRepository.searchPublicEvents(param);

        Comparator<EventShortDto> comparator = Comparator.comparing(EventShortDto::getId);

        if (param.getSort() != null) {
            if (param.getSort().equals(EventSort.EVENT_DATE.name())) {
                comparator = Comparator.comparing(EventShortDto::getEventDate);
            } else if (param.getSort().equals(EventSort.VIEWS.name())) {
                comparator = Comparator.comparing(EventShortDto::getViews, Comparator.reverseOrder());
            }
        }

        Map<Long, Long> view = getView(events, false);
        return events.stream()
                .map(e -> {
                    return eventMapper.toShortDto(e, view.getOrDefault(e.getId(), 0L));
                })
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getView(List<Event> events, boolean unique) {
        if (!events.isEmpty()) {
            List<Long> eventsId = events.stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());
            return statsService.getView(eventsId, unique);
        } else return new HashMap<>();
    }

    public EventLongDto getEventDtoById(Long id, HttpServletRequest httpServletRequest) {

        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event must be published"));

        EventLongDto eventLongDto = eventMapper.toLongDto(event);

        log.info("Событие ID = {} успешно обновлено от имени администратора", id);
        return eventLongDto;
    }

    public EventFullDto getEventDtoByIdWithHit(Long id, HttpServletRequest httpServletRequest) {

        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event must be published"));

        List<Long> eventViews = event.getViews();
        if (eventViews == null) {
            eventViews = new ArrayList<>();
        }
        if (!eventViews.contains(id)) {
            eventViews.add(id);
            event.setViews(eventViews);
            eventRepository.save(event);
        }

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setViews(Long.valueOf(eventViews.size()));

        log.info("Событие ID = {} успешно обновлено от имени администратора", id);
        return eventFullDto;
    }


    // ----- Вспомогательная часть ----

    // Вспомогательная функция обновления статуса
    private void updateStateOfEventByUser(String stateAction, Event eventSaved) {
        StateActionForUser stateActionForUser;
        try {
            stateActionForUser = StateActionForUser.valueOf(stateAction);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid parameter stateAction");
        }
        switch (stateActionForUser) {
            case SEND_TO_REVIEW:
                eventSaved.setState(EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                eventSaved.setState(EventState.CANCELED);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + stateAction);
        }
    }

    // Вспомогательная функция обновления статуса и время публикации
    private void updateStateOfEventByAdmin(String stateAction, Event eventSaved) {
        StateActionForAdmin stateActionForAdmin;
        try {
            stateActionForAdmin = StateActionForAdmin.valueOf(stateAction);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid parameter stateAction");
        }
        switch (stateActionForAdmin) {
            case REJECT_EVENT:
                if (eventSaved.getState().equals(EventState.PUBLISHED)) {
                    throw new DataConflictException("The event has already been published.");
                }
                eventSaved.setState(EventState.CANCELED);
                break;
            case PUBLISH_EVENT:
                if (!eventSaved.getState().equals(EventState.PENDING)) {
                    throw new DataConflictException("Cannot publish the event because it's not in the right state: PUBLISHED");
                }
                eventSaved.setState(EventState.PUBLISHED);
                eventSaved.setPublishedOn(LocalDateTime.now());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + stateAction);
        }
    }

    // Получение event по id

    public Event getEventById(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isPresent()) {
            return eventOptional.get();
        }
        throw new NotFoundException("Event with ID = " + eventId + " was not found");
    }


    public List<Event> getAllEventsByListId(List<Long> eventsId) {
        return eventRepository.findAllById(eventsId);
    }

    public void updateEventData(String dataTime, Event eventSaved) {
        if (LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .isBefore(LocalDateTime.now().plusHours(1))) {
            throw new InvalidRequestException("The start date of the event to be modified must be no earlier " +
                    "than one hour from the date of publication.");
        }
        eventSaved.setEventDate(LocalDateTime.parse(dataTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public Optional<Event> findByCategory(Category category) {
        return eventRepository.findByCategory(category);
    }
}