package ru.practicum.application.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.application.api.dto.category.CategoryDto;
import ru.practicum.application.api.dto.enums.EventState;
import ru.practicum.application.api.dto.enums.StateAction;
import ru.practicum.application.api.dto.event.EventFullDto;
import ru.practicum.application.api.dto.request.EventRequestDto;
import ru.practicum.application.api.dto.user.UserDto;
import ru.practicum.application.api.exception.ConflictException;
import ru.practicum.application.api.exception.NotFoundException;
import ru.practicum.application.api.exception.ValidationException;
import ru.practicum.application.api.exception.WrongDataException;
import ru.practicum.application.api.request.event.UpdateEventAdminRequest;
import ru.practicum.application.category.client.CategoryClient;
import ru.practicum.application.event.repository.EventRepository;
import ru.practicum.application.event.repository.LocationRepository;
import ru.practicum.application.request.client.EventRequestClient;
import ru.practicum.application.event.mapper.EventMapper;
import ru.practicum.application.event.model.Event;
import ru.practicum.application.user.client.UserClient;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.stats.client.AnalyzerClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.application.api.util.JsonFormatPattern.JSON_FORMAT_PATTERN_FOR_TIME;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminEventServiceImpl implements AdminEventService {
    final EventRepository eventRepository;
    final LocationRepository locationRepository;

    final UserClient userClient;
    final CategoryClient categoryClient;
    final EventRequestClient requestClient;
    final AnalyzerClient analyzerClient;

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) throws ValidationException {

        List<EventFullDto> eventDtos = null;
        List<EventState> eventStateList = null;

        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new ValidationException("Время начала поиска позже времени конца поиска");
            }
        }

        if ((states == null) || (states.isEmpty())) {
            eventStateList = Arrays.stream(EventState.values()).collect(Collectors.toList());
        } else {
            eventStateList = states.stream().map(EventState::valueOf).collect(Collectors.toList());
        }

        if (users == null && categories == null) {
            Map<Long, Event> allEventsWithDates = new ArrayList<>(eventRepository.findAll(PageRequest.of(from / size, size)).getContent())
                    .stream().collect(Collectors.toMap(Event::getId, e -> e));
            List<EventRequestDto> requestsByEventIds = requestClient.findByEventIds(allEventsWithDates.values().stream()
                    .mapToLong(Event::getId).boxed().collect(Collectors.toList()));

            List<Long> usersIds = allEventsWithDates.values().stream().map(Event::getInitiator).toList();
            Set<Long> categoriesIds = allEventsWithDates.values()
                    .stream().map(Event::getCategory).collect(Collectors.toSet());
            Map<Long, UserDto> usersByRequests = userClient.getUsersList(usersIds, 0, Math.max(allEventsWithDates.size(), 1))
                    .stream()
                    .collect(Collectors.toMap(UserDto::getId, userDto -> userDto));
            Map<Long, CategoryDto> categoriesByRequests = categoryClient.getCategoriesByIds(categoriesIds).stream()
                    .collect(Collectors.toMap(CategoryDto::getId, categoryDto -> categoryDto));

            eventDtos = allEventsWithDates.values().stream()
                    .map(e -> EventMapper.mapEventToFullDto(e,
                            requestsByEventIds.stream()
                                    .filter(r -> r.getId().equals(e.getId()))
                                    .count(),
                            categoriesByRequests.get(e.getCategory()),
                            usersByRequests.get(e.getInitiator())))
                    .toList();
        } else {
            Map<Long, Event> allEventsWithDates = eventRepository.findAllEventsWithDates(users,
                    eventStateList, categories, rangeStart, rangeEnd,
                    PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "e.eventDate")))
                    .stream().collect(Collectors.toMap(Event::getId, e -> e));

            List<EventRequestDto> requestsByEventIds = requestClient.findByEventIds(allEventsWithDates.values().stream()
                    .mapToLong(Event::getId).boxed().collect(Collectors.toList()));

            List<Long> usersIds = allEventsWithDates.values().stream().map(Event::getInitiator).toList();
            Set<Long> categoriesIds = allEventsWithDates.values()
                    .stream().map(Event::getCategory).collect(Collectors.toSet());
            Map<Long, UserDto> usersByRequests = userClient.getUsersList(usersIds, 0, Math.max(allEventsWithDates.size(), 1))
                    .stream()
                    .collect(Collectors.toMap(UserDto::getId, userDto -> userDto));
            Map<Long, CategoryDto> categoriesByRequests = categoryClient.getCategoriesByIds(categoriesIds).stream()
                    .collect(Collectors.toMap(CategoryDto::getId, categoryDto -> categoryDto));

             eventDtos = allEventsWithDates.values().stream()
                    .map(e -> EventMapper.mapEventToFullDto(e,
                            requestsByEventIds.stream()
                                    .filter(r -> r.getEvent().equals(e.getId()))
                                    .count(),
                            categoriesByRequests.get(e.getCategory()),
                            usersByRequests.get(e.getInitiator())))
                    .toList();
        }

        if (!eventDtos.isEmpty()) {
            HashMap<Long, Integer> eventIdsWithViewsCounter = new HashMap<>();
            for (EventFullDto dto : eventDtos) {
                eventIdsWithViewsCounter.put(dto.getId(), 0);
            }
            ArrayList<Long> longs = eventDtos.stream()
                    .map(EventFullDto::getId).collect(Collectors.toCollection(ArrayList::new));
            List<EventRequestDto> requests = requestClient.getByEventAndStatus(longs, "CONFIRMED");
            return eventDtos.stream()
                    .peek(dto -> dto.setConfirmedRequests(
                            requests.stream()
                                    .filter((request -> request.getEvent().equals(dto.getId())))
                                    .count()
                    ))
                    .peek(dto -> {
                        List<RecommendedEventProto> protos = analyzerClient.getInteractionsCount(
                                InteractionsCountRequestProto.newBuilder().addEventId(dto.getId()).build()
                        );
                        dto.setRating(protos.isEmpty() ? 0.0 : protos.getFirst().getScore());
                    })
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) throws ConflictException, ValidationException, NotFoundException, WrongDataException {
        log.info("Редактирование данных события и его статуса");
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не существует " + eventId));

        if (LocalDateTime.now().isAfter(event.getEventDate().minus(2, ChronoUnit.HOURS))) {
            throw new ConflictException("До начала события меньше часа, изменение события невозможно");
        }
        if (!event.getState().equals(EventState.PENDING)) {
            throw new ConflictException("Событие не в состоянии \"Ожидание публикации\", изменение события невозможно");
        }
        if ((!StateAction.REJECT_EVENT.toString().equals(updateRequest.getStateAction())
                && event.getState().equals(EventState.PUBLISHED))) {
            throw new ConflictException("Отклонить опубликованное событие невозможно");
        }
        updateEventWithAdminRequest(event, updateRequest);
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Событие уже завершилось");
        }
        saveLocation(event);
        event = eventRepository.save(event);
        EventFullDto eventFullDto = getEventFullDto(event);
        return getViewsCounter(eventFullDto);
    }

    EventFullDto getEventFullDto(Event event) throws NotFoundException {
        Long confirmed = requestClient.countByEventAndStatuses(event.getId(), List.of("CONFIRMED"));
        return EventMapper.mapEventToFullDto(event, confirmed, categoryClient.getCategoryById(event.getCategory()),
                userClient.getById(event.getInitiator()));
    }

    Event getEventById(Long eventId) throws NotFoundException {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие " + eventId + " не найдено"));
    }

    void updateEventWithAdminRequest(Event event, UpdateEventAdminRequest updateRequest) throws NotFoundException, WrongDataException {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            if (!categoryClient.existById(updateRequest.getCategory())) {
                throw new NotFoundException("Категория не найдена " + updateRequest.getCategory());
            }
            event.setCategory(updateRequest.getCategory());
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateRequest.getEventDate(), DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME)));
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(EventMapper.mapDtoToLocation(updateRequest.getLocation()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction().toUpperCase()) {
                case "PUBLISH_EVENT":
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new WrongDataException("Неверное состояние события, не удалось обновить");
            }
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    void saveLocation(Event event) {
        event.setLocation(locationRepository.save(event.getLocation()));
        log.info("Локация сохранена {}", event.getLocation().getId());
    }

    EventFullDto getViewsCounter(EventFullDto eventFullDto) {
        List<RecommendedEventProto> protos = analyzerClient.getInteractionsCount(
                InteractionsCountRequestProto.newBuilder().addEventId(eventFullDto.getId()).build()
        );
        Double rating = protos.isEmpty() ? 0.0 : protos.getFirst().getScore();
        eventFullDto.setRating(rating);
        return eventFullDto;
    }

}
