package ru.practicum.errors.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.errors.exceptions.DataConflictException;
import ru.practicum.errors.exceptions.InvalidRequestException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.errors.exceptions.ValidationException;
import ru.practicum.errors.model.ApiError;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        return new ApiError(getErrors(e), e.getMessage(), "No user with this id", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({InvalidRequestException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(Exception e) {
        return new ApiError(getErrors(e), e.getMessage(), "Not correct data in request", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ApiError(getErrors(e), e.getMessage(),
                "Incorrectly made request.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({DataConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataConflictException(Exception e) {
        return new ApiError(getErrors(e), e.getMessage(),
                "Integrity constraint has been violated.", HttpStatus.CONFLICT);
    }

    private String getErrors(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
