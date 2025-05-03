package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationRequestDto;
import ru.practicum.compilation.dto.CompilationResponseDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository repository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationResponseDto create(CompilationRequestDto request) {
        log.info("Create method started with params: {}", request.toString());

        Compilation compilation = compilationMapper.fromDto(request);

        List<Event> events = new ArrayList<>();
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            events = eventRepository.getAllByIds(request.getEvents());
        }
        compilation.setEvents(events);
        compilation = repository.save(compilation);

        return getDto(compilation);
    }

    @Override
    public CompilationResponseDto update(CompilationRequestDto request, Long id) {
        log.info("Update method started with params: {}, id = {}", request.toString(), id);
        Compilation compilation = getCompilation(id);

        log.info("Updating model");

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() == null) {
            compilation.setPinned(false);
        } else {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            List<Event> events = eventRepository.getAllByIds(request.getEvents());
            compilation.setEvents(events);
        }

        return compilationMapper.toDto(repository.save(compilation));
    }

    @Override
    public void delete(Long id) {
        log.info("Delete method started with id = {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<CompilationResponseDto> get(Boolean pinned, Integer offset, Integer size) {
        log.info("Get method started with param: pinned = {}, offset = {}, size = {}", pinned, offset, size);
        log.info("Checking pinned param = {}", pinned);

        if (pinned == null) {
            return compilationMapper.toDtoList(repository.getCompilations(size, offset));
        }

        log.info("Returning compilations list by pinned param = {}", pinned);
        return compilationMapper.toDtoList(repository.getCompilationsByPinned(pinned, offset, size));
    }

    @Override
    public CompilationResponseDto getById(Long id) {
        log.info("Get by id method started with id = {}", id);
        Compilation compilation = getCompilation(id);

        return getDto(compilation);
    }

    private Compilation getCompilation(Long id) {
        log.info("Getting compilation with id = {}", id);
        return repository.findById(id).orElseThrow(() -> new NotFoundException("No such compilation with id: " + id));
    }

    private CompilationResponseDto getDto(Compilation compilation) {
        CompilationResponseDto responseDto = compilationMapper.toDto(compilation);
        responseDto.setEvents(new ArrayList<>());

        if (compilation.getEvents() != null) {
            List<EventShortDto> events = compilation.getEvents().stream()
                    .map(eventMapper::toEventShortDto)
                    .toList();

            responseDto.setEvents(events);
        }

        return responseDto;
    }
}
