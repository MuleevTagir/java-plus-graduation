package ru.practicum.categories.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.service.CategoryService;

@Slf4j
@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Get POST request /admin/categories with body = {}", newCategoryDto);
        CategoryDto response = categoryService.createCategory(newCategoryDto);
        log.info("Send response with body = {}", response);
        return response;
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Get DELETE request /admin/categories/{}", catId);
        categoryService.deleteCategory(catId);
        log.info("Category deleted");
    }

    @PatchMapping("/{catId}")
    public CategoryDto patchCategory(@Positive @PathVariable Long catId,
                                     @Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Get PATCH request /admin/categories/{} with body = {}", catId, newCategoryDto);
        CategoryDto response = categoryService.updateCategory(newCategoryDto, catId);
        log.info("Send response with body = {}", response);
        return response;
    }
}
