package ru.practicum.mappers;

import org.mapstruct.Mapper;
import ru.practicum.StatsResponseDto;
import ru.practicum.model.Hit;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    StatsResponseDto toDto(Hit hit);

    List<StatsResponseDto> toDtoList(List<Hit> hits);
}
