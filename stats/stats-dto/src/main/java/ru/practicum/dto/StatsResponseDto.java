package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsResponseDto {
    @NotBlank(message = "Идентификатор сервиса для которого записывается информация не должен быть пустым.")
    String app;

    @NotBlank(message = "URI для которого был осуществлен запрос не должен быть пустым.")
    String uri;

    Long hits;
}
