package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationRequestDto;
import ru.practicum.compilation.dto.CompilationResponseDto;

import java.util.List;

public interface CompilationService {

    CompilationResponseDto create(CompilationRequestDto request);

    CompilationResponseDto update(CompilationRequestDto request, Long id);

    void delete(Long id);

    List<CompilationResponseDto> get(Boolean pinned, Integer from, Integer size);

    CompilationResponseDto getById(Long id);
}
