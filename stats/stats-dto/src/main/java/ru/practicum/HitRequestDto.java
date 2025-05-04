package ru.practicum;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HitRequestDto {

    @NotBlank(message = "Название сервиса не может быть пустым")
    private String app;

    @NotBlank(message = "Название URI не может быть пустым")
    private String uri;

    @NotBlank(message = "IP-адрес не может быть пустым")
    private String ip;

    @NotBlank(message = "Временная метка не может быть пустым")
    private String timestamp;
}
