package ru.practicum.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsResponseDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testValidHitsDto() {
        StatsResponseDto statsResponseDto = new StatsResponseDto("myApp", "/my-uri", 100L);
        Set<ConstraintViolation<StatsResponseDto>> violations = validator.validate(statsResponseDto);
        assertTrue(violations.isEmpty(), "There should be no validation errors for a valid HitsDto");
    }

    @Test
    void testAppIsBlank() {
        StatsResponseDto statsResponseDto = new StatsResponseDto("", "/my-uri", 100L);
        Set<ConstraintViolation<StatsResponseDto>> violations = validator.validate(statsResponseDto);
        assertEquals(1, violations.size(), "There should be one validation error for a blank app");
        ConstraintViolation<StatsResponseDto> violation = violations.iterator().next();
        assertEquals("Идентификатор сервиса для которого записывается информация не должен быть пустым.", violation.getMessage());
    }

    @Test
    void testUriIsBlank() {
        StatsResponseDto statsResponseDto = new StatsResponseDto("myApp", "", 100L);
        Set<ConstraintViolation<StatsResponseDto>> violations = validator.validate(statsResponseDto);
        assertEquals(1, violations.size(), "There should be one validation error for a blank uri");
        ConstraintViolation<StatsResponseDto> violation = violations.iterator().next();
        assertEquals("URI для которого был осуществлен запрос не должен быть пустым.", violation.getMessage());
    }

    @Test
    void testBothAppAndUriAreBlank() {
        StatsResponseDto statsResponseDto = new StatsResponseDto("", "", 100L);
        Set<ConstraintViolation<StatsResponseDto>> violations = validator.validate(statsResponseDto);
        assertEquals(2, violations.size(), "There should be two validation errors when app and uri are blank");
    }

    @Test
    void testHitsCanBeNull() {
        StatsResponseDto statsResponseDto = new StatsResponseDto("myApp", "/my-uri", null);
        Set<ConstraintViolation<StatsResponseDto>> violations = validator.validate(statsResponseDto);
        assertTrue(violations.isEmpty(), "HitsDto should allow null for the hits field as it is not constrained");
    }
}
