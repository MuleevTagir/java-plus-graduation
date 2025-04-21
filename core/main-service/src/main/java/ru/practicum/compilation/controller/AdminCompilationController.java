package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.annotation.Create;
import ru.practicum.compilation.annotation.Update;
import ru.practicum.compilation.dto.CompilationRequestDto;
import ru.practicum.compilation.dto.CompilationResponseDto;
import ru.practicum.compilation.service.CompilationService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController {
    private final CompilationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationResponseDto create(@RequestBody @Validated(Create.class) CompilationRequestDto body) {
        log.info("POST /admin/compilations/create with body={}", body);
        return service.create(body);
    }

    @PatchMapping("/{id}")
    public CompilationResponseDto update(@RequestBody @Validated(Update.class) CompilationRequestDto body, @PathVariable Long id) {
        log.info("PATCH /admin/compilations/update with body={}", body);
        return service.update(body, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("DELETE /admin/compilations/delete with id={}", id);
        service.delete(id);
    }
}
