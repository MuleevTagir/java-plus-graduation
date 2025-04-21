package ru.practicum.users.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/admin/users")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(value = "from", defaultValue = "0") int from,
                                  @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("Get GET request with params ids = {}, from = {}, size = {}", ids, from, size);
        List<UserDto> response = userService.getUsers(ids, from, size);
        log.info("Send response with size = {}", response.size());
        return response;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest userRequest) {
        log.info("Get POST request /admin/users with body {}", userRequest);
        UserDto response = userService.createUser(userRequest);
        log.info("Send response with body {}", response);
        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive Long id) {
        log.info("Get DELETE request /{}", id);
        userService.deleteUser(id);
        log.info("User deleted with id = {}", id);
    }
}
