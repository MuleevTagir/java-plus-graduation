package ru.practicum.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StatsRequestDtoTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testValidationFailsWhenAppIsBlank() {
        StatsRequestDto hitDto = new StatsRequestDto("", "some-uri", "192.168.0.1", LocalDateTime.now());

        Set<ConstraintViolation<StatsRequestDto>> violations = validator.validate(hitDto);
        assertFalse(violations.isEmpty());
        assertEquals("Идентификатор сервиса для которого записывается информация не должен быть пустым.",
                violations.iterator().next().getMessage());
    }

    @Test
    void testValidationFailsWhenUriIsBlank() {
        StatsRequestDto hitDto = new StatsRequestDto("app-name", "", "192.168.0.1", LocalDateTime.now());

        Set<ConstraintViolation<StatsRequestDto>> violations = validator.validate(hitDto);
        assertFalse(violations.isEmpty());
        assertEquals("URI для которого был осуществлен запрос не должен быть пустым.",
                violations.iterator().next().getMessage());
    }

    @Test
    void testValidationFailsWhenIpIsBlank() {
        StatsRequestDto hitDto = new StatsRequestDto("app-name", "some-uri", "", LocalDateTime.now());

        Set<ConstraintViolation<StatsRequestDto>> violations = validator.validate(hitDto);
        assertFalse(violations.isEmpty());
        assertEquals("IP-адрес пользователя, осуществившего запрос не можен быть пустым",
                violations.iterator().next().getMessage());
    }

    @Test
    void testValidationPassesWithValidData() {
        StatsRequestDto hitDto = new StatsRequestDto("app-name", "some-uri", "192.168.0.1", LocalDateTime.now());

        Set<ConstraintViolation<StatsRequestDto>> violations = validator.validate(hitDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreateHitDtoWithValidData() {
        StatsRequestDto hitDto = new StatsRequestDto("app-name", "some-uri", "192.168.0.1", LocalDateTime.now());

        assertNotNull(hitDto);
        assertEquals("app-name", hitDto.getApp());
        assertEquals("some-uri", hitDto.getUri());
        assertEquals("192.168.0.1", hitDto.getIp());
    }
}
