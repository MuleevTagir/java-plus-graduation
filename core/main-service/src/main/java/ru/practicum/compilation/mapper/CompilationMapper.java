package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.compilation.dto.CompilationRequestDto;
import ru.practicum.compilation.dto.CompilationResponseDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class CompilationMapper {
    @Autowired
    protected EventMapper eventMapper;

    public Compilation fromDto(CompilationRequestDto request) {
        Compilation compilation = new Compilation();
        compilation.setTitle(request.getTitle());

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        } else {
            compilation.setPinned(false);
        }

        return compilation;
    }

    public CompilationResponseDto toDto(Compilation compilation) {
        CompilationResponseDto compilationResponseDto = new CompilationResponseDto();
        compilationResponseDto.setId(compilation.getId());
        compilationResponseDto.setTitle(compilation.getTitle());
        compilationResponseDto.setPinned(compilation.isPinned());
        compilationResponseDto.setEvents(
                compilation.getEvents().stream()
                        .map(event -> eventMapper.toEventShortDto(event))
                        .toList()
        );
        return compilationResponseDto;
    }

    public List<CompilationResponseDto> toDtoList(List<Compilation> compilations) {
        List<CompilationResponseDto> compilationResponseDtos = new ArrayList<>();
        for (Compilation compilation : compilations) {
            CompilationResponseDto dto = toDto(compilation);
            dto.setEvents(compilation.getEvents().stream()
                    .map(event -> eventMapper.toEventShortDto(event))
                    .toList()
            );
            compilationResponseDtos.add(dto);
        }

        return compilationResponseDtos;
    }
}
