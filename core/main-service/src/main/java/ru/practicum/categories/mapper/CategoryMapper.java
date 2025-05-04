package ru.practicum.categories.mapper;

import org.mapstruct.Mapper;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category mapToCategory(NewCategoryDto newCategoryDto);

    CategoryDto mapToCategoryDto(Category category);

    List<CategoryDto> mapToListCategoryDto(List<Category> categories);

}
