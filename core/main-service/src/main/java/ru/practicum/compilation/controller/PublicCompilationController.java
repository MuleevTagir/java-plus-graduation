package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationResponseDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/compilations")
public class PublicCompilationController {
    private final CompilationService service;

    @GetMapping
    public List<CompilationResponseDto> get(@RequestParam(value = "pinned", required = false) Boolean pinned,
                                            @RequestParam(value = "from", defaultValue = "0") Integer from,
                                            @RequestParam(value = "size",
                                                    defaultValue = "10") Integer size) {
        log.info("GET /compilations with params: pinned = {}, from = {}, size = {}", pinned, from, size);
        return service.get(pinned, from, size);
    }

    @GetMapping("/{id}")
    public CompilationResponseDto getById(@PathVariable Long id) {
        log.info("GET /compilations/{id} with id={}", id);
        return service.getById(id);
    }
}
